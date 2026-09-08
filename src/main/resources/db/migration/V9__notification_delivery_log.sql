CREATE TABLE IF NOT EXISTS notification_delivery_log (
    id UUID PRIMARY KEY,
    user_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    channel TEXT NOT NULL DEFAULT 'email' CHECK (channel IN ('email','zalo','whatsapp')),
    provider TEXT NOT NULL,
    recipient TEXT NOT NULL,
    template TEXT,
    status TEXT NOT NULL CHECK (status IN ('sent','failed')),
    error TEXT,
    provider_message_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_delivery_log_user_created ON notification_delivery_log(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_delivery_log_status_created ON notification_delivery_log(status, created_at DESC);
