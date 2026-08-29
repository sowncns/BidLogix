# QS-CRM Go Domain Reference (for Java port)

This document inventories every business domain in the Go project
(`qs-crm-be`, hexagonal architecture: `internal/core/{domain}`,
`internal/adapters/http/{domain}`, `internal/adapters/postgres/*_repo.go`,
`migrations/*.sql`) so it can be ported to the Java project
(`com.qs.Backend`, structured per `docs/erp_module_structure.md`)
module-by-module. It is documentation only — no Java code is written here.

Each domain lists: purpose, entity fields (from `entity.go`), business
rules/invariants (from `errors.go` + service doc comments), backing Postgres
table(s) (cross-referenced against `migrations/*.sql`) with FK relationships,
HTTP endpoints (from `handler.go` / the central `router.go`), and the
recommended Java target package per the platform/modules classification rule:
cross-cutting infra reused identically by any ERP module → `platform.{domain}`;
business-specific → `modules.{module}.{domain}`.

Sections are grouped by target module. Domains confirmed against
`ls internal/core`: activationrequest, activity, audit, businessfield,
customer, customergroup, customerlog, customerstatus, customerview, delivery,
file, keygen, lead, manualhub, notification, organization, permission,
product, productitem, productitemlog, profile, role, servicelog,
verification, workorder, workordercomment (`shared` is the ID-only kernel,
skipped as instructed) — plus `internal/modules/system/{auth,user}`. Six
HTTP-only surfaces with no `internal/core` counterpart were also
investigated: `customerportal`, `metadata`, `customersupplementalproduct`,
`messaging`, `dashboard`, `content`.

---

## Platform

Cross-cutting infrastructure with no business semantics — reused identically
regardless of which ERP module (CRM, Inventory, Sales, ...) is asking.

### auth (`internal/modules/system/auth`)

- **Purpose**: identity, login/session/JWT lifecycle, password reset, email
  verification-gated registration, and SSO ticket handoff between first-party
  apps (qs-website ↔ ERP/portal).
- **Entities**:
  - `AuthUser`: ID, Username, PasswordHash, Email, Phone, Region (drives email
    locale VN→vi/else→en), RefID `*shared.ID` (logical FK to a business
    entity, e.g. `customers.id` — **no DB FK constraint**, app layer
    maintains integrity), IsActive, CreatedAt, UpdatedAt,
    EmailNotifications (`EmailNotificationPrefs{ActivationRequest, ServiceRequest}` bools).
  - `Session`: ID, UserID, RefreshTokenHash, AccessTokenJTI (JWT ID for
    revocation), DeviceInfo, IPAddress, ExpiresAt, CreatedAt, RevokedAt.
  - `PasswordResetToken`: ID, UserID, TokenHash, ExpiresAt, UsedAt, CreatedAt.
  - `SSOTicket`: ID, UserID, TokenHash, ExpiresAt, UsedAt, CreatedAt
    (short-lived, single-use).
  - `RegisterParams`, `TokenPair`, `LoginResult`, `AuthUserWithPermissions`
    (User + Roles + Permissions + DataScope + OrganizationIDs) are
    request/aggregate DTOs, not persisted.
- **Business rules**:
  - Credentials, session (revoked/expired), token (expired/invalid/revoked),
    and password (too short <8, no uppercase, no number, mismatch,
    same-as-old) are all distinct error families — map to distinct
    `errorCode`s in Java, not one generic 400.
  - Password reset and SSO tickets are both single-use (`UsedAt`) with
    expiry — same pattern, could share one Java abstraction.
  - Registration requires at least one role code (`RegisterParams.RoleCodes`).
  - `RegisterCustomer` creates an inactive user pending email verification;
    activation only happens via `VerifyContact`. Phone verification is
    dead/unsupported (comment: "Phone verification is no longer supported").
  - `SyncPortalEmail`/`SyncPortalRegion`/`CheckPortalEmailAvailable`,
    `GrantPortalAccess`/`RevokePortalAccess`/`SendPortalPasswordReset`: the
    auth service owns the customer-portal-account lifecycle keyed by
    `RefID` — portal access is "true" only when an active auth user is
    linked via `ref_id`.
  - `Region` must be a non-empty short code ≤16 chars.
- **Postgres tables**: `auth_users` (originally had `user_type` FK to
  `user_types`, dropped in `000101_replace_usertype_with_role.up.sql` in
  favor of `user_roles`; `email_verified_at`/`phone_verified_at` added in
  `000128`), `sessions` (FK `user_id`→`auth_users`), `password_reset_tokens`
  (FK `user_id`→`auth_users`), `sso_tickets` (FK `user_id`→`auth_users`).
  `user_types` table still exists but is now vestigial (no FK references it).
- **HTTP endpoints** (`internal/modules/system/auth/handler.go`):
  - `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`
  - `POST /auth/forgot-password`, `POST /auth/reset-password`
  - `POST /auth/customer-register`, `POST /auth/verify`,
    `POST /auth/resend-verification`
  - `POST /auth/sso` (exchange ticket), `POST /auth/sso/ticket` (issue,
    authenticated)
  - `GET /auth/me`, `POST /auth/logout`, `POST /auth/change-password`
    (authenticated)
- **Java target**: `platform.auth`. **Known violation** (see below):
  `auth`'s HTTP handler directly imports and calls `customer.Service` — do
  not repeat this in Java; that flow belongs behind a public port.

### user (`internal/modules/system/user`)

- **Purpose**: user-management read model + admin CRUD over `auth_users`,
  plus role/organization assignment endpoints. Distinct from `auth` (which
  owns credentials/sessions) — `user` is the "manage other users" surface.
- **Entity**: `User`: ID, Email, Phone, FullName, Region, CreatedAt,
  EmailNotifications (`ActivationRequest`, `ServiceRequest` bools — same
  shape as `auth.EmailNotificationPrefs`, kept in sync via a matching
  `auth_users` column per-feature).
- **Business rules**:
  - `ErrSelfDelete`: a user cannot delete their own account (checked via
    `currentUserID` param in `Delete`).
  - `ChangePassword` here is the **admin-driven** path — no old-password
    check (unlike `auth.ChangePassword`, which verifies the current
    password for self-service changes).
  - `Delete` is a soft delete (`is_active = false`), not a row delete.
- **Postgres tables**: same `auth_users` table as `auth` (this domain is a
  different read/write facet over it) plus junction tables it manages:
  `user_roles` (PK `user_id,role_id`, FK→`auth_users`,`roles`) and
  `user_organizations` (PK `user_id,organization_id`, FK→`auth_users`,`organizations`).
  Also owns `user_profiles` indirectly via the `profile` domain (see below).
- **HTTP endpoints**:
  - `GET /users`, `POST /users`, `GET /users/{id}`, `PATCH /users/{id}`,
    `DELETE /users/{id}`
  - `POST /users/{userId}/organizations`, `DELETE /users/{userId}/organizations/{orgId}`,
    `GET /users/{userId}/organizations`
  - `POST /users/{userId}/roles`, `DELETE /users/{userId}/roles/{roleId}`,
    `GET /users/{userId}/roles`
- **Java target**: `platform.auth` (as a management sub-feature of the same
  identity domain) — its own imports (`audit`, `permission`, `profile`) are
  all platform, so no boundary issue here.

### permission (`internal/core/permission`)

- **Purpose**: RBAC core — roles, permissions, role↔permission and
  user↔role↔organization assignments, and data-scope resolution.
- **Entities**: `Role` (ID, Code, Name, DataScope), `Permission` (ID, Code,
  Description), `UserRole` (UserID, RoleID), `RolePermission` (RoleID,
  PermissionID), `UserOrganization` (UserID, OrganizationID),
  `UserPermissionInfo` (aggregate: UserID, Roles, Permissions, effective
  DataScope, OrganizationIDs). `DataScope` enum: `SELF`, `ORGANIZATION`,
  `ORGANIZATION_AND_CHILDREN`, `ALL`.
