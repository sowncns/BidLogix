# Design System — Backend Architecture & API

Áp dụng cho toàn bộ `com.qs.Backend`. Đi kèm [[erp_module_structure.md]] (cấu trúc package).

## 1. Kiến trúc module (module boundaries)

- **3 tầng package**: `shared` (kernel, không phụ thuộc gì) → `platform` (hạ tầng dùng chung, phụ thuộc `shared`) → `modules` (nghiệp vụ ERP, phụ thuộc `platform` + `shared`).
- **Một chiều duy nhất**: `modules → platform → shared`. Không có import ngược. Vi phạm quy tắc này là bug kiến trúc, không phải "tối ưu tạm thời".
- **Giao tiếp giữa 2 module nghiệp vụ** (vd. `sales` cần dữ liệu `inventory`): gọi qua `service` public của module kia (Spring bean), **không** import trực tiếp `entity`/`repository` nội bộ. Nếu cần tách rời hoàn toàn sau này (vd. tách microservice), interface của service chính là "port".
- **Domain package layout chuẩn** (mỗi domain trong `platform.*` hoặc `modules.*.*`):

  | Package con     | Trách nhiệm                                             | Không được làm |
  |------------------|---------------------------------------------------------|-----------------|
  | `entity/`        | JPA entity, mapping DB                                   | Business logic phức tạp, gọi service khác |
  | `dto/`           | Request/Response, validation annotation (`@NotBlank`...) | Map trực tiếp entity ra ngoài API |
  | `repository/`    | Spring Data JPA interface                                 | Business logic |
  | `service/`       | Business logic, transaction (`@Transactional`)             | Trả entity thẳng ra controller nếu DTO khác entity |
  | `controller/`    | REST endpoint, gọi service, map exception → response       | Business logic, truy vấn DB trực tiếp |
  | `security/`      | Chỉ có ở `auth`: filter, principal, JWT                     | — |

- **Không tạo abstraction thừa**: không tạo `modules/{module}` rỗng khi chưa có nghiệp vụ; không tạo interface cho service chỉ có 1 implementation trừ khi thực sự cần mock trong test hoặc có ý định thay implementation.

## 2. Response envelope

Mọi response API (thành công lẫn lỗi) đi qua `shared.response.ApiResponse` — không trả entity/DTO trần ra ngoài không bọc envelope. Envelope cố định 1 hình dạng cho toàn bộ API để FE xử lý đồng nhất.

```json
// success
{ "success": true, "data": { ... }, "message": null }

// error
{ "success": false, "data": null, "message": "Invalid credentials", "errorCode": "AUTH_INVALID_CREDENTIALS" }
```

- `errorCode` là mã ổn định (SCREAMING_SNAKE_CASE, prefix theo domain: `AUTH_`, `AUCTION_`...) để FE switch theo mã, **không** switch theo `message` (message có thể đổi theo locale).
- Toàn bộ exception nghiệp vụ kế thừa `shared.exception.AppException` (có `errorCode` + HTTP status), bắt tập trung tại `GlobalExceptionHandler` — controller không tự viết `try/catch` để format lỗi.

## 3. Quy ước REST API

- **Prefix**: `/api/v1/...` (versioning theo path, không theo header) — bump `v2` khi có breaking change ở endpoint đó, các endpoint không đổi vẫn giữ `v1`.
- **Naming**: danh từ số nhiều, kebab/lowercase, không verb trong path (`POST /customers`, không `POST /create-customer`).
- **Auth**: Bearer JWT qua header `Authorization`, danh sách route `permitAll()` khai báo tập trung ở `platform.config.SecurityConfig` — không rải rác `@PermitAll` khắp controller.
- **Phân trang**: query param `page` (0-based), `size`, response bọc thêm `pagination: { page, size, totalElements, totalPages }` bên trong `data`.
- **Lỗi validate DTO** (`@Valid` fail) → 400, `errorCode = VALIDATION_ERROR`, `message` liệt kê field lỗi.
- **Response DTO** tách riêng theo hướng đọc (vd. `CustomerListItemResponse` khác `CustomerDetailResponse`) thay vì 1 DTO to nhét mọi field rồi để null tuỳ chỗ gọi.

## 4. Data & transaction

- Migration bằng Flyway, đặt tên `V{n}__{mô_tả}.sql`, không sửa migration đã chạy — luôn thêm migration mới.
- Transaction boundary ở tầng `service`, không mở transaction ở `controller` hay `repository`.
- Entity không được để `@Transactional` lan ra ngoài lớp tạo ra nó (tránh lazy-loading exception ở tầng controller).

## 5. Khi thêm module ERP mới

1. Xác định domain nào là "lõi nghiệp vụ" của module → `modules/{module}/{domain}`.
2. Domain nào bản chất dùng chung (không đổi theo module) → đưa vào `platform/` thay vì copy lại.
3. Tạo đủ 5 subpackage chuẩn (`entity/dto/repository/service/controller`), không bỏ tầng DTO dù "chỉ là CRUD đơn giản".
4. Thêm route mới vào `SecurityConfig` nếu public, mặc định là `authenticated()`.
5. Viết migration Flyway riêng cho domain mới, không gộp chung với module khác.
