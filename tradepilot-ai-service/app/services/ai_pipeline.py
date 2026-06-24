import json
import logging
import re
from datetime import datetime, timezone

from json_repair import repair_json

import httpx

from app.kafka.producer import publish_ai_result

logger = logging.getLogger(__name__)

OLLAMA_URL = "http://localhost:11434/api/chat"
OLLAMA_MODEL = "phi3:mini"
OLLAMA_TIMEOUT = httpx.Timeout(connect=5.0, read=120.0, write=10.0, pool=5.0)

INTENT_CLASSES = {
    "price_inquiry",
    "bulk_order",
    "repeat_order",
    "payment_follow_up",
    "delivery_status",
    "complaint",
    "negotiation_counter",
    "relationship_message",
}

SYSTEM_PROMPT = """Analyse the trade message. Reply with ONLY a JSON object, no preamble. Every value must be a short string or null. Maximum 5 words per value. Never explain or elaborate.

MANDATORY: You MUST always set detectedIntent to exactly one of these 8 values (no other values allowed, never null, never omit):
price_inquiry, bulk_order, repeat_order, payment_follow_up, delivery_status, complaint, negotiation_counter, relationship_message
MANDATORY: You MUST always set confidenceScore to a numeric value between 0.01 and 1.0. Never return 0, 0.0, null, or omit it.

Classification rules — apply in order:
- Message asks about rate, price, cost, bhav, daam → price_inquiry
- Message asks for discount, less rate, negotiation, kam karo → negotiation_counter
- Message says hello, thanks, will call later, greetings → relationship_message
- Message orders large quantity (tons, MT, quintal) → bulk_order
- Default to price_inquiry if about prices or rates. Default to relationship_message for greetings or social messages.

confidenceScore rules — decimal 0.0 to 1.0, NEVER null, NEVER omit:
- Clear price inquiry in Hindi trade language (bhav, rate, daam) → 0.85-0.95
- English price inquiry → 0.75-0.90
- Ambiguous or mixed messages → 0.40-0.60
- Greetings or social messages → 0.90-0.95

extractedEntities extraction rules — MANDATORY:
- "commodity" = base material type ONLY. Examples: TMT, HRC, MS Angle, MS Pipe, DAP. Never put a grade code here.
- "grade" = specification or quality code. Examples: Fe415, Fe500, Fe500D, Fe550, Fe600. Grade codes always start with Fe followed by numbers.
- If message says "Fe500D ka bhav" → commodity is "TMT", grade is "Fe500D".
- If you see Fe followed by numbers anywhere in the message, it always goes in "grade", never in "commodity".
- "discountPercent" = numeric percentage discount requested, ONLY for negotiation_counter messages. Examples: "5% discount chahiye" → 5.0, "give me 3 percent off" → 3.0, "can you reduce price a little" → null (no specific number). ALWAYS include discountPercent in extractedEntities — set to null if no specific percentage is mentioned, never omit it.

For all other extractedEntities fields: use JSON null (no quotes) for absent fields, never the string "null". Do not guess or infer beyond what is explicitly stated.

{"detectedIntent":"price_inquiry|bulk_order|repeat_order|payment_follow_up|delivery_status|complaint|negotiation_counter|relationship_message","confidenceScore":0.0,"extractedEntities":{"commodity":null,"grade":null,"quantity":null,"unit":"MT|quintal|bundle|bag|piece or null","priceSignal":null,"paymentTerms":"advance|net-30|LC|other or null","deliveryTerms":"ex-works|ex-Mumbai|door delivery|other or null","urgencyMarker":"aaj|urgent|jaldi or null","discountPercent":null}}"""


def _determine_routing(confidence: float) -> str:
    if confidence >= 0.85:
        return "AUTO_SEND"
    if confidence >= 0.50:
        return "PENDING_APPROVAL"
    return "ESCALATED"


def _build_fallback(whatsapp_message_id: str, from_number: str) -> dict:
    return {
        "whatsappMessageId": whatsapp_message_id,
        "fromNumber": from_number,
        "detectedIntent": "price_inquiry",
        "confidenceScore": 0.0,
        "extractedEntities": {},
        "suggestedReply": None,
        "routingDecision": "ESCALATED",
        "processedAt": datetime.now(timezone.utc).isoformat(),
    }


