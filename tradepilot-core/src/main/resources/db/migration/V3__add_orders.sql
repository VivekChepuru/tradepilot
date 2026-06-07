CREATE TABLE orders (
                        id                  BIGSERIAL PRIMARY KEY,
                        order_reference     VARCHAR(50) NOT NULL UNIQUE,
                        trade_contact_id    BIGINT NOT NULL REFERENCES trade_contacts(id),
                        whatsapp_thread_id  VARCHAR(255),
                        commodity           VARCHAR(255) NOT NULL,
                        grade               VARCHAR(100),
                        quantity            NUMERIC(10,3),
                        unit                VARCHAR(20),
                        quoted_price        NUMERIC(15,2),
                        final_price         NUMERIC(15,2),
                        total_amount        NUMERIC(15,2),
                        status              VARCHAR(50) NOT NULL DEFAULT 'INQUIRY',
                        payment_status      VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                        payment_terms       VARCHAR(100),
                        delivery_terms      VARCHAR(100),
                        notes               TEXT,
                        created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- status values: INQUIRY, QUOTED, NEGOTIATING, CONFIRMED, DISPATCHED, DELIVERED, CANCELLED
-- payment_status values: PENDING, PARTIAL, PAID, OVERDUE

CREATE INDEX idx_orders_contact ON orders(trade_contact_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);