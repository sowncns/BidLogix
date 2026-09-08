CREATE TABLE product_items (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    customer_id VARCHAR(255) REFERENCES customers(id),
    manufacturing_date TIMESTAMPTZ,
    installation_date TIMESTAMPTZ,
    warranty_expiry TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL DEFAULT 'stock',
    activated_at TIMESTAMPTZ,
    activated_by BIGINT REFERENCES accounts(id),
    activation_notes TEXT,
    recalled_at TIMESTAMPTZ,
    recall_notes TEXT,
    refurbished_at TIMESTAMPTZ,
    refurbish_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_product_items_product_id ON product_items(product_id);
CREATE INDEX idx_product_items_customer_id ON product_items(customer_id);
CREATE INDEX idx_product_items_status ON product_items(status);
CREATE INDEX idx_product_items_created_at ON product_items(created_at);
