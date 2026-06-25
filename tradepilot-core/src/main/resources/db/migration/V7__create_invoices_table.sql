CREATE TABLE invoices (
    id                BIGSERIAL      PRIMARY KEY,
    invoice_number    VARCHAR(50)    NOT NULL UNIQUE,
    order_id          BIGINT         NOT NULL REFERENCES orders(id),
    trade_contact_id  BIGINT         NOT NULL REFERENCES trade_contacts(id),
    subtotal          NUMERIC(15,2)  NOT NULL,
    gst_rate          NUMERIC(5,2)   NOT NULL DEFAULT 18.00,
    gst_amount        NUMERIC(15,2)  NOT NULL,
    total_amount      NUMERIC(15,2)  NOT NULL,
    quantity          NUMERIC(10,3),
    unit              VARCHAR(20),
    commodity         VARCHAR(100),
    grade             VARCHAR(50),
    payment_terms     VARCHAR(20)    NOT NULL DEFAULT 'POST_DELIVERY',
    status            VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    invoice_text      TEXT,
    issued_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    due_date          TIMESTAMP      NOT NULL,
    sent_at           TIMESTAMP,
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_order_id       ON invoices(order_id);
CREATE INDEX idx_invoices_trade_contact  ON invoices(trade_contact_id);
CREATE INDEX idx_invoices_status         ON invoices(status);
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);