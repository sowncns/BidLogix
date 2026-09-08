# Cấu trúc thư mục Backend (qs-crm-be)

```
qs-crm-be/
├── cmd/                          # Entry points
│   ├── server/main.go            # Main API server
│   ├── filegc/main.go            # File garbage collector job
│   ├── migrate-activity-images/main.go
│   ├── password_tool.go
│   └── test/excel.go             # (module riêng, có go.mod)
│
├── internal/
│   ├── app/                      # Composition root (DI)
│   │   ├── container.go
│   │   ├── grpc.go
│   │   ├── http.go
│   │   ├── run.go
│   │   ├── keygen_adapters.go
│   │   ├── workorder_adapters.go
│   │   └── workordercomment_adapters.go
│   │
│   ├── core/{domain}/            # Business logic thuần (mỗi domain: entity.go, errors.go, ports.go, service.go, validation.go...)
│   │   activationrequest, activity, audit, auth, businessfield,
│   │   customer, customergroup, customerlog, customerstatus,
│   │   customerview, delivery, file, keygen, lead, manualhub,
│   │   notification, organization, permission, product, productitem,
│   │   productitemlog, profile, role, servicelog, shared,
│   │   user, verification, workorder, workordercomment
│   │
│   └── adapters/
│       ├── http/{domain}/        # REST handlers (handler.go, request.go, response.go, mapper.go, respond.go, helpers.go...)
│       │   activationrequest, activity, audit, auth, businessfield,
│       │   customer, customergroup, customerlog, customerportal,
│       │   customerstatus, customersupplementalproduct, customerview,
│       │   dashboard, file, keygen, lead, manualhub, messaging,
│       │   metadata, middleware, notification, organization,
│       │   product, productitem, productitemlog, profile, role,
│       │   servicelog, user, workorder, workordercomment
│       │   + ctx/keys.go, deps.go, router.go
│       │
│       ├── postgres/             # SQL repository implementations (1 file/domain: *_repo.go)
│       ├── email/                # auditing_sender.go
│       ├── storage/              # local_file_storage.go
│       └── ws/                   # WebSocket: auth.go, conn.go, handler.go, hub.go
│
├── pkg/                          # Shared libs
│   ├── config/                   # config.go, env.go, email.go, whatsapp.go, zalo.go
│   ├── crypto/                   # password.go
│   ├── database/                 # postgres.go, executor.go, errors.go
│   ├── email/                    # service.go, sender.go (resend/smtp/mock), locale.go, subjects.go, templates/ (html en/vi)
│   ├── jwt/                      # jwt.go
│   ├── logger/                   # logger.go
│   ├── whatsapp/                 # client.go, sender.go, send_text.go, send_template.go
│   └── zalo/                     # client.go, sender.go, phone.go, token_refresh.go
│
├── migrations/                   # golang-migrate SQL (000001 → 000168, up/down)
├── seeds/                        # SQL seed data (000001 → 000013)
├── scripts/                      # delete_junk_users.sql
├── tmp/                          # build output (Air hot-reload)
├── Dockerfile, Dockerfile.dev
├── .air.toml, .air.debug.toml
└── go.mod, go.sum, .env(.example)
```

**Điểm chính:** kiến trúc Clean/Hexagonal — `core/` (logic thuần, ~29 domain) không phụ thuộc framework; `adapters/http` và `adapters/postgres` implement theo từng domain song song với `core`; `app/container.go` là nơi wiring DI duy nhất.

## Gom nhóm domain theo bounded context

Các domain hiện tại là flat, ngang hàng nhau trong `internal/core/` và `internal/adapters/http/` (đúng chuẩn hexagonal — mỗi domain độc lập, `core` chỉ phụ thuộc `core/shared`). Nhóm dưới đây chỉ để **document/trình bày** (README, sơ đồ kiến trúc), không đổi cấu trúc thư mục hay gộp code thật, vì gộp code sẽ phá vỡ nguyên tắc tách domain độc lập đã quy định trong CLAUDE.md.

- **Auth & Access**: auth, user, role, permission, profile, activationrequest, verification
- **Organization**: organization
- **Customer**: customer, customerlog, customergroup, customerstatus, customerview, customersupplementalproduct (http-only), lead
- **Product & Inventory**: product, productitem, productitemlog, keygen
- **Work Order**: workorder, workordercomment, servicelog
- **Notification & Delivery**: notification, delivery, messaging (http-only)
- **Content/Ops**: manualhub, file, activity, audit, businessfield, dashboard (http-only), metadata (http-only)

## Mapping file Go → layer Java (Controller / Service / Entity / Repo / DTO)

Mỗi domain trong Go trải trên 3 thư mục (`core/{domain}`, `adapters/http/{domain}`, `adapters/postgres/{domain}_repo.go`). Bảng dưới ánh xạ từng file sang layer tương ứng khi viết lại bằng Java (Spring):

