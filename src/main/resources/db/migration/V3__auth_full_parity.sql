-- accounts: extend with contact info, region, logical ref_id, verification timestamps
ALTER TABLE accounts
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN phone VARCHAR(32),
    ADD COLUMN region VARCHAR(16) NOT NULL DEFAULT 'VN',
    ADD COLUMN ref_id BIGINT,
    ADD COLUMN email_verified_at TIMESTAMPTZ,
    ADD COLUMN phone_verified_at TIMESTAMPTZ,
    ADD COLUMN email_notify_activation_request BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN email_notify_service_request BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN updated_at TIMESTAMPTZ;

CREATE UNIQUE INDEX idx_accounts_email ON accounts(email) WHERE email IS NOT NULL;
CREATE INDEX idx_accounts_ref_id ON accounts(ref_id);

-- roles: add stable code + data scope for RBAC
ALTER TABLE roles
    ADD COLUMN code VARCHAR(50),
    ADD COLUMN data_scope VARCHAR(30) NOT NULL DEFAULT 'SELF';

UPDATE roles SET code = name WHERE code IS NULL;

ALTER TABLE roles
    ALTER COLUMN code SET NOT NULL,
    ADD CONSTRAINT uq_roles_code UNIQUE (code);

INSERT INTO roles (name, code, data_scope) VALUES ('Customer', 'CUSTOMER', 'SELF')
    ON CONFLICT (code) DO NOTHING;

-- sso_tickets
CREATE TABLE sso_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    used_at TIMESTAMPTZ
);

-- verification_codes (email/phone OTP for registration, etc.)
CREATE TABLE verification_codes (
    id BIGSERIAL PRIMARY KEY,
    target VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    account_id BIGINT REFERENCES accounts(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    consumed_at TIMESTAMPTZ
);

CREATE INDEX idx_verification_codes_lookup ON verification_codes(target, purpose, consumed_at, created_at);

-- account_organizations: logical link only, no FK to a business "organizations" table yet
CREATE TABLE account_organizations (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL
);

CREATE INDEX idx_account_organizations_account_id ON account_organizations(account_id);
