CREATE TABLE IF NOT EXISTS service_logs (
    id VARCHAR(64) PRIMARY KEY,
    product_item_id VARCHAR(64),
    work_order_id VARCHAR(64),
    service_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    service_type VARCHAR(50) NOT NULL,
    issue_description TEXT,
    action_taken TEXT,
    parts_replaced JSONB NOT NULL DEFAULT '[]',
    technician_id VARCHAR(64),
    customer_notes TEXT,
    internal_notes TEXT,
    source_comment_id VARCHAR(64),
    event_type VARCHAR(64),
    event_metadata JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_service_logs_product_item ON service_logs(product_item_id);
CREATE INDEX IF NOT EXISTS idx_service_logs_work_order ON service_logs(work_order_id);
CREATE INDEX IF NOT EXISTS idx_service_logs_technician ON service_logs(technician_id);
CREATE INDEX IF NOT EXISTS idx_service_logs_date ON service_logs(service_date);
CREATE INDEX IF NOT EXISTS idx_service_logs_event_type ON service_logs(event_type);
