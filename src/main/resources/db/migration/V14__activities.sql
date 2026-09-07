CREATE TABLE IF NOT EXISTS activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activityable_type VARCHAR(20) NOT NULL CHECK (activityable_type IN ('customer')),
    activityable_id VARCHAR(64) NOT NULL,
    sales_id VARCHAR(64) NOT NULL,
    action VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    contact_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_activities_activityable ON activities(activityable_type, activityable_id);
CREATE INDEX IF NOT EXISTS idx_activities_sales_id ON activities(sales_id);
CREATE INDEX IF NOT EXISTS idx_activities_created_at ON activities(created_at DESC);
