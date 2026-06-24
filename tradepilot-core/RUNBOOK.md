# TradePilot Core — Developer Runbook

Everything you need to run, test, and understand this project.
Update this file every time a new service, tool, or pattern is added.

---

## 1. Starting the Stack

Always start in this order:

### Step 1 — Start Docker (Kafka + Zookeeper + Kafka UI)
```powershell
cd C:\Users\vivek\Files\IdeaProjects\TradePilot_Core
docker compose up -d
docker compose ps   # verify all 3 containers show Up
```

### Step 2 — Start Spring Boot (TradePilot Core)
- Open IntelliJ
- Run `TradePilotApplication` with profile `local`
- Confirm startup in under 10 seconds with no errors

### Step 3 — Start Python AI Service
```powershell
cd C:\Users\vivek\Files\IdeaProjects\tradepilot-ai-service
venv\Scripts\activate
python -m app.main
```

---

## 2. Verify Everything is Running

| What | URL | Expected |
|---|---|---|
| Spring Boot health | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| AI Service health | http://localhost:8001/health | `{"status":"ok"}` |
| Kafka UI | http://localhost:9090 | Dashboard with tradepilot-local cluster |
| FastAPI docs | http://localhost:8001/docs | Auto-generated Swagger UI |

---

## 3. Services and Ports

| Service | Port | Notes |
|---|---|---|
| Spring Boot (TradePilot Core) | 8080 | Main backend |
| FastAPI (AI Service) | 8001 | AI processing pipeline |
| Kafka broker | 9092 | Internal only |
| Kafka UI | 9090 | Browser dashboard |
| Zookeeper | 2181 | Internal only |
| PostgreSQL | 5432 | DB: messengerdb |

---

## 4. Kafka Topics

| Topic | Partitions | Purpose |
|---|---|---|
| tradepilot.messages.inbound | 3 | Raw inbound WhatsApp messages from Spring Boot |
| tradepilot.messages.outbound | 3 | Outbound messages to send via WhatsApp API |
| tradepilot.ai.results | 3 | AI processing results from Python service |
| tradepilot.follow.up.jobs | 1 | Scheduled follow-up and payment reminder jobs |

**View messages:** Kafka UI → Topics → [topic name] → Messages tab

---

## 5. Database Tables

| Table | Purpose | Migration |
|---|---|---|
| users | Operator accounts | Pre-Flyway (Hibernate) |
| chats | Conversation threads | Pre-Flyway (Hibernate) |
| messages | Raw messages | Pre-Flyway (Hibernate) |
| trade_contacts | Buyer/supplier profiles | V2 |
| orders | Order lifecycle | V3 |
| price_rules | Commodity pricing config | V4 |
| follow_up_jobs | Scheduled automation jobs | V5 |
| inbound_messages | WhatsApp inbound log | V6 |
| flyway_schema_history | Flyway migration tracker | Auto-created |

**Rule:** never edit a migration file after it has run. Always create a new V{n} file.

---

## 6. Testing the Webhook

### Verification handshake (Meta calls this once)
```
GET http://localhost:8080/webhook
  ?hub.mode=subscribe
  &hub.verify_token=tradepilot_webhook_secret_dev
  &hub.challenge=test_challenge_123
```
Expected: `200 OK` body `test_challenge_123`

### Simulate inbound WhatsApp message
```
POST http://localhost:8080/webhook
Content-Type: application/json

{
  "object": "whatsapp_business_account",
  "entry": [{
    "id": "ENTRY_ID",
    "changes": [{
      "field": "messages",
      "value": {
        "messaging_product": "whatsapp",
        "metadata": {
          "display_phone_number": "15550001234",
          "phone_number_id": "TEST_PHONE_ID"
        },
        "contacts": [{
          "profile": { "name": "Rajesh Steel" },
          "wa_id": "919876543210"
        }],
        "messages": [{
          "id": "wamid.test001",
          "from": "919876543210",
          "timestamp": "1700000000",
          "type": "text",
          "text": { "body": "Bhai TMT Fe500D ka rate kya hai aaj?" }
        }]
      }
    }]
  }]
}
```
Expected: `200 OK` body `EVENT_RECEIVED`

After sending: check Kafka UI → `tradepilot.messages.inbound` → Messages.
Then check `tradepilot.ai.results` — AI service should publish a stub result.

---

## 7. Config Files

| File | Location | Committed? | Purpose |
|---|---|---|---|
| application.yml | TradePilot_Core/src/main/resources/ | Yes | All non-secret config |
| application-local.yml | TradePilot_Core/src/main/resources/ | No (gitignored) | DB credentials |
| .env | tradepilot-ai-service/ | No (gitignored) | AI service secrets |
| .env.example | tradepilot-ai-service/ | Yes | Template for .env |
| docker-compose.yml | TradePilot_Core/ | Yes | Kafka + Zookeeper + UI |

---

## 8. Build Progress (14-Week Plan)

