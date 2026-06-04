# Send Email Verification Email – Internal & Behavior

## 1. Business Goal

Gửi email xác minh khi Auth Service publish `EMAIL_VERIFICATION_REQUESTED`. Notification Service chỉ deliver **mã OTP 6 chữ số** từ payload Auth — không tạo OTP, **không** build verification link.

## 2. Trigger

- Kafka topic `auth.email.verification_requested` → event type `EMAIL_VERIFICATION_REQUESTED`
- Internal ingest `POST /api/v1/notification/internal/events` (dev/test)

## 3. Flow

1. **Ingest:** `AuthSecurityEmailNotificationPayloadNormalizer` map payload Auth (`email`, `verification_code` / `verification_token`) → `recipient_email` + `verification_code` (6 digit). **Không** gọi `buildLink()` / không set `verification_link` cho event type này. Sau đó `JacksonNotificationEventPayloadSanitizer` redact sensitive keys.
2. **Worker:** `EmailVerificationNotificationEventHandler` resolve `user_id` / `recipient_user_id`, gọi `SendEmailNotificationUseCase`.
3. **Delivery:** Critical override bật email dù user tắt `allow_email`; template `Verify your 2Hands email` với `{{verification_code}}`.

## 4. Auth Payload (producer)

```json
{
  "user_id": "<uuid>",
  "email": "user@example.com",
  "verification_code": "123456",
  "verification_token": "123456",
  "verification_token_type": "EMAIL_VERIFY"
}
```

- `verification_token`: tên field legacy trên envelope Kafka; **giá trị là OTP 6 chữ số**, không phải link token dài.
- Notification persist/display: `recipient_email` + `verification_code` only.

Aliases khi producer gửi sẵn: `recipient_email`, `verification_code`.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| Event `COMPLETED` | Email provider chấp nhận |
| Event `FAILED` + `PERMANENT` | Thiếu email/OTP, email invalid, template lỗi |
| Event `FAILED` + `RETRYABLE` | Thiếu recipient user id, DB settings lỗi, provider timeout |
| `NO_OP` / skip | `NOTIFICATION_EMAIL_ENABLED=false` |

## 6. Security

- Raw `verification_token` / OTP không lưu DB sau ingest; không log OTP trong `last_error`.
- Log email dạng masked (`u***@example.com`).

## 7. Configuration

```yaml
notification:
  integrations:
    email:
      enabled: ${NOTIFICATION_EMAIL_ENABLED:false}
```

Flow email verify **OTP-only** — không cần `NOTIFICATION_EMAIL_VERIFICATION_LINK_BASE_URL`. Password reset vẫn dùng `password-reset-link-base-url` riêng (`PASSWORD_RESET_REQUESTED`).

## 8. FE / Client

- Client không gọi trực tiếp flow này.
- User nhận email, nhập OTP trên màn Verify Email, gọi Auth `POST /api/v1/auth/verify-email` với `{ "token": "123456" }`.
- Resend OTP: `POST /api/v1/auth/resend-email-verification` (xem FR Resend).
