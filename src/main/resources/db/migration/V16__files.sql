CREATE TABLE IF NOT EXISTS files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_name TEXT NOT NULL,
    storage_key TEXT NOT NULL UNIQUE,
    mime_type TEXT NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes >= 0),
    visibility VARCHAR(32) NOT NULL CHECK (visibility IN ('internal', 'customer_visible')),
    uploaded_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_files_uploaded_by ON files(uploaded_by) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_files_visibility ON files(visibility) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS file_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id UUID NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    purpose VARCHAR(64) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT file_links_entity_purpose_check CHECK (entity_type <> '' AND purpose <> ''),
    CONSTRAINT file_links_unique_entity_file_purpose UNIQUE (file_id, entity_type, entity_id, purpose)
);

CREATE INDEX IF NOT EXISTS idx_file_links_entity ON file_links(entity_type, entity_id, purpose);
CREATE INDEX IF NOT EXISTS idx_file_links_file_id ON file_links(file_id);
