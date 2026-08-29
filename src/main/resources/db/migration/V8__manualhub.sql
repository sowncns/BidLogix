CREATE TABLE manualhub_documents (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES manualhub_documents(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    document_type VARCHAR(50),
    format VARCHAR(20),
    content JSONB,
    status VARCHAR(30) NOT NULL DEFAULT 'draft',
    language VARCHAR(10) NOT NULL DEFAULT 'vi',
    version VARCHAR(50) NOT NULL DEFAULT '1',
    author_id BIGINT,
    author_name VARCHAR(255),
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    file_count INTEGER NOT NULL DEFAULT 0,
    submitted_at TIMESTAMPTZ,
    released_at TIMESTAMPTZ,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_manualhub_documents_product_id ON manualhub_documents(product_id);
CREATE INDEX idx_manualhub_documents_parent_id ON manualhub_documents(parent_id);
CREATE INDEX idx_manualhub_documents_status ON manualhub_documents(status);

CREATE TABLE manualhub_document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES manualhub_documents(id),
    version VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    content JSONB,
    file_url VARCHAR(500),
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_manualhub_versions_document_id ON manualhub_document_versions(document_id);

CREATE TABLE manualhub_files (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES manualhub_documents(id),
    storage_key VARCHAR(500) NOT NULL,
    original_name VARCHAR(255),
    content_type VARCHAR(150),
    size BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_manualhub_files_document_id ON manualhub_files(document_id);

CREATE TABLE manualhub_activities (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES manualhub_documents(id),
    actor_id BIGINT,
    actor_name VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_manualhub_activities_document_id ON manualhub_activities(document_id);
