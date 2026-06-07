CREATE TABLE follow_up_jobs (
                                id                  BIGSERIAL PRIMARY KEY,
                                job_type            VARCHAR(50)  NOT NULL,
                                order_id            BIGINT REFERENCES orders(id),
                                trade_contact_id    BIGINT NOT NULL REFERENCES trade_contacts(id),
                                scheduled_at        TIMESTAMP NOT NULL,
                                executed_at         TIMESTAMP,
                                status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                                attempt_count       INTEGER DEFAULT 0,
                                message_template    VARCHAR(100),
                                context_payload     JSONB,
                                created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- job_type values: INQUIRY_FOLLOWUP, PAYMENT_REMINDER, DELIVERY_UPDATE, RELATIONSHIP
-- status values: PENDING, SENT, FAILED, CANCELLED

CREATE INDEX idx_follow_up_jobs_status ON follow_up_jobs(status);
CREATE INDEX idx_follow_up_jobs_scheduled ON follow_up_jobs(scheduled_at);
CREATE INDEX idx_follow_up_jobs_contact ON follow_up_jobs(trade_contact_id);