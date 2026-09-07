CREATE TABLE IF NOT EXISTS business_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    label_vi VARCHAR(255),
    label_en VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_business_fields_name ON business_fields(name);

CREATE TABLE IF NOT EXISTS campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    product_id VARCHAR(64),
    business_field_id VARCHAR(64),
    organization_id VARCHAR(64) NOT NULL,
    assigned_person_id VARCHAR(64),
    funnel_type TEXT NOT NULL CHECK (funnel_type IN ('sales','course')),
    channel_type TEXT,
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','ended')),
    sla_hours INT,
    created_by VARCHAR(64) NOT NULL,
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_campaigns_org ON campaigns(organization_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_campaigns_assigned_person_id ON campaigns(assigned_person_id);

INSERT INTO permissions (code, description) VALUES
    ('campaign.read', 'View campaigns'),
    ('campaign.create', 'Create new campaigns'),
    ('campaign.update', 'Update campaign details'),
    ('campaign.end', 'End a campaign')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('campaign.read', 'campaign.create', 'campaign.update', 'campaign.end')
WHERE r.code IN ('ADMIN', 'STAFF', 'MANAGER')
ON CONFLICT (role_id, permission_id) DO NOTHING;
