ALTER TABLE products
    ADD COLUMN IF NOT EXISTS code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS specifications JSONB NOT NULL DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS warranty_months INTEGER NOT NULL DEFAULT 24,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

UPDATE products SET code = sku WHERE code IS NULL;

ALTER TABLE products ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_products_code ON products(code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_products_deleted_at ON products(deleted_at);
