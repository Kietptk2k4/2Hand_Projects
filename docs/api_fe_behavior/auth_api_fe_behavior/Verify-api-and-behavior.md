# Verify Email - API and Behavior Spec

## 1. Scope
Backend API contract và frontend behavior cho **Verify Email** (Auth Service) — **chỉ OTP 6 chữ số**, không verification link.

In scope:
- Nhận OTP 6 chữ số từ user (API field `token`)
- Validate OTP (format, hash, type, expiry, usage)
- Kích hoạt tài khoản `PENDING_VERIFICATION` → `ACTIVE`
- Đánh dấu verification token đã dùng
- Ghi outbox sau kích hoạt thành công

Out of scope:
- Resend OTP (API riêng)
- Register / login / refresh / logout
- Verify qua link trên email

## 2. Source Docs
- `docs/feature_requirements/auth/FR_Verify_Email.md`
- `docs/feature_requirements/auth/FR_ResendEmailVerification.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/kafka/kafka_section_email_otp.md`

## 3. API Contract

### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/verify-email`
- Auth: Public

### Request Body
```json
{
  "token": "123456"
}
```

### Validation
- `token`: required, đúng **6 chữ số** (`/^\d{6}$/`)

### Success Response (200)
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

### Error Responses
- `400` OTP invalid format, sai hash, hết hạn, đã dùng
- `429` verify rate limited (theo IP)
- `500` internal error

Example 400:
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

## 4. Backend Behavior (Authoritative)

1. Validate payload (`token` = 6 digits).
2. Rate limit verify attempt (IP).
3. Hash input `token`, so khớp `VERIFICATION_TOKENS` (`type = EMAIL_VERIFY`, chưa dùng, chưa hết hạn).
4. Load user; yêu cầu `PENDING_VERIFICATION`.
5. Transaction: mark token used, `email_verified=true`, `status=ACTIVE`, outbox user activation event.
6. Return 200 (idempotent nếu user đã ACTIVE + đã verify).

## 5. Database Impact
- `verification_tokens`: `used_at`
- `users`: `email_verified`, `status`, `updated_at`
- `outbox_events`: user activation event

## 6. Security Rules
- Không log raw OTP.
- Generic 400 cho OTP sai/hết hạn (anti-enumeration).
- Rate limit verify theo IP.
- HTTPS only.

## 7. FE Behavior

### Verify Screen
- Input: **OTP 6 chữ số** (`inputMode=numeric`, `maxLength=6`)
- CTA: Xác thực email
- **Gửi lại mã OTP** → `POST /api/v1/auth/resend-email-verification`; countdown **60–120s** (khuyến nghị 90s) sau mỗi lần resend thành công

### UX States
- Inline validation: `/^\d{6}$/`
- Loading on submit
- `200`: success toast → redirect Login
- `400`: "Mã OTP không hợp lệ hoặc đã hết hạn."
- `429`: thông báo thao tác quá nhanh
- Không dùng `?token=` URL làm luồng chính

## 8. Acceptance Criteria
- OTP hợp lệ + user pending → ACTIVE, token marked used
- OTP sai/hết hạn → 400, user không đổi
- Outbox cùng transaction verify thành công
- OTP không có trong logs

## 9. Prompt for Stitch (UI only)
```text
Generate a Verify Email screen for 2Hands:
- OTP input (6 digits, numeric keyboard) + Verify button
- Resend OTP button with 60-120s cooldown after success
- Inline validation and loading state
- Success: message then redirect to Login
- Error: invalid/expired OTP message
- Mobile-friendly, accessible
```
