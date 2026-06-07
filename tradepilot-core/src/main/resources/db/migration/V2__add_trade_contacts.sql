CREATE TABLE trade_contacts (
                                id                  BIGSERIAL PRIMARY KEY,
                                whatsapp_number     VARCHAR(20)  NOT NULL UNIQUE,
                                display_name        VARCHAR(255),
                                business_name       VARCHAR(255),
                                contact_type        VARCHAR(50)  NOT NULL DEFAULT 'BUYER',
                                city                VARCHAR(100),
                                commodity_interest  VARCHAR(255),
                                lifetime_value      NUMERIC(15,2) DEFAULT 0.00,
                                total_orders        INTEGER DEFAULT 0,
                                is_active           BOOLEAN DEFAULT TRUE,
                                created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trade_contacts_whatsapp ON trade_contacts(whatsapp_number);
CREATE INDEX idx_trade_contacts_type ON trade_contacts(contact_type);