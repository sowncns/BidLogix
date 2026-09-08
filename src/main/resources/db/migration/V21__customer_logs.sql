CREATE TABLE IF NOT EXISTS customer_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(64) NOT NULL,
    actor_id VARCHAR(64),
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(32) NOT NULL DEFAULT 'customer',
    target_id VARCHAR(64),
    changes JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customer_logs_customer_id_created_at ON customer_logs(customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_customer_logs_actor_id ON customer_logs(actor_id);
