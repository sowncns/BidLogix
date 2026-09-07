CREATE TABLE IF NOT EXISTS customer_views (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(64) NOT NULL,
    sales_id VARCHAR(64) NOT NULL,
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT customer_views_customer_sales_unique UNIQUE (customer_id, sales_id)
);

CREATE INDEX IF NOT EXISTS idx_customer_views_sales_id ON customer_views(sales_id);