- [x] Week 1 — Project restructure, domain naming, package skeleton
- [x] Week 2 — Kafka setup, Flyway migrations, WhatsApp webhook receiver
- [x] Week 3 — Python FastAPI AI service scaffold, Kafka consumer/producer
- [ ] Week 3-5 — Commodity entity extraction, intent classification
- [ ] Week 5-7 — Price engine, decision routing, auto-quote
- [ ] Week 7-11 — Order lifecycle, follow-up scheduler, Next.js dashboard
- [ ] Week 11-14 — Negotiation intelligence, RAG, weekly digest

---

## 9. Meta WhatsApp API Status

- Meta Business Manager: created
- App created as: TradePilot Dev (type: Other, no business verification needed)
- WhatsApp Cloud API: access pending (rate limited on new account)
- Verify token: `tradepilot_webhook_secret_dev`
- Credentials (phone_number_id, access_token): fill in application.yml when unblocked
- ngrok needed to expose local webhook to Meta during development


- [x] Week 6 — Decision routing engine, WhatsApp sender (simulation mode)

## 10. Outbound Message Routing

| routingDecision | Action |
|---|---|
| PRICE_QUOTED | Auto-send via WhatsAppSenderService |
| PENDING_APPROVAL | Logged — queued for operator (dashboard in Week 9) |
| ESCALATED | Logged — flagged for human handling |

## 11. Switching to Real WhatsApp API

When Meta credentials are ready:
1. Fill phone-number-id and access-token in application-local.yml
2. Set tradepilot.whatsapp.simulation-mode: false in application.yml
3. Implement RealWhatsAppSenderService.send() in
   com.tradepilot.core.channel.RealWhatsAppSenderService
4. No other changes needed — interface and routing logic unchanged

- [x] Week 7 — Order state machine, TradeContact management

## 12. Order Lifecycle

Status transitions:
INQUIRY → QUOTED → NEGOTIATING → CONFIRMED → DISPATCHED → DELIVERED
Any non-terminal status → CANCELLED

Auto-created when: price quote sent with routingDecision=PRICE_QUOTED
Check orders: SELECT * FROM orders ORDER BY created_at DESC;
Check contacts: SELECT * FROM trade_contacts;

## 13. Follow-Up Scheduler

Jobs are created automatically when a new order reaches QUOTED status.

**Schedule:**
| Template | Fires at | Message |
|---|---|---|
| INQUIRY_FOLLOWUP_1 | T+90 minutes | Soft check-in with price |
| INQUIRY_FOLLOWUP_2 | T+4 hours | Follow-up nudge |
| INQUIRY_FOLLOWUP_3 | T+24 hours | Final follow-up |
| PAYMENT_REMINDER_DUE | Due date | Polite payment reminder |
| PAYMENT_REMINDER_3D | +3 days | Gentle reminder |
| PAYMENT_REMINDER_7D | +7 days | Firm reminder |
| PAYMENT_REMINDER_15D | +15 days | Urgent reminder |

**Processor:** runs every 60 seconds via @Scheduled
**To test immediately:**
```sql
UPDATE follow_up_jobs
SET scheduled_at = NOW() - INTERVAL '5 minutes'
WHERE id = {job_id};
```

**Check job status:**
```sql
SELECT id, job_type, message_template, scheduled_at,
       status, executed_at, attempt_count
FROM follow_up_jobs
ORDER BY scheduled_at ASC;
```

**Failure handling:** attempt_count increments on each failure.
After 3 failed attempts status moves to FAILED automatically.

---

## 14. Negotiation Engine

Activated when AI classifies intent as negotiation_counter.
Configured in application.yml under tradepilot.negotiation.

**Thresholds:**
| Discount Requested | Action | Routing Decision |
|---|---|---|
| <= 2% | Auto-approve, apply discount, send price | PRICE_QUOTED |
| 3–5% | Queue for operator review, no reply sent | PENDING_APPROVAL |
| > 5% | Reject politely, escalate to human | ESCALATED |

**Config (application.yml):**
```yaml
tradepilot:
  negotiation:
    max-auto-discount-percent: 2.0
    max-escalate-discount-percent: 5.0
```

**Key files:**
- `NegotiationProperties.java` — binds config values
- `NegotiationService.java` — 3-tier routing logic
- `AiResultsConsumer.java` — routes negotiation_counter here before price engine

**AI extraction:** discountPercent is extracted from the message by Phi-3 Mini
and placed in extractedEntities. NegotiationService reads it via safeDouble helper.

**Test scenarios verified (2026-06-23):**
- 2% discount → PRICE_QUOTED, ₹71056.15/MT (from base ₹72506.28)
- 4% discount → PENDING_APPROVAL, queued for operator
- 10% discount → ESCALATED, polite rejection reply sent

---

## 15. Pipeline Test Results (All Scenarios Verified 2026-06-21)

All 9 core pipeline scenarios passing. English-only testing scope
(Hindi/regional deferred until Meta credentials activated and better model available).