| File Go | Layer Java | Ghi chú |
|---|---|---|
| `core/{domain}/entity.go` | **Entity** (`{Domain}.java`) | struct → class, field → field |
| `core/{domain}/errors.go` | **Exception** (`{Domain}Exception.java`, hoặc mã lỗi enum) | domain error → custom exception |
| `core/{domain}/validation.go` | **Validator** / Bean Validation (`@Valid`, annotation, hoặc `{Domain}Validator.java`) | business rule validate trước khi lưu |
| `core/{domain}/ports.go` | **Interface** (`{Domain}Repository.java`, `{Domain}Service.java`) | định nghĩa contract, Java dùng interface tương tự |
| `core/{domain}/service.go` | **Service impl** (`{Domain}ServiceImpl.java`) | business logic thuần, gọi qua interface repo |
| `adapters/postgres/{domain}_repo.go` | **Repository impl** (`{Domain}RepositoryImpl.java` hoặc JPA `{Domain}Repository extends JpaRepository`) | SQL → JPA/MyBatis |
| `adapters/http/{domain}/handler.go` | **Controller** (`{Domain}Controller.java`) | route handler → `@RestController` method |
| `adapters/http/{domain}/request.go` | **DTO Request** (`{Domain}Request.java` / `Create{Domain}Request.java`...) | payload đầu vào |
| `adapters/http/{domain}/response.go` | **DTO Response** (`{Domain}Response.java`) | payload trả về |
| `adapters/http/{domain}/mapper.go` | **Mapper** (`{Domain}Mapper.java`, dùng MapStruct) | Entity ↔ DTO |
| `adapters/http/{domain}/respond.go` | **`@ExceptionHandler` / `@ControllerAdvice`** | map domain error → HTTP status |
| `adapters/http/{domain}/helpers.go` | tiện ích riêng của controller (thường gộp vào Controller hoặc 1 `Util` class) | không có layer chuẩn, tùy nội dung |

### Thứ tự nên code lại (theo từng domain, từ trong ra ngoài)

1. **Entity** (`entity.go`) — model dữ liệu trước tiên
2. **DTO** (`request.go`, `response.go`) — payload vào/ra
3. **Repository interface + impl** (`ports.go` phần repo + `{domain}_repo.go`) — truy cập DB
4. **Service interface + impl** (`ports.go` phần service + `service.go`, kèm `validation.go`, `errors.go`)
5. **Mapper** (`mapper.go`) — nối Entity ↔ DTO
6. **Controller** (`handler.go`) — expose API, dùng `respond.go` làm cơ sở viết exception handler

### Thứ tự nên làm theo domain (từ ít phụ thuộc → nhiều phụ thuộc)

1. `organization`, `permission`, `role` (nền tảng RBAC)
2. `auth`, `user`, `profile`, `verification`, `activationrequest` (auth phụ thuộc organization/role)
3. `customer`, `customergroup`, `customerstatus`, `customerview`, `customerlog`, `lead`, `businessfield`
4. `product`, `productitem`, `productitemlog`, `keygen`
5. `workorder`, `workordercomment`, `servicelog`
6. `notification`, `delivery`, `manualhub`, `file`, `activity`, `audit`

Đi theo thứ tự này để domain sau không bị thiếu entity/service mà domain trước đã định nghĩa (ví dụ `workorder` cần `customer` và `productitem` đã có sẵn).

## Workflow refactor từng API sang Java (áp dụng cho từng domain, lặp lại)

Làm theo domain, đi hết 1 domain rồi mới sang domain kế (theo thứ tự phụ thuộc ở trên). Với mỗi domain, làm theo trình tự sau:

1. **Đọc source Go của domain đó**
   - Đọc `core/{domain}/entity.go` → liệt kê field, type, quan hệ.
   - Đọc `core/{domain}/ports.go` → liệt kê method interface (Repository + Service).
   - Đọc `adapters/http/{domain}/handler.go` → liệt kê route (method, path, permission yêu cầu).

2. **Tạo Entity**
   - Viết `{Domain}.java` (JPA `@Entity` nếu dùng Hibernate, hoặc POJO nếu dùng MyBatis).
   - Đối chiếu cột DB trong `migrations/*.sql` để map đúng tên cột, kiểu dữ liệu, nullable, FK.

3. **Tạo DTO**
   - `request.go` → `{Action}{Domain}Request.java` (vd `CreateCustomerRequest`, `UpdateCustomerRequest`).
   - `response.go` → `{Domain}Response.java`.
   - Thêm annotation validate (`@NotNull`, `@Size`...) dựa theo `validation.go`.

4. **Tạo Repository**
   - Copy method signature từ `ports.go` (phần Repository) sang interface Java.
   - Viết implementation dựa theo SQL trong `{domain}_repo.go` — giữ nguyên logic filter/scope (đặc biệt các query có áp `scope_filter.go`).

5. **Tạo Service**
   - Copy method signature từ `ports.go` (phần Service) sang interface Java.
   - Port logic từ `service.go` từng method một — chuyển early-return error của Go thành `throw` exception Java (map theo `errors.go`).
   - Port `validation.go` vào đầu method service hoặc lớp validator riêng.

6. **Tạo Mapper**
   - Port `mapper.go` → dùng MapStruct hoặc mapper thủ công, đảm bảo field mapping Entity ↔ DTO khớp 1-1.

7. **Tạo Controller**
   - Port từng route trong `handler.go` → 1 method `@RestController`.
   - Copy middleware chain (Authenticate → Authorize → ApplyScope) thành `@PreAuthorize` / interceptor tương ứng.
   - Port `respond.go` → `@ExceptionHandler` cho domain đó (hoặc gộp vào `@ControllerAdvice` chung).

8. **Test lại API**
   - So sánh request/response mẫu giữa Go (chạy thử) và Java để đảm bảo hành vi giống nhau (status code, message lỗi, cấu trúc JSON).
   - Ưu tiên test các case: not-found, permission denied, data scope filter.

9. **Đánh dấu domain đã xong**, chuyển sang domain kế tiếp trong thứ tự phụ thuộc.

> Gợi ý: làm domain nhỏ trước (`organization`, `role`, `permission`) để quen pattern, rồi mới sang domain lớn nhiều field như `customer`, `workorder`, `productitem`.
