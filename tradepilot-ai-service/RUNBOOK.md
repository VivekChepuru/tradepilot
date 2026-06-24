# TradePilot AI Service — Developer Runbook

See `TradePilot_Core/RUNBOOK.md` for the full stack runbook.
This file covers the AI service specifically.

---

## 1. Starting the Service

```powershell
cd C:\Users\vivek\Files\IdeaProjects\tradepilot-ai-service
venv\Scripts\activate
python -m app.main
```

Confirm you see:
```
Starting TradePilot AI Service
Kafka consumer started — listening on tradepilot.messages.inbound
Uvicorn running on http://0.0.0.0:8001
```

---

## 2. Verify Running

| What | URL | Expected |
|---|---|---|
| Health check | http://localhost:8001/health | `{"status":"ok"}` |
| Swagger UI | http://localhost:8001/docs | Auto-generated API docs |

---

## 3. Project Structure

```
tradepilot-ai-service/
├── app/
│   ├── main.py          — FastAPI app, lifespan, router registration
│   ├── config.py        — Settings loaded from .env via pydantic-settings
│   ├── api/
│   │   └── health.py    — GET /health endpoint
│   ├── kafka/
│   │   ├── consumer.py  — Consumes tradepilot.messages.inbound
│   │   └── producer.py  — Publishes to tradepilot.ai.results
│   ├── models/
│   │   └── message.py   — InboundMessageEvent, AiProcessingResult
│   └── services/
│       └── ai_pipeline.py — Main processing pipeline (stub → real AI)
├── .env                 — Secrets (gitignored)
├── .env.example         — Template for .env (committed)
├── requirements.txt     — Pinned dependencies
└── .gitignore
```

---

## 4. Environment Variables

Copy `.env.example` to `.env` and fill in values:

| Variable | Value | Notes |
|---|---|---|
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka broker |
| KAFKA_CONSUMER_GROUP | tradepilot-ai-service | Consumer group ID |
| KAFKA_TOPIC_MESSAGES_INBOUND | tradepilot.messages.inbound | Reads from here |
| KAFKA_TOPIC_AI_RESULTS | tradepilot.ai.results | Publishes here |
| ANTHROPIC_API_KEY | fill later | Needed in Week 4 |
| APP_HOST | 0.0.0.0 | |
| APP_PORT | 8001 | |

---

## 5. Message Flow

```
Spring Boot                    AI Service                     Kafka
    │                              │                            │
    │── POST /webhook ────────────▶│                            │
    │                              │                            │
    │◀── 200 EVENT_RECEIVED ───────│                            │
    │                              │                            │
    │── publish InboundMessageEvent ──────────────────────────▶│
    │                     tradepilot.messages.inbound           │
    │                              │                            │
    │                              │◀── consume ───────────────│
    │                              │                            │
    │                              │── process_message() ──────│
    │                              │   (ai_pipeline.py)        │
    │                              │                            │
    │                              │── publish AiProcessingResult ▶│
    │                         tradepilot.ai.results             │
```

---

## 6. Current Pipeline Status

| Stage | Status | Location |
|---|---|---|
| Kafka consumer | ✅ Working | app/kafka/consumer.py |
| Kafka producer | ✅ Working | app/kafka/producer.py |
| Entity extraction | ✅ Working | app/services/ai_pipeline.py |
| Intent classification | ✅ Working | app/services/ai_pipeline.py |
| Confidence scoring | ✅ Working | app/services/ai_pipeline.py |
| Response generation | 🔜 Week 5 | app/services/ai_pipeline.py |

Current model: Phi-3 Mini via Ollama (localhost:11434)
Inference time: 20-30s on CPU (i7-13700H, no GPU)
Routing: AUTO_SEND ≥0.85 / PENDING_APPROVAL 0.50-0.84 / ESCALATED <0.50

Current stub returns:
```json
{
  "detectedIntent": "price_inquiry",
  "confidenceScore": 0.0,
  "extractedEntities": {},
  "routingDecision": "ESCALATED"
}
```

---

## 7. Dependencies

| Package | Version | Purpose |
|---|---|---|
| fastapi | 0.115.0 | Web framework |
| uvicorn | 0.30.6 | ASGI server |
| aiokafka | 0.11.0 | Async Kafka consumer/producer |
| pydantic | 2.8.2 | Data validation |
| pydantic-settings | 2.4.0 | .env config loading |
| python-dotenv | 1.0.1 | .env file support |
| httpx | 0.27.2 | HTTP client for Anthropic API |

Install all:
```powershell
pip install -r requirements.txt
```

---

## 8. Build Progress (Updated 2026-06-23)

- [x] FastAPI app scaffold with lifespan management
- [x] Kafka consumer — reads from tradepilot.messages.inbound
- [x] Kafka producer — publishes to tradepilot.ai.results
- [x] Pydantic models for InboundMessageEvent and AiProcessingResult
- [x] Health endpoint
- [x] Ollama integration with Phi-3 Mini
- [x] Commodity entity extraction with normalization
- [x] Intent classification (8 classes: price_inquiry, bulk_order, repeat_order, payment_follow_up, delivery_status, complaint, negotiation_counter, relationship_message)
- [x] Confidence scoring and routing logic
- [x] json-repair fallback for malformed model output
- [x] Safe quantity parsing (handles "20 tons" → 20.0)
- [x] Confidence fallback — 0.0 score replaced with intent-based defaults (price_inquiry=0.80, relationship_message=0.90, negotiation_counter=0.85, others=0.75)
- [x] isinstance guard — Ollama JSON list response handled gracefully
- [x] discountPercent extraction for negotiation_counter messages
- [x] Grade/commodity separation rules — Fe+digits always goes in grade field
- [x] extractedEntities extraction rules added to system prompt
- [x] English-only testing verified (Hindi/regional deferred — Phi-3 Mini unreliable)
- [ ] Price engine integration (complete — handled in Spring Boot)
- [ ] Response generation (Week 12)
- [ ] Negotiation intelligence with memory (Week 12)
- [ ] Hindi/regional language support (pending better model or Meta API activation)
---

## 9. Local AI Model Setup

TradePilot uses Ollama for local inference — no API key required.

### Install Ollama
Download from https://ollama.com/download and install.
Ollama runs as a system tray app on Windows — launch it from Start menu.

### Pull the model
```powershell
ollama pull phi3:mini
```

### Verify
```powershell
curl http://localhost:11434/api/tags
```

Should return phi3:mini in the models list.

### Start order
Ollama must be running before starting the AI service.
It starts automatically on Windows login once installed.
Check system tray for the llama icon to confirm it's running.

