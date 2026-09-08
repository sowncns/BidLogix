CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT REFERENCES organizations(id),
    path TEXT NOT NULL,
    level INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_organizations_path ON organizations(path);
CREATE INDEX IF NOT EXISTS idx_organizations_parent_id ON organizations(parent_id);
