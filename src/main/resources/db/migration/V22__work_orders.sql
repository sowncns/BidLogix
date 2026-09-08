CREATE TABLE IF NOT EXISTS work_orders (
    id VARCHAR(64) PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    product_item_id VARCHAR(64),
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'normal',
    assigned_to VARCHAR(64),
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    scheduled_date TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_by VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_work_orders_number ON work_orders(order_number);
CREATE INDEX IF NOT EXISTS idx_work_orders_product_item ON work_orders(product_item_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_work_orders_assigned ON work_orders(assigned_to) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_work_orders_status ON work_orders(status) WHERE deleted_at IS NULL;
