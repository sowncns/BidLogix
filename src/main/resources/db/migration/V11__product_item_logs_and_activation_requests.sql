CREATE TABLE product_item_logs (
    id BIGSERIAL PRIMARY KEY,
    product_item_id BIGINT NOT NULL REFERENCES product_items(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    actor_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    actor_role VARCHAR(50),
    source VARCHAR(50) NOT NULL DEFAULT 'system',
    customer_id VARCHAR(255) REFERENCES customers(id) ON DELETE SET NULL,
    metadata TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_item_logs_item_time ON product_item_logs(product_item_id, occurred_at DESC);

CREATE TABLE activation_requests (
    id BIGSERIAL PRIMARY KEY,
    request_type VARCHAR(50) NOT NULL DEFAULT 'activation',
    sales_user_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    customer_id VARCHAR(255) NOT NULL REFERENCES customers(id),
    organization_id VARCHAR(255),
    inputs TEXT NOT NULL,
    warranty_expiry TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    result TEXT,
    reject_reason TEXT,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_by BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activation_requests_status ON activation_requests(status);
CREATE INDEX idx_activation_requests_sales_user ON activation_requests(sales_user_id);
