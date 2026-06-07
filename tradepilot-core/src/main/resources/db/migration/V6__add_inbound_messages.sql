CREATE TABLE inbound_messages (
                                  id                  BIGSERIAL PRIMARY KEY,
                                  whatsapp_message_id VARCHAR(255) NOT NULL UNIQUE,
                                  from_number         VARCHAR(20)  NOT NULL,
                                  trade_contact_id    BIGINT REFERENCES trade_contacts(id),
                                  message_type        VARCHAR(20)  NOT NULL DEFAULT 'TEXT',
                                  raw_content         TEXT,
                                  media_url           VARCHAR(500),
                                  detected_intent     VARCHAR(50),
                                  confidence_score    NUMERIC(4,3),
                                  extracted_entities  JSONB,
                                  ai_suggested_reply  TEXT,
                                  routing_decision    VARCHAR(20),
                                  processed_at        TIMESTAMP,
                                  received_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

-- message_type: TEXT, IMAGE, VOICE, DOCUMENT
-- routing_decision: AUTO_SENT, PENDING_APPROVAL, ESCALATED

CREATE INDEX idx_inbound_messages_from ON inbound_messages(from_number);
CREATE INDEX idx_inbound_messages_intent ON inbound_messages(detected_intent);
CREATE INDEX idx_inbound_messages_routing ON inbound_messages(routing_decision);
CREATE INDEX idx_inbound_messages_received ON inbound_messages(received_at);