- **Business rules**:
  - `GetEffectiveDataScope`: a user's *effective* scope is the most
    permissive across all their roles, ranked `ALL` > `ORGANIZATION_AND_CHILDREN`
    > `ORGANIZATION` > `SELF` — short-circuits on `ALL`.
  - `ErrCannotDeleteAdminRole` — the `admin` role is protected from deletion.
  - `ErrUserNotInOrganization` — a user must be assigned to at least one
    organization for org-scoped access to resolve.
  - Every assign/remove role or permission mutation writes an `audit` log
    entry (see `audit` domain) — this pairing should be preserved in Java
    (permission service calls audit service synchronously, not fire-and-forget).
- **Postgres tables**: `roles` (enum `data_scope`), `permissions`,
  `role_permissions` (PK `role_id,permission_id`), `user_roles` (PK
  `user_id,role_id`, FK→`auth_users`), `user_organizations` (PK
  `user_id,organization_id`, FK→`auth_users`,`organizations`).
- **HTTP endpoints**: no dedicated `permission` handler — surfaced through
  `role` handler and `user` role/org-assignment endpoints (see above).
- **Java target**: `platform.permission`. Imports only `audit` (platform) —
  clean.

### role (`internal/core/role`)

- **Purpose**: thin CRUD/service wrapper around `permission.Role` — role
  create/list/get/update/delete. Exists as a separate Go package purely as a
  use-case layer over the `permission` domain's entity; it defines no entity
  of its own (`entity.go` is empty).
- **Entity**: none — operates on `permission.Role`.
- **Business rules**: `ErrRoleNotFound` only; relies on `permission` package
  for the actual data-scope semantics.
- **Postgres table**: `roles` (same table as `permission`).
- **HTTP endpoints**: `POST /roles`, `GET /roles`, `GET /roles/{id}`,
  `PATCH /roles/{id}`, `DELETE /roles/{id}`.
- **Java target**: **merge into `platform.permission`** rather than creating
  a separate `platform.role` package — in Go this is already a redundant
  split (a `role` package that only re-exposes `permission.Role`); don't
  reproduce the split in Java. Expose role CRUD as part of the permission
  module's controller instead.

### audit (`internal/core/audit`)

- **Purpose**: append-only audit trail of RBAC changes (role/permission/org
  assignment and removal), independent of the CRM-specific `customerlog`
  domain (different table, different action vocabulary).
- **Entity**: `AuditLog`: ID, ActionType (`ASSIGN_ROLE`, `REMOVE_ROLE`,
  `ASSIGN_PERMISSION`, `REMOVE_PERMISSION`, `ASSIGN_ORG`, `REMOVE_ORG`),
  PerformedBy, TargetUserID/`TargetRoleID`/`TargetPermissionID`/`TargetOrganizationID`
  (all nullable — only the relevant one(s) are set per action type),
  Metadata (JSONB free-form context, e.g. role_code/permission_code),
  CreatedAt. `AuditLogParams` (write DTO) and `AuditLogFilters` (query DTO)
  mirror the entity's optional target fields.
- **Business rules**: `errors.go` is empty — this domain has no invariants
  of its own beyond `ActionType.IsValid()`; it's a pure log sink written to
  by `permission`.
- **Postgres table**: `rbac_audit_logs`. FKs: `performed_by`→`auth_users`,
  `target_user_id`→`auth_users`, `target_role_id`→`roles`,
  `target_permission_id`→`permissions`, `target_organization_id`→`organizations`.
- **HTTP endpoints**: `GET /audit/rbac` (List, filterable by `user_id`,
  `role_id`, `action_type`, `performed_by`), `GET /audit/rbac/{id}` (Get).