async def _call_ollama(text: str) -> dict:
    payload = {
        "model": OLLAMA_MODEL,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text},
        ],
        "stream": False,
        "options": {"temperature": 0.1, "num_predict": 500},
    }
    # trust=env=False prevents httpx from picking up HTTP_PROXY / HTTPS_PROXY
    # environment variables, which can intercept localhost traffic on some machines.
    async with httpx.AsyncClient(timeout=OLLAMA_TIMEOUT, trust_env=False) as client:
        response = await client.post(OLLAMA_URL, json=payload)
        response.raise_for_status()
        return response.json()


async def process_message(event: dict):
    """
    Main AI processing pipeline.
    Calls Ollama (phi3:mini) for entity extraction + intent classification,
    then routes based on confidence score.
    """
    whatsapp_message_id = event.get("messageId", "unknown")
    from_number = event.get("from", "unknown")
    text_content = event.get("textBody", "")

    logger.info(
        "Processing message %s from %s: %s",
        whatsapp_message_id,
        from_number,
        text_content,
    )

    result = _build_fallback(whatsapp_message_id, from_number)

    try:
        ollama_response = await _call_ollama(text_content)
        raw_content: str = ollama_response["message"]["content"].strip()

        # Strip optional markdown code fences the model might emit despite instructions
        if raw_content.startswith("```"):
            lines = raw_content.splitlines()
            raw_content = "\n".join(
                line for line in lines if not line.startswith("```")
            ).strip()

        logger.debug("Ollama raw response for message %s: %r", whatsapp_message_id, raw_content)

        match = re.search(r"\{.*\}", raw_content, re.DOTALL)
        if match:
            raw_content = match.group()

        try:
            parsed: dict = json.loads(raw_content)
        except json.JSONDecodeError:
            repaired = repair_json(raw_content)
            parsed = json.loads(repaired)
            logger.warning(
                "Ollama response for message %s required JSON repair",
                whatsapp_message_id,
            )

        if isinstance(parsed, list):
            parsed = parsed[0] if parsed and isinstance(parsed[0], dict) else {}
            logger.warning(
                "Ollama returned a JSON list instead of object for message %s, extracted first element",
                whatsapp_message_id,
            )

        intent = parsed.get("detectedIntent") or "price_inquiry"
        if intent not in INTENT_CLASSES:
            logger.warning("Unknown intent '%s', defaulting to price_inquiry", intent)
            intent = "price_inquiry"

        confidence = float(parsed.get("confidenceScore") or 0.0)
        confidence = max(0.0, min(1.0, confidence))
        if confidence == 0.0:
            _intent_defaults = {
                "price_inquiry": 0.80,
                "relationship_message": 0.90,
                "negotiation_counter": 0.85,
            }
            confidence = _intent_defaults.get(intent, 0.75)
            logger.warning(
                "confidenceScore was 0.0 for message %s (intent=%s), using default %.2f",
                whatsapp_message_id,
                intent,
                confidence,
            )

        entities = parsed.get("extractedEntities", {})

        result.update(
            {
                "detectedIntent": intent,
                "confidenceScore": confidence,
                "extractedEntities": entities,
                "routingDecision": _determine_routing(confidence),
                "processedAt": datetime.now(timezone.utc).isoformat(),
            }
        )

        logger.info(
            "Message %s → intent=%s confidence=%.2f routing=%s",
            whatsapp_message_id,
            intent,
            confidence,
            result["routingDecision"],
        )

    except json.JSONDecodeError as exc:
        logger.error(
            "Ollama JSON unparseable even after repair for message %s — raw: %r — error: %s",
            whatsapp_message_id,
            exc.doc,
            exc,
            exc_info=True,
        )
    except httpx.HTTPStatusError as exc:
        # raise_for_status() path: Ollama returned 4xx/5xx
        logger.error(
            "Ollama returned HTTP %s for message %s — url=%s body=%r",
            exc.response.status_code,
            whatsapp_message_id,
            exc.request.url,
            exc.response.text[:500],
            exc_info=True,
        )
    except httpx.TransportError as exc:
        # Network-level failure: ConnectError, ConnectTimeout, ReadError, etc.
        # str(exc) is often "" for anyio-backed transport errors; repr() always shows type+args.
        logger.error(
            "Ollama transport error for message %s — %s: %r",
            whatsapp_message_id,
            type(exc).__name__,
            repr(exc),
            exc_info=True,
        )
    except Exception as exc:
        logger.error(
            "Unexpected error processing message %s — %s: %r",
            whatsapp_message_id,
            type(exc).__name__,
            repr(exc),
            exc_info=True,
        )

    await publish_ai_result(from_number, result)
    logger.info("AI result published for message %s", whatsapp_message_id)
