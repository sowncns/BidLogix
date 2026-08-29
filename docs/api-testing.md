# Auth API — Test Input

Base URL (dev): `http://localhost:8081/api/v1` (đổi theo `SERVER_PORT`/`PREFIX_API` trong `.env`)

Role/permission mẫu đã seed: `ADMIN`, `MANAGER`, `ACCOUNTANT`, `STAFF`, `CUSTOMER`.

---

## Public — không cần token

### 1. Đăng ký tài khoản nội bộ (staff/admin)
`POST /auth/register`
```json
{
  "username": "admin01",
  "password": "Admin1234",
  "roleCodes": ["ADMIN"],
  "email": "admin01@example.com",
  "region": "VN"
}
```
- Password bắt buộc ≥8 ký tự, có ít nhất 1 chữ hoa + 1 số.
- `roleCodes` phải khớp role đã tồn tại (`ADMIN`, `MANAGER`, `ACCOUNTANT`, `STAFF`, `CUSTOMER`).
- **Không trả token** — tạo xong phải gọi `/auth/login` riêng.
- Response: `{ id, username, email }`

### 2. Đăng nhập
`POST /auth/login`
```json
{
  "username": "admin01",
  "password": "Admin1234"
}
```
- Response: `{ tokens: { accessToken, refreshToken, tokenType }, userId, username }`
- Lưu `accessToken` để gọi các API protected bên dưới.

### 3. Refresh token
`POST /auth/refresh?refreshToken=<refreshToken>`
- Không có body, truyền qua query param.
- Response: `{ accessToken, refreshToken, tokenType }` (refresh token cũ bị revoke, cấp cặp mới).

### 4. Quên mật khẩu
`POST /auth/forgot-password`
```json
{ "username": "admin01" }
```
- Luôn trả message thành công dù username không tồn tại (chống dò tài khoản).
- Token thật xem trong log console app: `[email:vi] Password reset link for ...`

### 5. Đặt lại mật khẩu
`POST /auth/reset-password`
```json
{
  "token": "<token lấy từ log>",
  "newPassword": "NewPass123",
  "confirmPassword": "NewPass123"
}
```
- Sau khi đổi, tất cả session cũ của account bị revoke (phải login lại).

### 6. Đăng ký khách hàng (self-register)
`POST /auth/customer-register`
```json
{
  "email": "customer01@example.com",
  "password": "Customer123",
  "fullName": "Nguyen Van A",
  "phone": "0901234567",
  "region": "VN"
}
```
- Tạo account `active=false`, role `CUSTOMER`, sinh mã OTP 6 số.
- Mã OTP xem trong log: `[email:vi] Verification code for ...`
- Nếu có `phone` và bật `ZALO_ENABLED`/`WHATSAPP_ENABLED`, mã cũng được fan-out qua đó (mặc định tắt).

### 7. Xác thực OTP đăng ký
`POST /auth/verify`
```json
{
  "email": "customer01@example.com",
  "code": "<mã 6 số lấy từ log>"
}
```
- Kích hoạt account (`active=true`) và **trả token luôn** (login-equivalent).

### 8. Gửi lại mã xác thực
`POST /auth/resend-verification`
```json
{ "email": "customer01@example.com" }
```
- Luôn trả message thành công dù email không tồn tại.

### 9. Đăng nhập qua SSO ticket
`POST /auth/sso`
```json
{ "ticket": "<ticket lấy từ API #13>" }
```
- Ticket dùng 1 lần, hết hạn sau 60 giây.

---

## Protected — cần header `Authorization: Bearer <accessToken>`

### 10. Thông tin user hiện tại
`GET /auth/me`
- Không cần body.
- Response: `{ id, username, email, phone, region, roles[], permissions[], dataScope, organizationIds[] }`

### 11. Đăng xuất
`POST /auth/logout`
- Không cần body, chỉ cần header token — revoke session gắn với access token đang dùng.

### 12. Đổi mật khẩu
`POST /auth/change-password`
```json
{
  "oldPassword": "Admin1234",
  "newPassword": "Admin5678",
  "confirmPassword": "Admin5678"
}
```
- Sau khi đổi, tất cả session của account bị revoke (kể cả session hiện tại → cần login lại).

### 13. Tạo SSO ticket
`POST /auth/sso/ticket`
- Không cần body.
- Response: `{ ticket, expiresInSeconds }` (60s) — dùng cho API #9.

---

## Ví dụ curl nhanh (đăng nhập rồi gọi /me)

```bash
BASE=http://localhost:8081/api/v1

# 1. Login, lấy accessToken
TOKEN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin01","password":"Admin1234"}' | jq -r '.data.tokens.accessToken')

# 2. Gọi API protected
curl -s $BASE/auth/me -H "Authorization: Bearer $TOKEN" | jq
```

## Mã lỗi thường gặp (`errorDetails` trong response)

| errorDetails | Ý nghĩa |
|---|---|
| `USERNAME_TAKEN` | username đã tồn tại khi register |
| `ROLE_NOT_FOUND` | roleCode không tồn tại |
| `INVALID_CREDENTIALS` | sai username/password khi login |
| `INVALID_REFRESH_TOKEN` / `SESSION_EXPIRED` | refresh token không hợp lệ/hết hạn |
| `PASSWORD_TOO_SHORT` / `PASSWORD_NO_UPPERCASE` / `PASSWORD_NO_NUMBER` / `PASSWORD_CONFIRM_MISMATCH` | vi phạm password policy |
| `PASSWORD_MISMATCH` | sai mật khẩu cũ khi change-password |
| `PASSWORD_SAME_AS_OLD` | mật khẩu mới trùng mật khẩu cũ |
| `RESET_TOKEN_INVALID` / `RESET_TOKEN_EXPIRED` / `RESET_TOKEN_USED` | lỗi khi reset-password |
| `SSO_TICKET_INVALID` / `SSO_TICKET_EXPIRED` / `SSO_TICKET_USED` | lỗi khi login qua SSO |
| `VERIFICATION_CODE_INVALID` / `VERIFICATION_CODE_EXPIRED` | lỗi khi verify OTP |
| `USER_ALREADY_EXISTS` | email đã đăng ký khi customer-register |
| `INVALID_EMAIL` | email sai định dạng |