| # | Scenario | Status |
|---|---|---|
| 1 | TMT Fe500D Hindi price inquiry | ✅ PASS |
| 2 | MS Angle multi-word prefix | ✅ PASS |
| 3 | Unknown commodity (Copper wire) escalation | ✅ PASS |
| 4 | Order confirmation (bulk_order intent) | ✅ PASS |
| 5 | Negotiation / discount request routing | ✅ PASS |
| 6 | Generic greeting (relationship_message) | ✅ PASS |
| 7 | New WhatsApp contact auto-creation | ✅ PASS |
| 8 | Duplicate contact prevention | ✅ PASS |
| 9 | Malformed payload graceful handling | ✅ PASS |

**Known model quirks (Phi-3 Mini):**
- confidence often returns 0.0 → Python fallback assigns intent-based default
- unit field sometimes returns prompt template text instead of extracted value
- Ollama occasionally returns JSON list instead of object → isinstance guard handles it
- MIN_CONFIDENCE threshold lowered to 0.10 in AiResultsConsumer.java

---

## 16. Build Progress (Updated 2026-06-23)

- [x] Week 1 — Project restructure, domain naming, package skeleton
- [x] Week 2 — Kafka setup, Flyway migrations, WhatsApp webhook receiver
- [x] Week 3 — Python FastAPI AI service scaffold, Kafka consumer/producer
- [x] Week 4 — Real AI extraction and intent classification (Ollama + Phi-3 Mini)
- [x] Week 5 — Price engine, quote calculation, outbound pipeline
- [x] Week 6 — Decision routing engine, WhatsApp sender (simulation mode)
- [x] Week 7 — Order state machine, TradeContact management
- [x] Week 8 — Follow-up scheduler, Kafka-driven jobs, negotiation engine (3-tier)
- [x] Week 8 remaining — Payment reminders verified end-to-end
- [ ] Week 8 final — Invoice generation (text-based, PDF deferred to post-Meta setup)
- [ ] Week 9 — Next.js operator dashboard (inbox, pipeline, approvals)
- [ ] Week 10 — AWS deployment, live pilot onboarding
- [ ] Week 11 — pgvector customer history RAG
- [ ] Week 12 — Negotiation intelligence, weekly digest
- [ ] Week 13 — Multi-user / team support
- [ ] Week 14 — First paying customer

## 17. Payment Reminders

Triggered automatically when an order transitions to CONFIRMED status.
Scheduled via FollowUpSchedulerService.schedulePaymentReminders().

**4 reminder tiers:**
| Template | Fires at | Tone |
|---|---|---|
| PAYMENT_REMINDER_DUE | Order confirmed (T+0) | Polite — payment due today |
| PAYMENT_REMINDER_3D | T+3 days | Gentle — 3 days overdue |
| PAYMENT_REMINDER_7D | T+7 days | Important — 7 days overdue |
| PAYMENT_REMINDER_15D | T+15 days | Urgent — 15 days overdue |

**Trigger flow:**
1. OrderService.transitionStatus() called with newStatus=CONFIRMED
2. cancelPendingFollowUps(orderId) — cancels remaining INQUIRY_FOLLOWUP jobs
3. schedulePaymentReminders(order, contact) — inserts 4 PAYMENT_REMINDER jobs
4. FollowUpJobProcessor picks them up at scheduled time (runs every 60s)
5. FollowUpMessageBuilder resolves template with context (name, ref, commodity, price)
6. WhatsAppSenderService sends the message (simulation mode until Meta activated)

**Context payload fields used by templates:**
- {name} — contact displayName or whatsappNumber fallback
- {ref} — orderReference (e.g. TP-1718900000000)
- {commodity}, {grade}, {price} — from order at time of scheduling

**To test immediately (force a job to fire):**
```sql
UPDATE follow_up_jobs
SET scheduled_at = NOW() - INTERVAL '5 minutes'
WHERE message_template = 'PAYMENT_REMINDER_DUE'
AND status = 'PENDING'
LIMIT 1;
```

**To manually insert a test payment reminder job:**
```sql
INSERT INTO follow_up_jobs
(job_type, order_id, trade_contact_id, scheduled_at, status, attempt_count, message_template, context_payload, created_at)
SELECT
    'PAYMENT_REMINDER', o.id, o.trade_contact_id,
    NOW() - INTERVAL '5 minutes', 'PENDING', 0, 'PAYMENT_REMINDER_DUE',
    json_build_object(
        'orderReference', o.order_reference,
        'toNumber', tc.whatsapp_number,
        'displayName', tc.display_name,
        'commodity', o.commodity,
        'grade', COALESCE(o.grade, ''),
        'quotedPrice', o.quoted_price::text
    )::jsonb, NOW()
FROM orders o
JOIN trade_contacts tc ON tc.id = o.trade_contact_id
WHERE o.status = 'QUOTED' LIMIT 1;
```

**Verified:** 2026-06-23 — PAYMENT_REMINDER_DUE fired and delivered via simulated send.