- **Java target**: `platform.audit` (could alternatively be a sub-feature of
  `platform.permission`, given it only exists to log permission's mutations
  — pick one, don't split further).

### organization (`internal/core/organization`)

- **Purpose**: hierarchical org-unit tree (branches/departments) used for
  RBAC data-scoping (`ORGANIZATION`/`ORGANIZATION_AND_CHILDREN` scopes) and
  as an optional dimension on customers/users/keygen history.
- **Entity**: `Organization`: ID, Code, Name, ParentID `*shared.ID`, Path
  (materialized path, e.g. `/root/child`), Level (int, depth — root=0),
  Status, CreatedAt, UpdatedAt.
- **Business rules**:
  - `ValidateInvariant()` / `DeriveLevel(path)`: `Level` must always equal
    the slash-count-derived depth of `Path` — a structural invariant
    enforced on read (`ErrInvalidTreeStructure` if it drifts). Port this
    validation as-is; don't let Java trust a stored `level` blindly.
  - `ErrCodeAlreadyExists` — `code` is unique.
- **Postgres table**: `organizations`. Self-referencing FK
  `parent_id`→`organizations(id)`. Referenced by: `customers.organization_id`,
  `user_profiles.organization_id`, `user_organizations.organization_id`,
  `keygen_history.organization_id`, `product_item_activation_requests.organization_id`.
- **HTTP endpoints**: `GET /organizations`, `POST /organizations`,
  `GET /organizations/{id}`, `PATCH /organizations/{id}`,
  `DELETE /organizations/{id}`.
- **Java target**: `platform.organization` — every module (Sales, HR,
  Accounting, ...) would reuse the same org-tree/data-scope mechanism
  identically; it carries no CRM-specific semantics despite currently only
  being consumed by CRM tables.

### profile (`internal/core/profile`)

- **Purpose**: 1:1 display profile per auth user (name shown in UI, avatar) —
  kept separate from `auth.AuthUser` (credentials) and `user.User`
  (admin-management fields).
- **Entity**: `Profile`: ID, UserID (unique), FirstName, LastName,
  AvatarURL, CreatedAt, UpdatedAt.
- **Business rules**: `ErrNotFound` and a generic `ErrInvalid(string)` only
  — no complex invariants; straightforward CRUD.
- **Postgres table**: `user_profiles`. FK `user_id`→`auth_users` (unique,
  cascade delete), `organization_id`→`organizations` (nullable; this column
  was later removed at the entity/business level per migration
  `000117_remove_organization_id_from_user_profiles` even though the entity
  struct never carried it — check current schema before mapping 1:1).
- **HTTP endpoints**: `GET /profile`, `PATCH /profile`,
  `POST /profile/change-password` (self-service, current-user scoped),
  `GET /users/{id}/profile`, `PATCH /users/{id}/profile` (admin-scoped).
- **Java target**: `platform.profile` (or fold into `platform.auth` as a
  sub-feature — it only ever appears alongside auth/user in the Go code).

### verification (`internal/core/verification`)

- **Purpose**: OTP/verification-code issuance and checking for email
  (registration, contact verification, password reset) — phone type exists
  in the enum but is unsupported per `auth` comments.
- **Entity**: `VerificationCode`: ID, AuthUserID `*shared.ID` (optional —
  may predate the user record during registration), Type (`email`/`phone`),
  Target (email or phone string), Code (plaintext, not persisted — comment
  says so explicitly), CodeHash (SHA-256), Purpose (`registration`,
  `verification`, `password_reset`), ExpiresAt, VerifiedAt, Attempts,
  CreatedAt.
- **Business rules**:
  - `IsExpired`, `IsVerified`, `CanAttempt(maxAttempts)` are entity methods
    — max-attempts lockout (`ErrMaxAttemptsExceeded`) and expiry
    (`ErrCodeExpired`) are both enforced at verify time.
  - `ErrRateLimitExceeded` — code generation itself is rate-limited per
    target (constructor takes `expiryMinutes, maxAttempts, rateLimitPerHour`
    as configuration, not hardcoded).
  - Codes are compared by hash only; plaintext never leaves the generation
    call.
- **Postgres table**: `verification_codes`. FK `auth_user_id`→`auth_users`
  (nullable).
- **HTTP endpoints**: none dedicated — consumed internally by `auth`
  (register/verify/resend-verification/forgot-password flows).
- **Java target**: `platform.verification` (already the sample-scaffold
  pattern referenced by the project docs).

### file (`internal/core/file`)

- **Purpose**: generic file storage + polymorphic attachment linking
  (`FileLink`) so any entity (work order, comment, product, activity, user
  avatar) can attach files without each domain owning its own upload logic.
- **Entities**:
  - `File`: ID, OriginalName, StorageKey (unique), MIMEType, SizeBytes,
    Visibility (`internal` | `customer_visible`), UploadedBy, CreatedAt,
    DeletedAt (soft delete). `MediaType()` derives `image`/`video`/`file`
    from MIME prefix.
  - `FileLink`: ID, FileID, EntityType (string, e.g. `work_order`,
    `activity`, `product`, `user`), EntityID, Purpose (`attachment`,
    `image`, `avatar`), DisplayOrder, CreatedAt.
  - `LinkedFile` = `File` embedded + `Link` (the join view returned to callers).
- **Business rules**:
  - A file can have multiple links (comment noted in `service.go`: "the
    same upload can belong to a work order and to the comment that
    introduced it; removing one link leaves the other — and the file — in
    place") — links and files have independent lifecycles.
  - `EnsureCanAttach` enforces per-entity attachment count limits
    (`ErrTooManyFiles`) and size/MIME constraints (`ErrFileTooLarge`,
    `ErrInvalidMIMEType`) before upload.
  - `DeleteAllLinked` is the "entity itself was deleted" cleanup path,
    distinct from `Unlink` (detach one link, keep file+other links) and
    `DeleteLinked` (soft-delete the file AND remove one link).
  - Unique constraint at DB level on `(file_id, entity_type, entity_id, purpose)`.
- **Postgres tables**: `files`, `file_links` (FK `file_id`→`files`, cascade
  delete; `entity_id` is a loose polymorphic reference, no FK — same
  logical-reference pattern as `auth_users.ref_id`).
- **HTTP endpoints**: no standalone `/files` CRUD — exposed entirely via
  other domains' routes (e.g. `POST /activities/{id}/images`,
  `POST /work-orders/{id}/attachments`, `POST /products/{id}/images`,
  `POST /manualhub/documents/{id}/images`, each with matching
  download/delete), registered centrally in `router.go` against a shared
  `FileHandler`.
- **Java target**: `platform.file` — matches the already-planned
  `platform.file` (`FileStorageService`) package; extend it with the
  `FileLink` polymorphic-attachment concept rather than reinventing
  per-module attachment logic.

### notification (`internal/core/notification`)

- **Purpose**: in-app notification feed per user, pushed live over
  WebSocket (`Pusher` interface, nil-safe/no-op in REST-only mode).
- **Entity**: `Notification`: ID, UserID, Type (string), Params (JSONB),
  Link (nullable), Data (JSONB, nullable), ReadAt (nullable — `IsRead()`),
  CreatedAt. `CreateInput` is the producer-side write DTO.
- **Business rules**:
  - `ErrForbidden` — every read/write op enforces ownership (`FindByID`,
    `MarkRead`, `Delete` all take `userID` and scope by it).
  - Cursor-based pagination (`ListFilter.Cursor`), not offset — note this
    for the Java DTO/pagination design (differs from the app's usual
    page/size convention).
  - `MarkAllRead`/`DeleteAll` are bulk, user-scoped operations.
- **Postgres table**: `notifications`. FK `user_id`→`auth_users` (cascade
  delete). Companion: `notification_delivery_log` (see `delivery` below).
- **HTTP endpoints**: `GET /notifications`, `GET /notifications/unread-count`,
  `PATCH /notifications/{id}/read`, `POST /notifications/read-all`,
  `DELETE /notifications/{id}`, `DELETE /notifications`.
- **Java target**: `platform.notification` — generic in-app notification
  mechanism, would be identical for Sales/Accounting alerts.

### delivery (`internal/core/delivery`)

- **Purpose**: outbound message delivery abstraction (email today; Zalo
  ZNS/WhatsApp Cloud channel constants already defined) with a persisted
  send-attempt audit log — decouples "something happened, notify the user
  externally" from the specific provider.
- **Entity**: `DeliveryLog`: ID, UserID `*shared.ID` (nullable), Channel
  (`email`/`zalo`/`whatsapp`), Provider, Recipient, Template, Status
  (`sent`/`failed`), Error, ProviderMessageID, CreatedAt, SentAt.
- **Business rules**:
  - `ErrSendFailed`/`ErrInvalidRecipient` are the only domain errors — most
    validation lives at the channel/provider adapter level.
  - Service doc comment: "Audit write failures are logged but never mask
    the primary send result" — persisting the log is best-effort and must
    never fail the actual send/caller flow. Preserve this fire-and-forget
    logging semantic in Java (don't wrap it in the same transaction as the
    triggering business action).
- **Postgres table**: `notification_delivery_log` (channel currently
  `CHECK (channel = 'email')` at the DB level even though the Go enum lists
  more — note this drift for the Java migration). FK `user_id`→`auth_users`
  (`ON DELETE SET NULL`).
- **HTTP endpoints**: none of its own; surfaced via the `messaging` handler
  (see Uncategorized) as an admin test endpoint.
- **Java target**: `platform.notification` (as the outbound-channel sibling
  of the in-app `notification` domain) — despite the aspirational Go→Java
  structure sketch in `erp_module_structure.md` listing `delivery` under
  `modules/crm`, apply the stated classification rule instead: sending a
  templated message over email/SMS/Zalo has no CRM-specific semantics and
  would be reused identically by Sales (order confirmations) or Accounting
  (invoice reminders) — recommend `platform.notification` (or a sibling
  `platform.delivery` package) over `modules.crm.delivery`.

---

## CRM

Business-specific domains for customer relationship management: `com.qs.Backend.modules.crm.*`.

### customer (`internal/core/customer`)

- **Purpose**: core customer record — the central entity most other CRM
  domains hang off of.
- **Entity**: `Customer`: ID, OrganizationID `*shared.ID`, Code (unique),
  FullName, Email, Phone, CompanyName, Source, StatusID `*shared.ID` (FK to
  `customer_statuses`), AssignedSalesID `*shared.ID` (FK to `auth_users`,
  **not** `sales_users` — changed by migration `000122`), CreatedBy
  `*shared.ID`, MainPhone, MainEmail, Website, Address, Gender, Region,
  GroupIDs `[]shared.ID` (many-to-many via join table), BusinessFieldIDs
  `[]shared.ID` (many-to-many via join table), LastContactAt (bumped by
  `customerlog.Log`), CreatedAt, UpdatedAt, DeletedAt (soft delete).
- **Business rules**:
  - `ErrCodeAlreadyExists` — `code` unique; `GenerateCustomerCode` derives a
    new code scoped to the organization.
  - Every create/update/soft-delete calls into `customerlog.Service` to
    write an audit trail (`SetAuditService` is late-bound — a
    container-wiring detail, not a business rule, but confirms
    `customer`→`customerlog` is a hard runtime dependency).
  - `diffCustomer` computes field-level changes for the audit log on update
    — only *changed* fields are logged.
  - Portal-related audit events (`LogPortalGranted/Revoked/PasswordReset`)
    live on the customer service even though the actual portal-account
    mutation happens in `auth` — customer service is just the log writer
    for those events, called by `auth`/handler glue.
- **Postgres table**: `customers`. FKs: `assigned_sales_id`→`auth_users`,
  `organization_id`→`organizations`, `status_id`→`customer_statuses`
  (added by `000121`, replacing an old free-text `status` column),
  `created_by`→`auth_users`. Join tables: `customer_customer_groups` (PK
  `customer_id,customer_group_id`), `customer_business_fields` (PK
  `customer_id,business_field_id`), both cascade-delete from `customers`.
- **HTTP endpoints**: `GET /customers`, `POST /customers`,
  `POST /customers/import`, `GET /customers/export-template`,
  `GET /customers/tracking-stats`, `GET /customers/{id}`,
  `PATCH /customers/{id}`, `DELETE /customers/{id}`. Plus routes owned by
  sibling domains but nested under `/customers/{id}/...`: `/activities`,
  `/logs`, `/supplemental-products`.
- **Java target**: `modules.crm.customer` (already scaffolded).

### customerstatus (`internal/core/customerstatus`)

- **Purpose**: reference/lookup table for customer lifecycle status
  (replacing a free-text column).
- **Entity**: `CustomerStatus`: ID, Name, Description, LabelVI, LabelEN,
  CreatedAt, UpdatedAt.
- **Business rules**: plain CRUD, `ErrNotFound`/`ErrInvalid` only. Seeded
  defaults: active, inactive, pending, closed.
- **Postgres table**: `customer_statuses`. Referenced by `customers.status_id`.
- **HTTP endpoints**: `GET/POST /customer-statuses`,
  `GET/PATCH/DELETE /customer-statuses/{id}`.
- **Java target**: `modules.crm.customerstatus`.

### customergroup (`internal/core/customergroup`)

- **Purpose**: reference table for grouping/segmenting customers (many-to-many).
- **Entity**: `CustomerGroup`: ID, Name, Description, LabelVI, LabelEN,
  CreatedAt, UpdatedAt.
- **Business rules**: plain CRUD only.
- **Postgres table**: `customer_groups`. Join: `customer_customer_groups`.
- **HTTP endpoints**: `GET/POST /customer-groups`,
  `GET/PATCH/DELETE /customer-groups/{id}`.
- **Java target**: `modules.crm.customergroup`.

### businessfield (`internal/core/businessfield`)

- **Purpose**: reference table for a customer's industry/business field
  (many-to-many), also reused as a free-text-ish tag on `lead`.
- **Entity**: `BusinessField`: ID, Name, Description, LabelVI, LabelEN,
  CreatedAt, UpdatedAt.
- **Business rules**: plain CRUD only.
- **Postgres table**: `business_fields`. Join: `customer_business_fields`.
- **HTTP endpoints**: `GET/POST /business-fields`,
  `GET/PATCH/DELETE /business-fields/{id}`.
- **Java target**: `modules.crm.businessfield`.

### customerlog (`internal/core/customerlog`)

- **Purpose**: append-only audit trail specific to customer-domain actions
  (distinct from the generic RBAC `audit` domain) — the source for the
  customer detail page's activity/history feed.
- **Entity**: `CustomerLog`: ID, CustomerID, ActorID, Action (large enum:
  `customer_create/update/delete`, `status_change`, `assign_sales`,
  `grant_portal`/`revoke_portal`/`reset_portal_password`,
  `note_add/update/delete`, `product_add/activate/recall/refurbish`),
  TargetType (`customer`/`note`/`product_item`/`supplemental_product`),
  TargetID `*shared.ID`, Changes (`[]FieldChange{Field,Old,New}`, JSONB),
  CreatedAt.
- **Business rules**:
  - `Log` skips writing for update-type actions when `changes` is empty
    ("prevents noise") — don't log no-op updates.
  - `Log` has a side effect beyond writing the row: it "bumps
    `customers.last_contact_at`" — i.e. writing *any* customer log entry
    touches the parent customer row. This cross-entity write must be
    preserved (likely as a single transactional service call in Java, not
    two independent repositories racing).
  - Note the **table name collision across two eras**: an original
    `customer_logs` table (migration `000050`) was dropped and recreated
    with a different schema (migration `000139`) after activity content
    was split out into the separate `activities` table (migration `000116`
    literally migrates old `customer_logs` rows into `activities` before
    dropping it). The *current* `customer_logs` table (from `000139`) is
    this generic-action audit trail; it is unrelated to the older
    freeform-note table of the same name. Use the `000139` schema only.
- **Postgres table**: `customer_logs` (current schema, from `000139`). FK
  `customer_id`→`customers` (cascade), `actor_id`→`auth_users`.
- **HTTP endpoints**: none standalone — `GET /customers/{id}/logs` is
  registered against `CustomerLogHandler` in `router.go`.
- **Java target**: `modules.crm.customerlog`.

### customerview (`internal/core/customerview`)

- **Purpose**: tracks the first time a salesperson viewed a given customer
  (used for "new/unseen" badges in sales UI).
- **Entity**: `CustomerView`: ID, CustomerID, SalesID, ViewedAt.
- **Business rules**: `RecordView` is explicitly idempotent — "if the view
  already exists, it does nothing" (backed by a DB `UNIQUE(customer_id, sales_id)`).
- **Postgres table**: `customer_views`. FKs `customer_id`→`customers`,
  `sales_id`→`auth_users` (both cascade delete). Unique
  `(customer_id, sales_id)`.
- **HTTP endpoints**: `POST /customer-views` (RecordView only — reads are
  consumed internally by the customer list endpoint, not exposed directly).
- **Java target**: `modules.crm.customerview`.

### lead (`internal/core/lead`)

- **Purpose**: lightweight contact-capture record from a public intake form
  ("khách hàng tiềm năng"), later convertible to a full `customer`.
- **Entity**: `Lead`: ID, Name, Phone, Email, BusinessField (string, not FK
  — despite the `business_field` reference domain existing separately),
  Notes, Services (`[]string`), CreatedAt, UpdatedAt. (Migration
  `000161_lead_replace_product_with_business_field_and_notes` — the schema
  used to carry a `product` field instead.)
- **Business rules**: plain CRUD, `ErrNotFound`/`ErrInvalid` only. No
  built-in "convert to customer" operation was found in the service
  surface — conversion (if any) happens at a higher/manual layer.
- **Postgres table**: `leads`. Soft delete (`deleted_at`), no FKs (fully
  standalone/anonymous-submission table).
- **HTTP endpoints**: `GET/POST /leads`, `GET/PATCH/DELETE /leads/{id}`.
- **Java target**: `modules.crm.lead`.

### activity (`internal/core/activity`)

- **Purpose**: polymorphic activity/note log attachable to a customer (the
  `ActivityableType`/`ActivityableID` pattern currently supports `customer`
  only in practice, though `lead` is mentioned in the DB check constraint).
  This is the sales rep's freeform note feed — distinct from `customerlog`
  (structured action audit) and `servicelog` (technical maintenance journal).
- **Entities**: `Activity`: ID, ActivityableType, ActivityableID, SalesID,
  Action, Content, ContactAt (nullable), CreatedAt, Images (`[]Image`).
  `Image`: ID, ActivityID, ImageURL, DisplayOrder, CreatedAt.
- **Business rules**:
  - Every create/update/delete on a customer-linked activity also writes a
    `customerlog` entry (`note_add`/`note_update`/`note_delete`) via
    `SetAuditService` — same late-bound wiring pattern as `customer`.
  - `noteSnippet` truncates content for the audit feed display — a
    presentation concern currently baked into the service layer.
- **Postgres tables**: `activities` (`CHECK activityable_type IN ('lead','customer')`,
  FK `sales_id`→`auth_users`), `activity_images` (FK `activity_id`→`activities`,
  cascade). Both were populated by migrating out of the old `customer_logs`/
  `customer_log_images` tables (see `customerlog` note above); a later
  migration (`000159_drop_activity_images`) exists — verify current schema
  before assuming `activity_images` still exists as a first-class table
  when writing the Java entity (it may have been folded into `file`/`FileLink`
  as `EntityTypeActivity`/`PurposeImage` instead — the `file` domain's
  `EntityTypeActivity` constant and the `/activities/{id}/images` routes
  wired to `FileHandler` in `router.go` both suggest images moved to the
  generic `file` domain post-`000159`).
- **HTTP endpoints**: `GET/POST /customers/{customer_id}/activities`,
  `GET/PATCH/DELETE /activities/{id}`; images via `FileHandler`:
  `POST/GET/DELETE /activities/{id}/images[/{file_id}]`.
- **Java target**: `modules.crm.activity`.

### customerportal (`internal/adapters/http/customerportal`, no `internal/core` counterpart)

- **Purpose**: a customer-facing self-service read/write surface (the "my
  account" area of the CRM's public-facing portal) — not its own domain but
  an aggregation layer scoped to the logged-in customer (resolved via
  `auth`'s `RefID`), composing `productitem`, `product`, `workorder`,
  `workordercomment`, `servicelog`, `profile`, and `file`.
- **Backing domains** (from handler imports): `authdomain` (session/RefID→customer
  resolution), `filedomain`, `productdomain`, `productitemdomain`,
  `profiledomain`, `servicelogdomain`, `workorderdomain`, `workordercommentdomain`.
- **Business rules**: enforces that every resource fetched/mutated belongs
  to *this* logged-in customer (via product item's `CustomerID` and work
  order chain) — the entire point of the handler is that authorization
  boundary; there is no separate entity or table.
- **Postgres tables**: none of its own — reads/writes existing `product_items`,
  `work_orders`, `work_order_comments`, `service_logs`, `files`/`file_links` tables.
- **HTTP endpoints**: `GET /my/product-items[/{id}]`, `GET /my/warranties`,
  `POST /my/service-requests`, `GET /my/service-requests[/{id}]`,
  `POST/GET /my/service-requests/{id}/attachments[/{file_id}]`,
  `GET/POST /my/service-requests/{id}/comments`,
  `PATCH/DELETE /my/service-requests/{id}/comments/{comment_id}`,
  `POST/GET/DELETE /my/service-requests/{id}/comments/{comment_id}/attachments[/{file_id}]`.
- **Java target**: `modules.crm.customerportal` — a controller-only
  aggregation module that calls the public services of `inventory.productitem`,
  `crm.workorder`, `crm.workordercomment`, `platform.file`, `platform.auth`.
  It should stay thin and never own entities directly.

### workorder (`internal/core/workorder`)

- **Purpose**: work order for production or service (maintenance/repair)
  against a product item, including customer-initiated service requests
  from the portal.
- **Entity**: `WorkOrder`: ID, OrderNumber (unique, auto `WO-YYYYMMDD-NNNN`),
  ProductItemID `*shared.ID`, Type (`production`,
  `preventive_maintenance`, `corrective_maintenance`, `customer_request`),
  Priority (`low`/`normal`/`high`/`urgent`), AssignedTo `*shared.ID`,
  Description, Status (`pending`/`in_progress`/`completed`/`cancelled`),
  ScheduledDate, StartedAt, CompletedAt, CreatedBy, CreatedAt, UpdatedAt,
  DeletedAt.
- **Business rules**:
  - State machine enforced via entity methods: `CanStart` (only from
    `pending`), `CanComplete` (only from `in_progress`), `CanReopen` (only
    from `completed`/`cancelled`). Corresponding `ErrCannotStart/Complete/Cancel/ReopenWorkOrder`.
  - Every lifecycle transition writes a `servicelog` journal event
    (`recordEvent` → `servicelog.EventType`: created/assigned/unassigned/
    reassigned/started/completed/reopened/cancelled/attachment_added/removed)
    — "the journal accounts for the whole request, not only its technical
    work" (doc comment). This is a **hard dependency workorder→servicelog**
    to preserve.
  - Also fires notifications via an injected `NotificationSender` for
    customer-request lifecycle events — nil-safe (silently no-ops if unset,
    used in tests), and deliberately injected post-construction via a
    setter "to avoid a circular dependency with the notifier package (which
    itself depends on email + customer + product services)" — i.e. the Go
    code already worked around a would-be dependency cycle by using
    late-binding/setter injection instead of constructor injection. In Java,
    prefer resolving this via an event-driven or explicit port interface
    rather than field injection.
  - File attachment events are reported into the journal by the *caller*
    that moved the file (`RecordAttachmentEvent`), since "Files are handled
    by the file domain, which knows nothing about journals" — i.e. `file`
    must stay ignorant of `workorder`; the coupling is one-directional from
    `workorder` into `file`, never the reverse.
- **Postgres table**: `work_orders` (originally `asset_id`, renamed to
  `product_item_id` by `000132`). FKs: `product_item_id`→`product_items`,
  `assigned_to`→`auth_users`, `created_by`→`auth_users`.
- **HTTP endpoints**: `GET/POST /work-orders`, `GET/PATCH/DELETE /work-orders/{id}`,
  `POST /work-orders/{id}/start`, `.../complete`, `.../cancel`, `.../reopen`.
  Attachments via `FileHandler`: `POST/GET/DELETE /work-orders/{id}/attachments[/{file_id}]`.
- **Java target**: `modules.crm.workorder`.

### workordercomment (`internal/core/workordercomment`)

- **Purpose**: free-form comment thread on a work order (service request),
  distinct from `servicelog` (structured maintenance record).
- **Entity**: `Comment`: ID, WorkOrderID, AuthorID, Content (max 5000
  chars), IsInternal (hides from customer portal), CreatedAt, UpdatedAt,
  plus read-only `AuthorName` and `Attachments []*file.LinkedFile`.
- **Business rules**:
  - `CanBeModifiedBy`: author can always edit/delete; others need a
    moderation permission resolved by the caller into `canManage`.
  - `AttachmentVisibility`: attachments on an internal comment are always
    `file.VisibilityInternal`, never `customer_visible` — enforced at the
    entity level so it can't be bypassed by callers.
  - Every comment write is mirrored into `servicelog` as a note entry
    (`mirrorToJournal`), "keyed on the comment, so an edit rewrites it
    instead of adding another line" — and deleting the comment removes its
    mirrored journal entry (`servicelog.DeleteCommentNote`). This
    comment↔servicelog mirroring is a two-way sync to replicate carefully.
  - `ErrInternalForbidden` — writing an internal comment requires the
    corresponding write permission, checked before create.
- **Postgres table**: `work_order_comments`. FK `work_order_id`→`work_orders`
  (cascade), `author_id`→`auth_users`. Soft delete (`deleted_at`). Ships
  its own permission set: `work_order_comment.read/create/read_internal/manage`.
- **HTTP endpoints**: `GET/POST /work-orders/{work_order_id}/comments`,
  `PATCH/DELETE /work-order-comments/{id}`,
  `POST /work-order-comments/{id}/attachments`,
  `GET/DELETE /work-order-comments/{id}/attachments/{file_id}`.
- **Java target**: `modules.crm.workordercomment`.

### servicelog (`internal/core/servicelog`)

- **Purpose**: two things sharing one table: (1) structured maintenance
  history for a product item (technician-written), and (2) an
  action/event journal for a work order's lifecycle (system-written) plus
  mirrored comments (see `workordercomment`). The `ServiceType` field
  discriminates all three.
- **Entity**: `ServiceLog`: ID, ProductItemID, WorkOrderID `*shared.ID`,
  ServiceDate, ServiceType (`installation`/`maintenance`/`repair`/
  `inspection`/`note`/`event`), IssueDescription, ActionTaken,
  PartsReplaced (`[]PartReplaced{PartName,Quantity,PartCode}`), TechnicianID,
  CustomerNotes, InternalNotes, SourceCommentID `*shared.ID` (set only on
  comment-mirrored `note` entries), EventType `*EventType` (set only on
  `event` entries: created/assigned/unassigned/reassigned/started/completed/
  reopened/cancelled/attachment_added/attachment_removed), EventMetadata
  (JSONB — "the previous and the new assignee, the name of the file
  attached"), CreatedAt, UpdatedAt.
- **Business rules**:
  - DB-level CHECK constraint: `(service_type = 'event') = (event_type IS NOT NULL)`
    — the two columns must agree; an `event` row without a kind or a kind
    on a maintenance row is invalid. **Enforce this at the Java entity/DB
    level too**, not just in application code.
  - `ErrEntryNotEditable` — mirrored (`note`) and system (`event`) entries
    cannot be freely edited like a technician's own maintenance log entry;
    only their originating action (comment edit, or nothing, for events)
    can change them.
  - `SyncCommentNote`/`DeleteCommentNote`: idempotent upsert/delete keyed on
    `SourceCommentID`, called by `workordercomment`.
  - `RecordEvent`: always appends, "nothing is keyed or merged" (unlike
    comment mirrors) — called by `workorder`.
- **Postgres table**: `service_logs` (originally `asset_id`, renamed
  `product_item_id` by `000132`; `event_type`/`event_metadata` added by
  `000165`). FKs: `product_item_id`→`product_items` (NOT NULL),
  `work_order_id`→`work_orders`, `technician_id`→`auth_users`.
- **HTTP endpoints**: `GET/POST /service-logs`, `GET/PATCH /service-logs/{id}`
  (note: no `DELETE` route — consistent with entries being an append-mostly
  journal).
- **Java target**: `modules.crm.servicelog`.

### activationrequest (`internal/core/activationrequest`)

- **Purpose**: sales-user-submitted request to bulk-activate one or more
  product items (or attach supplemental products) for a customer, subject
  to admin/manager approval before the activation actually runs.
- **Entity**: `ActivationRequest`: ID, RequestType (`activation` |
  `supplemental_product`), SalesUserID, CustomerID, OrganizationID
  `*shared.ID`, Inputs (`[]string`, JSONB), WarrantyExpiry `*time.Time`,
  Status (`pending`/`approved`/`rejected`), Result (`[]byte` raw JSON of
  the bulk-activate response, nil until approved), RejectReason,
  SubmittedAt, ReviewedBy, ReviewedAt, CreatedAt, UpdatedAt.
- **Business rules**:
  - `IsPending`/`CanReview` — only `pending` requests can be approved/rejected.
  - `Approve` "runs the bulk activation pipeline" synchronously — approval
    is not just a status flip, it performs the real `productitem.Activate`
    calls and persists their outcome into `Result`.
  - Input bounds: `ErrEmptyInputs`, `ErrTooManyInputs`, `ErrInputTooLong` —
    a capped batch size and per-input length limit.
  - `ErrEmptyCustomer` — `customer_id` is always required, even for the
    `supplemental_product` request type.
- **Postgres table**: `product_item_activation_requests`. FKs:
  `sales_user_id`→`auth_users` (RESTRICT), `customer_id`→`customers`
  (RESTRICT), `organization_id`→`organizations` (SET NULL),
  `reviewed_by`→`auth_users` (SET NULL). `status` has a DB CHECK constraint
  (`pending`/`approved`/`rejected`) — mirror as a Java enum + DB check, not
  just an application-level enum.
- **HTTP endpoints** (registered in `router.go`, scoped under
  `/product-items/activation-requests`): `GET .../activation-requests`,
  `GET .../activation-requests/{id}`, `POST .../activation-requests`
  (Submit), `POST .../activation-requests/{id}/approve`,
  `POST .../activation-requests/{id}/reject`.
- **Java target**: `modules.crm.activationrequest`.

### keygen (`internal/core/keygen`)

- **Purpose**: generates time-based unlock passwords for physical machines
  (factory demo passwords, "active" unlock codes derived from a device
  code, and 6-tier machine-lock duration passwords) and records a history
  of every generation for audit/compliance.
- **Entity**: `KeygenHistory`: ID, KeyType (`factory`/`active`/`machine_lock`),
  InputData/OutputData (`map[string]any`, JSONB), GeneratedBy,
  GeneratedByName (denormalized), OrganizationID `*shared.ID`,
  ProductItemID `*shared.ID`, ProductItemCode (denormalized, populated from
  JOIN), CreatedAt. Value types: `FactoryPassword{Time,Password}`,
  `MachineLockKeys{Week,Days30,Days60,Year1,Years2,Infinity}`.
- **Business rules**:
  - `GenerateFactoryPasswords` produces 10 passwords for t+0..t+9 minutes.
  - `GenerateActivePassword` extracts a device code from the input (split
    on first `-`) and auto-resolves the matching `productitem` by code —
    `GenerateActiveWithItemID` is the variant used by bulk-activate that
    skips this resolution because the caller already has the item ID.
  - `GenerateMachineLockKeys` format: `{input}-{PrivateKey}-{password}`,
    one per duration tier.
  - Every generation writes a lifecycle log entry via
    `productitemlog.Service` (`logKeygen`, best-effort/non-fatal) — a hard
    **keygen→productitemlog** dependency.
  - `ErrInvalidTimeParameters`, `ErrInvalidInput`, `ErrInvalidPassword` —
    input validation only; no state machine.
- **Postgres table**: `keygen_history` (originally `asset_id`, renamed
  `product_item_id` by `000132`). FKs: `generated_by`→`auth_users`,
  `organization_id`→`organizations`, `product_item_id`→`product_items`.
- **HTTP endpoints**: `POST /keygen/factory`, `POST /keygen/active`,
  `POST /keygen/machine-lock`, `GET /keygen/history`.
- **Java target**: `modules.crm.keygen` — despite touching `productitem`
  (inventory), the *business behavior* (license/unlock generation tied to
  the CRM activation workflow) is CRM-specific; call `inventory.productitem`
  through its public service, not its repository.

### manualhub (`internal/core/manualhub`)

- **Purpose**: versioned technical-document management for products
  (user manuals, spec sheets) with a draft→review→released→(hidden/rollback)
  workflow and a public-read surface for the marketing site.
- **Entities**:
  - `Document`: ID, ParentID `*shared.ID` (links a document to its previous
    released version), ProductID, ProductName (denormalized), Title,
    Description, DocumentType, Format, Language (`vi`/`en`), Version,
    Content (JSONB), Status (`draft`/`review`/`released`/`rejected`/
    `hidden`/`pending_delete`), AuthorID, AuthorName (denormalized),
    IsCurrent (bool — only one released version per product is "current"),
    RejectionReason, RollbackReason, SubmittedAt, ReleasedAt, CreatedAt,
    UpdatedAt, DeletedAt.
  - `Version`: ID, DocumentID, Version, Title, Content (JSONB), CreatedBy,
    CreatedByName, CreatedAt — an **immutable snapshot** taken at release
    time (comment: "immutable snapshot of a document taken at release time").
- **Business rules**:
  - `NextVersion`: bumps minor version `v1.4→v1.5`; anything not yet
    released (including initial `draft`) starts at `v1.0`.
  - `Publish` creates a `Version` snapshot and flips `IsCurrent`.
  - `Hide` hides a released doc and **promotes its parent back to current**;
    `Unhide` reverses this (restores the hidden doc as current, demotes the
    parent). `Rollback` hides the current version (marking it with an admin
    reason) and restores the previous version as current — three distinct,
    carefully paired state transitions to preserve exactly.
  - Soft-delete requires **two-step approval**: `RequestDelete` (submitter)
    → `ApproveDelete`/`RejectDelete` (reviewer) — not a direct delete, even
    for users with the base delete permission (`manualhub.document.delete`
    triggers the *request*; hard delete is a separate reviewer-only step
    per the router comment: "manualhub.document.delete (not .hard) is the
    route-level gate here").
  - Has its own activity/audit trail (`ListActivities`/`ListAllActivities`
    → `manualhub_activities` table) — a fourth, document-specific audit log
    pattern in this codebase (alongside `audit`, `customerlog`, `servicelog`).
- **Postgres tables**: `manualhub_documents` (self-referencing FK
  `parent_id`, FK `product_id`→`products`, FK `author_id`→`auth_users`,
  CHECK constraints on `status`/`language`), `manualhub_document_versions`
  (FK `document_id`→`manualhub_documents` cascade, unique
  `(document_id, version)`), `manualhub_activities` (table exists per
  `000174` migration, not dumped in full here — cross-check columns when
  porting).
- **HTTP endpoints**: `GET /manualhub/stats`, `GET/POST /manualhub/documents`,
  `GET/PATCH /manualhub/documents/{id}`, `GET /manualhub/documents/{id}/versions[/{version}]`,
  `GET /manualhub/documents/{id}/activities`, `GET /manualhub/activities`,
  `POST /manualhub/documents/{id}/publish|hide|unhide|rollback`,
  `POST /manualhub/documents/{id}/delete-request|delete-approve|delete-reject`,
  `DELETE /manualhub/documents/{id}` (hard delete, admin-only per router
  comment), images via `FileHandler`: `POST /manualhub/documents/{id}/images`.
  Public (no auth): `GET /public/manualhub/documents[/{id}]`.
- **Java target**: `modules.crm.manualhub` (per the structure doc's own
  sketch) — reasonable given it's product-documentation workflow tightly
  coupled to the sales/service story, though it could equally sit under
  `inventory` since it's keyed by `product_id`; keep under `crm` for
  consistency with the existing sketch.

### dashboard (`internal/adapters/http/dashboard`, no `internal/core` counterpart)

- **Purpose**: a single aggregation endpoint returning admin dashboard
  stats — no entity, no table.
- **Backing domains**: `customer.Service` (total customer count, respecting
  data-scope), `user.UserRepository` (total user count), `permission.Service`
  (resolves the caller's scope params).
- **Business rules**: counts respect the caller's data scope
  (org/org+children/all) via `permission.ScopeParams` — not a flat global
  count.
- **Postgres tables**: none of its own — reads `customers`, `auth_users`.
- **HTTP endpoints**: `GET /dashboard/stats` → `{total_customers, total_users}`.
- **Java target**: `modules.crm.dashboard` (per structure doc sketch) — thin
  aggregation controller calling `crm.customer` and `platform.auth`'s
  public services; do not give it its own entity/table.

### customersupplementalproduct (`internal/adapters/http/customersupplementalproduct`, no `internal/core` counterpart)

- **Purpose**: lets a customer be linked to "supplemental" product items
  (accessories/consumables beyond their primary machine) — reuses the
  existing `product`/`productitem`/`productitemlog` machinery rather than
  defining a new entity. Migration `000153` is explicitly a no-op ("This
  migration is intentionally empty so fresh databases do not create a
  separate customer/product table" — supplemental records ARE product-item
  rows, distinguished by `activationrequest.RequestTypeSupplementalProduct`
  and `customerlog.TargetSupplementalProduct`, not a separate table).
- **Backing domains**: `customer` (read-only lookup), `product`
  (find-or-create by code), `productitem` (activate / find-or-create),
  `productitemlog` (source enum reuse).
- **Business rules**: none of its own beyond orchestration — validation
  lives in the underlying `productitem.Activate`/`FindOrCreateByCode` calls.
- **Postgres tables**: none — uses `product_items`/`products`.
- **HTTP endpoints**: `GET /customers/{id}/supplemental-products`
  (permission: `customer.read`), `POST /customers/{id}/supplemental-products`
  (permission: `product_item.direct_activate`).
- **Java target**: fold into `modules.crm.customer` as a sub-feature/nested
  controller rather than a new top-level domain — it has no entity of its
  own and exists purely to compose `crm.customer` + `inventory.product` +
  `inventory.productitem`.

---

## Inventory

### product (`internal/core/product`)

- **Purpose**: product catalog/model definitions (the "SKU"), as opposed to
  `productitem` (a specific serialized unit of a product).
- **Entity**: `Product`: ID, Code (unique), Name, Specifications
  (`map[string]interface{}`, JSONB), WarrantyMonths (default from service
  config, not hardcoded), ImageURL (populated at the HTTP layer from
  `file_links` — **not stored on the row**, so the Java entity should omit
  this field and resolve it via `platform.file` at the read/DTO layer),
  CreatedAt, UpdatedAt, DeletedAt.
- **Business rules**:
  - `ErrProductCodeExists` — `code` unique (partial unique index,
    `WHERE deleted_at IS NULL`, so a soft-deleted code can be reused).
  - `DefaultWarrantyMonths()` / `FindOrCreateByCode`: auto-provisions unseen
    models during the bulk-activate flow with `{code, name: code}` and the
    configured default warranty — a convenience/data-quality mechanism to
    replicate (don't require product records to pre-exist for activation
    to work).
- **Postgres table**: `products` (also has an unused `model_number` column
  not present on the Go entity — check whether to carry it forward).
- **HTTP endpoints**: `GET/POST /products`, `GET/PATCH/DELETE /products/{id}`.
  Images via `FileHandler`: `POST/GET/DELETE /products/{id}/images[/{file_id}]`.
- **Java target**: `modules.inventory.product` (already scaffolded).

### productitem (`internal/core/productitem`)

- **Purpose**: an individual serialized machine/unit — the thing that gets
  activated for a customer, serviced, recalled, refurbished. Renamed from
  `Asset` (migration `000132`) — expect residual naming (`asset_id` columns
  elsewhere already renamed, but double check any leftover references).
- **Entity**: `ProductItem`: ID, Code (renamed from `serial_number`,
  `000129`), ProductID, CustomerID `*shared.ID`, ManufacturingDate,
  InstallationDate, WarrantyExpiry, Status (`stock`/`active`/`maintenance`/
  `out_of_order`/`decommissioned`/`recalled`/`refurbished`), ActivatedAt,
  ActivatedBy, ActivationNotes, RecalledAt, RecallNotes, RefurbishedAt,
  RefurbishNotes, CreatedAt, UpdatedAt, DeletedAt.
- **Business rules** (all as entity-level state-machine predicates):
  - `CanActivate`: only from `stock` or `refurbished`, and only if
    `CustomerID` is nil (i.e., not already bound to a customer).
  - `CanRecall`: only from `active`.
  - `CanRefurbish`: only from `recalled`.
  - `IsUnderWarranty`: `now < WarrantyExpiry` (nil-safe → false).
  - `ErrInvalidWarrantyExpiry` — an override warranty date cannot precede
    the installation date.
  - `ErrCodeExists` — soft-delete-aware unique constraint (`000154`).
  - Every create/activate/recall/refurbish call writes a
    `productitemlog.Log` entry (lifecycle history) AND, when bound to a
    customer, a `customerlog` entry (`product_add/activate/recall/refurbish`)
    — this domain has **two** parallel audit-log dependencies
    (`productitemlog` unconditionally, `customerlog` only when
    customer-bound) to preserve exactly.
  - `Recall` "detach[es] the customer" as part of the transition — recall
    is not just a status flip, it also nulls `customer_id`.
- **Postgres table**: `product_items` (renamed from `assets`, `000132`).
  FKs: `product_id`→`products`, `customer_id`→`customers`,
  `activated_by`→`auth_users`.
- **HTTP endpoints**: `GET/POST /product-items`,
  `POST /product-items/bulk-activate`, `POST /product-items/preflight`,
  `POST /product-items/preflight/release`, `GET/PATCH/DELETE /product-items/{id}`,
  `POST /product-items/{id}/activate|recall|refurbish`.
- **Java target**: `modules.inventory.productitem` (already scaffolded).

### productitemlog (`internal/core/productitemlog`)

- **Purpose**: append-only lifecycle event log for a product item — separate
  from `servicelog` (maintenance work) and `customerlog` (customer-scoped
  audit); this one is item-scoped and system/actor-attributed regardless of
  whether a customer is involved.
- **Entity**: `Log`: ID, ProductItemID, EventType (`created`/`activated`/
  `reactivated`/`recalled`/`refurbished`/`keygen`/`work_order_linked`/
  `status_changed`), ActorID `*shared.ID`, ActorRole (`admin`/`sales`/
  `accountant`/`system`/`customer`), Source (`direct`/`bulk`/
  `approval_request`/`customer_portal`/`system`/`work_order`), CustomerID
  `*shared.ID`, Metadata (JSONB), OccurredAt, CreatedAt, plus denormalized
  read fields (`ActorLastName`/`ActorFirstName`/`CustomerName`).
- **Business rules**: `Log` is explicitly documented as best-effort —
  "errors are logged but not returned in callers to avoid failing the
  primary operation." **Every caller of this service** (`productitem`,
  `keygen`) treats it as fire-and-forget; the Java port should not let a
  logging failure roll back the primary transaction.
- **Postgres table**: `product_item_logs`. FKs: `product_item_id`→`product_items`
  (cascade), `actor_id`→`auth_users` (SET NULL), `customer_id`→`customers`
  (SET NULL).
- **HTTP endpoints**: `GET /product-items/{id}/logs` (registered centrally,
  permission `product_item.read_logs`).
- **Java target**: `modules.inventory.productitemlog`.

---

## Sales

No domains found under `internal/core` or the HTTP layer map to Sales
beyond the vestigial `sales_users` table (created in `000030_sales`, still
referenced nowhere except as the historical FK target for
`customers.assigned_sales_id` before `000122` repointed it to `auth_users`
— `sales_users` appears effectively dead/unused by current Go code and is
**not** a domain to port). No quotation/order/contract logic exists yet in
Go — `modules.sales.*` in Java starts from zero, matching `erp_module_structure.md`.

## Purchasing / Accounting / HR / Manufacturing

No corresponding domains exist in the Go codebase at all — these Java
module directories should remain unpopulated until real business
requirements arrive, exactly as `erp_module_structure.md` already states
("chưa tạo — chỉ tạo khi thực sự triển khai nghiệp vụ tương ứng").

---

## Uncategorized

Domains that don't map cleanly onto one ERP module, or that fall outside
the ERP/CRM system's bounded context entirely.

### metadata (`internal/adapters/http/metadata`, no `internal/core` counterpart)

- **Purpose**: a single aggregation endpoint feeding frontend dropdowns —
  spans `customergroup`, `customerstatus`, `businessfield` (CRM reference
  data) and `organization`, `user`, `permission`/`role` (platform reference
  data) behind one `GET /metadata/{domain}` dispatcher, plus
  `GET /metadata/users`.
- **Business rules**: `customerAssignablePermissionCode = "customer.assignable"`
  gates which users show up as assignable sales reps — the one piece of
  actual logic here, everything else is passthrough listing.
- **Postgres tables**: none of its own — reads across `customer_groups`,
  `customer_statuses`, `business_fields`, `organizations`, `auth_users`, `roles`.
- **HTTP endpoints**: `GET /metadata/{domain}`, `GET /metadata/users`.
- **Java target**: **do not port as a single aggregator.** It structurally
  depends on both `modules.crm.*` and `platform.*` repositories directly,
  which would force `platform` (or a shared `metadata` package) to import
  business modules — a boundary violation by construction. Recommend
  splitting into per-module `GET /crm/metadata` (customer groups/statuses/
  business fields) and reusing existing `platform.organization`/`platform.auth`
  list endpoints directly from the frontend instead of a single dispatcher.

### messaging (`internal/adapters/http/messaging`, no `internal/core` counterpart)

- **Purpose**: admin-only QA/test endpoint that sends one message through
  the `delivery` domain's configured channel and returns the persisted log
  row. Explicitly documented as reusing an existing permission
  (`auth.manage_roles`) "to avoid adding a permission-seed migration just
  for a QA endpoint" — a deliberate shortcut, not a pattern to imitate.
- **Backing domain**: `delivery.Service` entirely — no logic of its own.
- **Postgres tables**: none of its own (writes to `notification_delivery_log`
  via `delivery`).
- **HTTP endpoints**: `POST /messages/send`.
- **Java target**: not a separate domain — expose as a diagnostic endpoint
  on `platform.notification` (wherever `delivery` lands), gated by a proper
  dedicated permission in Java rather than reusing an unrelated one.

### content (`internal/adapters/http/content`, no `internal/core` counterpart)

- **Purpose**: generic CRUD over the **marketing website's** CMS tables
  (`applications`, `catalog_products`, `download_files`, `hero_triptychs`,
  `machines`, `news`, `product_series`, `products`, `services`) living in a
  **separate database** (`qs_data`, distinct from the CRM's own Postgres
  database) for the public qs-website. The Go package doc comment is
  explicit: "These tables are flat, admin-edited JSON blobs with no CRM
  business rules attached... this handler talks to Postgres directly
  instead of going through the usual core/service/repository layers."
- **Business rules**: table allow-list (`Tables` map with per-table PK
  column) is the only "schema" — otherwise fully dynamic/generic CRUD +
  image upload to MinIO.
- **Postgres tables**: none of the CRM's own — a distinct `qs_data`
  database's tables, not covered by `migrations/*.sql` in this repo at all.
- **HTTP endpoints**: `GET/POST /public/content/{table}[/{id}]` (public,
  read-only), `GET/POST/PUT/DELETE /content/{table}[/{id}]`,
  `GET /content/tables`, `GET /content/{table}/_schema`,
  `POST /content/{table}/_bulk`, `POST /content/images` (authenticated, no
  per-table permission — "login required" is the entire gate per the
  router comment).
- **Java target**: **out of scope for this ERP/CRM port.** This is a
  separate marketing-site CMS bounded context (different database,
  explicitly no business rules) — do not create a `modules.*.content` or
  `platform.content`; if the Java backend needs to serve it at all, treat
  it as an entirely separate concern/service, not part of the ERP module tree.

---

## Suggested porting order

Ranked by fewest cross-domain dependencies first, so each step's
dependencies are already in place:

1. **`shared` (kernel)** — ID value type only; nothing depends on business
   code. (Not itself a domain to port as a "module", but the
   `com.qs.Backend.shared` kernel must exist first.)
2. **`platform.organization`** — no dependencies beyond `shared`; self-referencing tree only.
3. **`platform.verification`** — no dependencies beyond `shared`.
4. **`platform.file`** — no dependencies beyond `shared`; polymorphic, everything else attaches to it later.
5. **`platform.permission`** (absorbing `role`) — depends only on `shared`/`organization`.
6. **`platform.audit`** — depends only on `permission` (writer of the log).
7. **`platform.auth`** (absorbing `profile`) — depends on `permission`, `verification`; **must not** depend on `customer` (fix the Go violation here, see below).
8. **`platform.notification`** (absorbing `delivery`/in-app `notification`) — depends on `auth` (recipient) only.
9. **`modules.inventory.product`** — depends only on platform layer.
10. **`modules.inventory.productitem`** + **`modules.inventory.productitemlog`** — depend on `product`, `auth`, `customer` (see #11) — port these two together since `productitem` writes to `productitemlog` on nearly every mutation.
11. **`modules.crm.customer`** — depends on `organization`, `auth`; needed before `productitem` can bind customers, so in practice #10 and #11 should land close together (customer first, if a strict order is needed, since `productitem.CustomerID` references it).
12. **`modules.crm.customerstatus`, `customergroup`, `businessfield`** — pure reference tables, trivial, can be done in parallel with #11.
13. **`modules.crm.customerlog`** — depends on `customer`.
14. **`modules.crm.customerview`, `lead`, `activity`** — depend on `customer`/`auth` only.
15. **`modules.crm.workorder`** + **`modules.crm.servicelog`** — mutually referencing (workorder writes servicelog events); port together.
16. **`modules.crm.workordercomment`** — depends on `workorder`, `servicelog`, `file`.
17. **`modules.crm.keygen`** — depends on `productitem`, `productitemlog`.
18. **`modules.crm.activationrequest`** — depends on `productitem`, `product`, `customer`, `auth`.
19. **`modules.crm.customersupplementalproduct`** (as a `customer` sub-feature) — depends on everything in #9–18.
20. **`modules.crm.manualhub`** — depends on `product`, `file`, `auth`.
21. **`modules.crm.customerportal`, `dashboard`** — thin aggregation controllers, last, once all underlying services exist.
22. **`metadata`, `messaging`** — reimplement narrowly per the Uncategorized-section recommendations, whenever the frontend actually needs them; not a porting priority.
23. **`content`** — excluded entirely from this port.

## Known Go-side dependency violations to avoid repeating in Java

- **`auth` (platform) directly imports and calls `customer.Service`
  (business/CRM)** — `internal/modules/system/auth/handler.go` imports
  `qs-crm/internal/core/customer` and, in `CustomerRegister`, calls
  `customerSvc.Create(...)` to auto-provision a `customers` row when a
  newly-registered portal user has no `RefID` yet. This is the one
  platform→modules edge in the whole codebase. **In Java**: keep
  `platform.auth` ignorant of `modules.crm.customer` entirely — model
  "new customer self-registration" as `modules.crm.customer` (or a
  dedicated onboarding use case in `modules.crm`) calling `platform.auth`'s
  public registration service, or emit a domain event from auth
  (`UserRegistered`) that a CRM-side listener reacts to by creating the
  customer record. Either direction keeps `platform` dependency-free of
  `modules`.
- **`role` is a redundant Go package wrapping `permission.Role`** — not a
  layering violation, but an unnecessary split that would be a maintenance
  trap if copied verbatim; merge into `platform.permission` in Java (see
  the `role` section above).
- **Table-name reuse across schema eras**: `customer_logs` was dropped and
  recreated with an unrelated schema (`000050` era vs. `000139` era, with
  `activities`/`activity_images` carved out in between via `000116`). Don't
  let this history leak into the Java migration — write one clean
  `customer_logs` migration matching the *current* (`000139`) shape, and a
  separate `activities`/`activity_images` (or `activities` + `platform.file`
  links, if `000159` indeed retired `activity_images` — verify current
  schema before writing the Flyway migration) migration; do not try to
  replay the Go migration history.
- **`content`'s bypass of core/service/repository layers** talking straight
  to Postgres from the HTTP handler is an intentional, documented exception
  in Go for a non-CRM CMS surface — it is explicitly out of scope for this
  port (see the `content` section), not a pattern to defend against
  replicating elsewhere, but worth naming so nobody mistakes it for house style.
- **`workorder`'s notifier is wired via a post-construction setter
  (`SetNotifier`) specifically to dodge a dependency cycle** (notifier →
  email + customer + product services → ... → workorder). This is a code
  smell papering over a real cyclic dependency in the business logic
  (workorder needs to notify about things it doesn't otherwise depend on).
  In Java, resolve the cycle properly — e.g. `workorder` publishes a
  Spring application event on lifecycle transitions, and a separate
  notification listener in `modules.crm` (or `platform.notification`)
  reacts to it, rather than injecting a callback interface to break a
  circular bean dependency.
