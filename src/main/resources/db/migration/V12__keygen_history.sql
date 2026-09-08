CREATE TABLE keygen_history (
    id BIGSERIAL PRIMARY KEY,
    key_type VARCHAR(50) NOT NULL,
    input_data TEXT NOT NULL,
    output_data TEXT NOT NULL,
    generated_by BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    organization_id VARCHAR(255),
    product_item_id BIGINT REFERENCES product_items(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_keygen_history_key_type ON keygen_history(key_type);
CREATE INDEX idx_keygen_history_generated_by ON keygen_history(generated_by);
CREATE INDEX idx_keygen_history_product_item ON keygen_history(product_item_id);
CREATE INDEX idx_keygen_history_created_at ON keygen_history(created_at DESC);
