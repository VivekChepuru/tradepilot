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
| Entity extraction | 🔜 Week 4 | app/services/ai_pipeline.py |
| Intent classification | 🔜 Week 4 | app/services/ai_pipeline.py |
| Confidence scoring | 🔜 Week 4 | app/services/ai_pipeline.py |
| Response generation | 🔜 Week 5 | app/services/ai_pipeline.py |

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

## 8. Build Progress

- [x] FastAPI app scaffold with lifespan management
- [x] Kafka consumer — reads from tradepilot.messages.inbound
- [x] Kafka producer — publishes to tradepilot.ai.results
- [x] Pydantic models for InboundMessageEvent and AiProcessingResult
- [x] Health endpoint
- [ ] Commodity entity extraction prompt
- [ ] Intent classifier (8 classes)
- [ ] Confidence scoring
- [ ] Anthropic API integration