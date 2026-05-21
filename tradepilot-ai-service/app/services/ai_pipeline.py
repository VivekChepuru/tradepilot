import logging
from datetime import datetime, timezone
from app.kafka.producer import publish_ai_result

logger = logging.getLogger(__name__)

async def process_message(event: dict):
    """
    Main AI processing pipeline.
    Currently a stub — returns a hardcoded result.
    Real implementation: entity extraction → intent classification
    → confidence scoring → response generation.
    """
    whatsapp_message_id = event.get("whatsappMessageId", "unknown")
    from_number = event.get("fromNumber", "unknown")
    text_content = event.get("textContent", "")

    logger.info("Processing message %s from %s: %s",
                whatsapp_message_id, from_number, text_content)

    # STUB — hardcoded result until AI modules are built in Week 4
    result = {
        "whatsappMessageId": whatsapp_message_id,
        "fromNumber": from_number,
        "detectedIntent": "price_inquiry",
        "confidenceScore": 0.0,
        "extractedEntities": {},
        "suggestedReply": None,
        "routingDecision": "ESCALATED",
        "processedAt": datetime.now(timezone.utc).isoformat(),
    }

    await publish_ai_result(from_number, result)
    logger.info("AI result published for message %s", whatsapp_message_id)