-- ─────────────────────────────────────────
-- 1. DISTRIBUTORS
-- ─────────────────────────────────────────
CREATE TABLE distributors (
    id           BIGSERIAL     PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL UNIQUE,
    contact_name VARCHAR(100),
    phone        VARCHAR(20),
    city         VARCHAR(100),
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_distributors_name      ON distributors(name);
CREATE INDEX idx_distributors_is_active ON distributors(is_active);

-- Seed two common distributors for pilot
INSERT INTO distributors (name, contact_name, city) VALUES
    ('JSW Steel',  'JSW Rep',  'Mumbai'),
    ('Tata Steel', 'Tata Rep', 'Mumbai');

-- ─────────────────────────────────────────
-- 2. ADD DISTRIBUTOR TO PRICE RULES
-- ─────────────────────────────────────────
ALTER TABLE price_rules
    ADD COLUMN IF NOT EXISTS distributor_id BIGINT REFERENCES distributors(id),
    ADD COLUMN IF NOT EXISTS distributor_name VARCHAR(100);

CREATE INDEX idx_price_rules_distributor ON price_rules(distributor_id);

-- Remove duplicate TMT Fe500D (keep id=1, remove id=6)
DELETE FROM price_rules WHERE id = 6;

-- ─────────────────────────────────────────
-- 3. NEGOTIATION SETTINGS (GLOBAL DEFAULT)
-- ─────────────────────────────────────────
CREATE TABLE negotiation_settings (
    id                          BIGSERIAL    PRIMARY KEY,
    max_auto_discount_percent   NUMERIC(5,2) NOT NULL DEFAULT 2.00,
    max_escalate_discount_percent NUMERIC(5,2) NOT NULL DEFAULT 5.00,
    is_negotiation_enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at                  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by                  VARCHAR(100)
);

-- Seed one global default row
INSERT INTO negotiation_settings
    (max_auto_discount_percent, max_escalate_discount_percent, is_negotiation_enabled)
VALUES (2.00, 5.00, TRUE);

-- ─────────────────────────────────────────
-- 4. NEGOTIATION OVERRIDES (PER-COMMODITY)
-- ─────────────────────────────────────────
CREATE TABLE negotiation_overrides (
    id                          BIGSERIAL    PRIMARY KEY,
    commodity                   VARCHAR(100) NOT NULL UNIQUE,
    max_auto_discount_percent   NUMERIC(5,2) NOT NULL,
    max_escalate_discount_percent NUMERIC(5,2) NOT NULL,
    is_negotiation_enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by                  VARCHAR(100)
);

CREATE INDEX idx_negotiation_overrides_commodity ON negotiation_overrides(commodity);