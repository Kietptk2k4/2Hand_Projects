# Functional Requirement (FR) - Verify Email

## 1. Feature Overview
Chức năng cho phép Guest/User kích hoạt tài khoản bằng **mã OTP 6 chữ số** đã được gửi qua email sau khi đăng ký.
Khi xác thực thành công, tài khoản chuyển từ `PENDING_VERIFICATION` sang `ACTIVE`, đồng thời đánh dấu email đã được xác thực.

## 2. Actors
- **Guest/User:** Người dùng nhận mã OTP xác thực email.

## 3. Scope
- **In Scope:**
  - Tiếp nhận mã OTP verify email (API field `token`, giá trị đúng 6 chữ số).
  - Kiểm tra OTP hợp lệ/chưa hết hạn/chưa sử dụng (so khớp hash trong `VERIFICATION_TOKENS`).
  - Cập nhật trạng thái user và đánh dấu token đã dùng.
  - Ghi outbox event để các service khác đồng bộ user đã kích hoạt.
- **Out of Scope:**
  - Gửi lại OTP — xem `FR_ResendEmailVerification.md`.
  - Đăng ký (API riêng).
  - Login (API riêng).
  - Xác thực qua link/magic link trên email.

## 4. Preconditions
- User tồn tại trong `USERS`.
- User đang ở trạng thái `PENDING_VERIFICATION`.
- Có bản ghi trong `VERIFICATION_TOKENS` với:
  - `type = EMAIL_VERIFY`
  - `used_at IS NULL`
  - `expires_at > now()`

## 5. API Contract
**Endpoint:** `POST /api/v1/auth/verify-email`

**Request Body:**
```json
{
  "token": "123456"
}
```

**Ghi chú:** Field tên `token` giữ tương thích API; giá trị bắt buộc là **OTP 6 chữ số** (`/^\d{6}$/`).

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Xac thuc email thanh cong.",
  "data": {
    "user_id": "uuid-1234-5678",
    "email_verified": true,
    "status": "ACTIVE"
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 400 Bad Request (OTP sai/hết hạn/đã dùng hoặc format không hợp lệ):**
```json
{
  "code": 400,
  "success": false,
  "message": "Token xac thuc khong hop le hoac da het han.",
  "data": null,
  "errors": [
    {
      "field": "token",
      "reason": "INVALID_OR_EXPIRED"
    }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 429 Too Many Requests:** vượt rate limit verify theo IP.

## 6. Business Rules
- OTP verify email chỉ dùng 1 lần:
  - Thành công => set `used_at = now()`.
- Tài khoản chỉ được kích hoạt khi:
  - OTP hợp lệ (6 chữ số, khớp hash, chưa hết hạn, chưa dùng)
  - User đang `PENDING_VERIFICATION`
- Sau khi verify thành công:
  - `USERS.email_verified = true`
  - `USERS.status = ACTIVE`
- Trường hợp verify lại user đã ACTIVE:
  - Có thể trả 200 idempotent với thông báo "Tai khoan da duoc xac thuc truoc do."

## 7. Database Impact
Thực thi transaction ghi vào các bảng:
1. **VERIFICATION_TOKENS** — cập nhật `used_at = now()` cho bản ghi OTP hợp lệ.
2. **USERS** — `email_verified = true`, `status = ACTIVE`, `updated_at = now()`.
3. **OUTBOX_EVENTS** — insert event kích hoạt user (`USER_UPDATED` / convention dự án), `status = PENDING`.

## 8. Transaction & Consistency
- Cập nhật `VERIFICATION_TOKENS`, `USERS`, `OUTBOX_EVENTS` trong **một transaction ACID**.
- Race condition: update có điều kiện `used_at IS NULL`; nếu 0 row affected => OTP đã dùng/không hợp lệ.

## 9. Event Flow
1. API Gateway nhận request verify email.
2. Auth Service validate OTP (format + hash compare).
3. Auth Service cập nhật token + user trong transaction.
4. Auth Service ghi outbox event.
5. Auth Service trả 200.
6. Outbox worker publish event cho các service subscribe.

## 10. Security
- Không log giá trị OTP thuần văn.
- Lưu `token_hash` trong DB; so sánh bằng hash.
- Rate limit số lần verify thất bại theo IP (giảm brute-force OTP).
- Parameterized queries/ORM chống SQL injection.

## 11. Error Handling
- `400`: OTP không hợp lệ, sai format, hết hạn, đã dùng, hoặc user không ở trạng thái cho phép verify.
- `429`: vượt rate limit verify.
- `500`: lỗi hệ thống.

## 12. FE Behavior
- Màn hình Verify Email gồm:
  - Ô nhập **mã OTP 6 chữ số** (`inputMode=numeric`, `maxLength=6`).
  - Nút "Xác thực email".
  - Nút **"Gửi lại mã OTP"** → `POST /api/v1/auth/resend-email-verification` (chi tiết countdown trong `FR_ResendEmailVerification.md`).
- UX:
  - Validation inline: `/^\d{6}$/`; disable submit khi OTP rỗng/sai format.
  - Loading khi gọi API.
  - `200`: thông báo thành công, chuyển Login.
  - `400`: "Mã OTP không hợp lệ hoặc đã hết hạn."
  - Không parse `?token=` từ URL làm luồng chính.

## 13. Acceptance Criteria
- **AC1:** OTP hợp lệ, chưa hết hạn, chưa dùng => user `PENDING_VERIFICATION` → `ACTIVE`.
- **AC2:** `email_verified = true` sau verify thành công.
- **AC3:** OTP sai/hết hạn/đã dùng => `400`, không đổi `USERS`.
- **AC4:** Outbox event được tạo cùng transaction verify thành công.
- **AC5:** Không log OTP/password.

## 14. Mapping to Existing Project Docs
- `docs/use-cases/uc-user-authentication.md` (mục 3.2 Verify Email)
- `docs/business-flow/authentication-lifecycle-flow.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`
