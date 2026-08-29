# Định hướng tổ chức package cho ERP (CRM + các module tương lai)

Root package: `com.qs.Backend`.

## Tiêu chí phân loại (duy nhất)

Một domain có mang ý nghĩa nghiệp vụ cụ thể của một module ERP (CRM, Inventory, Sales...) không?
- Có → đặt trong `com.qs.Backend.modules.{module}.{domain}`
- Không (hạ tầng/tiện ích chung, module ERP nào cũng dùng y hệt, không mang ngữ nghĩa nghiệp vụ) → đặt trong `com.qs.Backend.platform.{domain}`
- Kernel domain-agnostic, không phụ thuộc Spring/domain nào (error codes, response envelope, value object dùng chung) → `com.qs.Backend.shared.{domain}`

Không có nhóm lửng lơ nào khác. Khi thêm domain mới, tự hỏi: "nếu mai có thêm module Kế toán/Kho/Bán hàng, domain này có tái dùng y hệt không?" → có thì `platform/`, không thì `modules/{module}/`.

Chiều phụ thuộc (một chiều):

```
modules/*  →  platform/  →  shared/
```

- `platform/` không bao giờ import ngược vào `modules/*`.
- `shared/` không phụ thuộc `platform/` lẫn `modules/*`.
- Module này muốn dùng domain của module khác thì đi qua service/interface public, không import thẳng entity/repository nội bộ của module kia.

## Cấu trúc package hiện tại (đã refactor)

```
com.qs.Backend/
├── BackendApplication.java
│
├── shared/                          # kernel domain-agnostic, không phụ thuộc Spring context
│   ├── exception/                   # AppException, AuctionOverException, GlobalExceptionHandler
│   └── response/                    # ApiResponse (envelope chuẩn cho mọi API)
│
├── platform/                        # hạ tầng dùng chung cho MỌI module ERP
│   ├── config/                      # SecurityConfig, RedissonConfig, StorageConfig, TestConfigController
│   ├── ratelimit/                   # RateLimitFilter (bucket4j)
│   ├── file/                        # FileStorageService — lưu trữ file dùng chung
│   ├── websocket/                   # WebSocketConfig — kernel realtime
│   ├── auth/                        # định danh, đăng nhập, session, JWT
│   │   ├── controller/ dto/ entity/ repository/ security/ service/
│   ├── permission/                  # RBAC (role/permission/data scope theo tổ chức)
│   │   ├── dto/ entity/ repository/ service/
│   └── verification/                # OTP / xác thực liên hệ
│       ├── entity/ repository/ service/
│
└── modules/                         # nghiệp vụ ERP theo từng module
    ├── crm/                         # Quan hệ khách hàng (chưa triển khai)
    │   ├── customer/ customerlog/ customerview/ customergroup/ customerstatus/
    │   ├── customerportal/ lead/ businessfield/ dashboard/
    │   ├── activationrequest/ keygen/ workorder/ workordercomment/
    │   ├── servicelog/ delivery/ manualhub/ activity/
    │
    ├── inventory/                   # Kho — sản phẩm/tồn kho (chưa triển khai)
    │   ├── product/ productitem/ productitemlog/
    │   ├── warehouse/ stocklevel/ stockmovement/
    │
    ├── sales/                       # Bán hàng — báo giá, đơn hàng, hợp đồng (chưa có)
    │   ├── quotation/ salesorder/ contract/
    │
    ├── purchasing/                  # Mua hàng — NCC, đơn mua (chưa có)
    │   ├── supplier/ purchaseorder/
    │
    ├── accounting/                  # Kế toán — hoá đơn, công nợ, sổ quỹ (chưa có)
    │   ├── invoice/ payment/ ledger/
    │
    ├── hr/                          # Nhân sự (chưa có)
    │   ├── employee/ attendance/ payroll/
    │
    └── manufacturing/               # Sản xuất (chưa có)
        ├── bom/ productionorder/
```

## Mapping so với doc gốc (Go)

| Khái niệm Go (`internal/...`)         | Java (`com.qs.Backend...`)         |
|----------------------------------------|-------------------------------------|
| `internal/platform/{domain}`           | `platform.{domain}`                 |
| `internal/shared`                      | `shared.{domain}`                   |
| `internal/modules/{module}/{domain}`   | `modules.{module}.{domain}`         |
| `entity.go, ports.go, service.go...`   | `entity/`, `repository/` (port), `service/`, `dto/`, `controller/` — mỗi lớp là 1 subpackage thay vì 1 file |

## Quy ước file nội bộ mỗi domain

Mỗi domain giữ bộ subpackage chuẩn (theo mẫu `platform.auth` đang làm mẫu):

- `entity/` — JPA entity, không chứa logic nghiệp vụ phức tạp
- `dto/` — request/response DTO, tách biệt hoàn toàn với entity
- `repository/` — Spring Data JPA repository (đóng vai trò port ra ngoài persistence)
- `service/` — business logic, transaction boundary (`@Transactional`)
- `controller/` — REST endpoint, chỉ gọi service, không chứa logic nghiệp vụ
- `security/` — riêng cho `auth` (JWT, filter, principal)

## Trạng thái hiện tại của repo

- `shared/` và `platform/{config,ratelimit,file,websocket,auth,permission,verification}` đã refactor xong (2026-08-27), tương ứng `platform/system/*` trong bản kế hoạch gốc.
- `modules/crm`, `modules/inventory`, `modules/sales`, `modules/purchasing`, `modules/accounting`, `modules/hr`, `modules/manufacturing` **chưa được tạo** — chỉ tạo khi thực sự triển khai nghiệp vụ tương ứng, tránh tạo sẵn thư mục rỗng (over-engineering).
- Thứ tự triển khai đề xuất: `modules/crm/customer` trước (nghiệp vụ cốt lõi) → `modules/inventory/product` (vì `crm` cần tham chiếu sản phẩm để kích hoạt/bảo hành) → các module còn lại theo nhu cầu thực tế.
