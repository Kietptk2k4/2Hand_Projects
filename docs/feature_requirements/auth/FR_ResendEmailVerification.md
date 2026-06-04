# Functional Requirement (FR) - Resend Email Verification

## 1. Feature Overview

Cho phép Guest/User yêu cầu **gửi lại mã OTP 6 chữ số** khi đăng ký nhưng chưa verify hoặc OTP cũ đã hết hạn. Hệ thống sinh OTP mới (hash lưu DB), vô hiệu hóa OTP `EMAIL_VERIFY` cũ chưa dùng (nếu có) và ghi outbox event để Notification Service gửi email chứa OTP (không verification link).

Mục tiêu:

- Hỗ trợ luồng kích hoạt tài khoản sau `FR_Register_Email`.
- Giảm tỷ lệ user kẹt ở `PENDING_VERIFICATION`.
- Chống spam resend bằng rate limit.

## 2. Actors

- **Guest/User:** Người đang chờ xác thực email.
- **System:** Auth Service + Outbox Worker + Notification Service.

## 3. Scope

- **In Scope:**
  - Nhận email (MVP: public, theo email đăng ký).
  - Sinh **OTP 6 chữ số** mới trong `VERIFICATION_TOKENS` với `type = EMAIL_VERIFY`.
  - Invalidate OTP `EMAIL_VERIFY` cũ chưa `used_at` của cùng user.
  - Ghi outbox `EMAIL_VERIFICATION_REQUESTED` (payload có `verification_code`).
  - Trả response anti-enumeration.
- **Out of Scope:**
  - Verify OTP (`FR_Verify_Email.md`).
  - Đổi email account.
  - Resend SMS/phone verification.

## 4. Preconditions

- Outbox worker publish event được bật.
- Notification Service consume `EMAIL_VERIFICATION_REQUESTED` và gửi email OTP.

## 5. Business Rules

- Chỉ user `PENDING_VERIFICATION` được sinh OTP mới (nếu email tồn tại).
- User `ACTIVE` / `SUSPENDED` / `DELETED`: không sinh OTP; vẫn trả `200` thông điệp chung (anti-enumeration).
- OTP mới có TTL ngắn (ví dụ 15 phút, theo config).
- OTP chỉ dùng 1 lần khi verify (`FR_Verify_Email.md`).
- `VERIFICATION_TOKENS` + `OUTBOX_EVENTS` trong cùng transaction.
- Rate limit theo IP + email (ví dụ tối đa 3 lần / 15 phút / email).
- Không log raw OTP.

## 6. API Contract

**Endpoint:** `POST /api/v1/auth/resend-email-verification`

**Auth:** Public (không bắt buộc JWT).

**Request body:**

```json
{
  "email": "user@example.com"
}
```

**Response - 200 OK (luôn dùng khi format hợp lệ):**

```json
{
  "code": 200,
  "success": true,
  "message": "Neu email hop le va chua xac thuc, chung toi da gui lai ma xac thuc.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

**Response - 400 / 429:** validation email hoặc rate limit.

## 7. Validation Rule

| Field | Type | Required | Rules |
| :--- | :--- | :--- | :--- |
| `email` | string | Yes | Định dạng email hợp lệ, max 255 ký tự |

## 8. Workflow

1. Client gửi email.
2. Validate format → `400` nếu sai.
3. Rate limit → `429` nếu vượt ngưỡng.
4. Tra cứu `USERS` theo email normalized.
5. Nếu user `PENDING_VERIFICATION`:
   - Vô hiệu hóa OTP `EMAIL_VERIFY` cũ chưa dùng.
   - Insert OTP mới + outbox `EMAIL_VERIFICATION_REQUESTED`.
6. Trả `200` thông điệp chung (kể cả email không tồn tại / đã ACTIVE).

## 9. Database Impact

- **VERIFICATION_TOKENS:** insert OTP mới (hash); có thể update OTP cũ.
- **OUTBOX_EVENTS:** insert `EMAIL_VERIFICATION_REQUESTED` khi sinh OTP thành công.

## 10. Transaction & Consistency

- OTP + outbox trong 1 transaction.
- Email gửi bất đồng bộ qua worker.

## 11. Security

- Anti-enumeration: không phân biệt email tồn tại / đã verify.
- Rate limit IP + email.
- Không log OTP.

## 12. FE Behavior

- Màn "Chờ xác thực email" / Verify Email có nút **"Gửi lại mã OTP"**.
- **Countdown 60–120 giây** sau mỗi lần resend thành công trước khi cho phép bấm lại (khuyến nghị 90s).
- Ô nhập email đăng ký (pre-fill nếu chuyển từ Register).
- Gọi `POST /api/v1/auth/resend-email-verification` với `{ "email": "..." }`.
- Hiển thị thông báo chung sau `200` (không tiết lộ email có tồn tại hay không).
- Link quay về Login.

## 13. Acceptance Criteria

- **AC1:** Email thuộc user `PENDING_VERIFICATION` → OTP mới + outbox event.
- **AC2:** Email không tồn tại hoặc user `ACTIVE` → vẫn `200`, không lộ thông tin.
- **AC3:** Rate limit khi spam resend.
- **AC4:** OTP cũ chưa dùng bị thay thế.
- **AC5:** Không log OTP trong logs.

## 14. Related

- `FR_Register_Email.md`, `FR_Verify_Email.md`
- `docs/kafka/kafka_section_email_otp.md`
