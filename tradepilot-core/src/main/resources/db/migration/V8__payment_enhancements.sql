-- Add payment_terms to orders table
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(20) NOT NULL DEFAULT 'POST_DELIVERY';

-- Add WAIVED to payment status (existing PENDING, PARTIAL, PAID, OVERDUE already exist)
-- No enum change needed in Postgres — stored as VARCHAR

-- Create payment_overdue_flags table
CREATE TABLE payment_overdue_flags (
    id                      BIGSERIAL    PRIMARY KEY,
    order_id                BIGINT       NOT NULL REFERENCES orders(id),
    trade_contact_id        BIGINT       NOT NULL REFERENCES trade_contacts(id),
    flagged_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
    resolved_at             TIMESTAMP,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    last_manual_reminder_at TIMESTAMP,
    last_reminder_tone      VARCHAR(20),
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_overdue_flags_order_id      ON payment_overdue_flags(order_id);
CREATE INDEX idx_overdue_flags_status        ON payment_overdue_flags(status);
CREATE INDEX idx_overdue_flags_trade_contact ON payment_overdue_flags(trade_contact_id);