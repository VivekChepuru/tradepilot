CREATE TABLE price_rules (
                             id                  BIGSERIAL PRIMARY KEY,
                             commodity           VARCHAR(255) NOT NULL,
                             grade               VARCHAR(100),
                             base_price          NUMERIC(15,2) NOT NULL,
                             margin_percent      NUMERIC(5,2)  NOT NULL DEFAULT 0.00,
                             gst_percent         NUMERIC(5,2)  NOT NULL DEFAULT 18.00,
                             freight_per_unit    NUMERIC(10,2) DEFAULT 0.00,
                             unit                VARCHAR(20)   NOT NULL DEFAULT 'MT',
                             effective_from      DATE NOT NULL DEFAULT CURRENT_DATE,
                             effective_to        DATE,
                             is_active           BOOLEAN DEFAULT TRUE,
                             last_updated_by     VARCHAR(100),
                             created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_price_rules_commodity ON price_rules(commodity);
CREATE INDEX idx_price_rules_active ON price_rules(is_active);
CREATE INDEX idx_price_rules_effective ON price_rules(effective_from, effective_to);