CREATE TABLE IF NOT EXISTS work_order_comments (
    id VARCHAR(64) PRIMARY KEY,
    work_order_id VARCHAR(64) NOT NULL,
    author_id VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_work_order_comments_thread ON work_order_comments(work_order_id, created_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_work_order_comments_author ON work_order_comments(author_id) WHERE deleted_at IS NULL;
