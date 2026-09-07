ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS customer_type VARCHAR(20) NOT NULL DEFAULT 'official',
    ADD COLUMN IF NOT EXISTS source_campaign_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS notes TEXT;

CREATE INDEX IF NOT EXISTS idx_customers_customer_type ON customers(customer_type) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_customers_campaign_phone
    ON customers (source_campaign_id, phone) WHERE source_campaign_id IS NOT NULL;
