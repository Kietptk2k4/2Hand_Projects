# Send Email Verification Email – Internal & Behavior

## 1. Business Goal

Gửi email xác minh khi Auth Service publish `EMAIL_VERIFICATION_REQUESTED`. Notification Service chỉ deliver link/code từ payload — không tạo verification token.

## 2. Trigger

- Kafka topic `auth.email.verification_requested` → event type `EMAIL_VERIFICATION_REQUESTED`
- Internal ingest `POST /api/v1/notification/internal/events` (dev/test)

## 3. Flow

1. **Ingest:** `EmailVerificationNotificationPayloadNormalizer` map payload Auth (`email`, `verification_token`) → `recipient_email` + `verification_link` (hoặc `verification_code` nếu không cấu hình base URL), rồi `JacksonNotificationEventPayloadSanitizer` redact sensitive keys.
2. **Worker:** `EmailVerificationNotificationEventHandler` (@Order 49) resolve `user_id` / `recipient_user_id`, gọi `SendEmailNotificationUseCase`.
3. **Delivery:** Critical override bật email dù user tắt `allow_email`; template `Verify your 2Hands email`.

## 4. Auth Payload (producer)

```json
{
  "user_id": "<uuid>",
  "email": "user@example.com",
  "verification_token": "<raw-token-from-auth>",
  "verification_token_type": "EMAIL_VERIFY"
}
```

Aliases đã hỗ trợ khi producer gửi sẵn: `recipient_email`, `verification_link`, `verify_link`, `verification_code`.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| Event `COMPLETED` | Email provider chấp nhận |
| Event `FAILED` + `PERMANENT` | Thiếu email/link/code, email invalid, template lỗi |
| Event `FAILED` + `RETRYABLE` | Thiếu recipient user id, DB settings lỗi, provider timeout |
| `NO_OP` / skip | `NOTIFICATION_EMAIL_ENABLED=false` |

## 6. Security

- Raw `verification_token` không lưu DB sau ingest; không log token/link trong `last_error`.
- Log email dạng masked (`u***@example.com`).

## 7. Configuration

```yaml
notification:
  integrations:
    email:
      enabled: ${NOTIFICATION_EMAIL_ENABLED:false}
      verification-link-base-url: ${NOTIFICATION_EMAIL_VERIFICATION_LINK_BASE_URL:https://2hands.vn/verify-email}
```

Base URL hỗ trợ placeholder `{{token}}` hoặc append `?token=` / `&token=`.

## 8. FE / Client

- Client không gọi trực tiếp flow này.
- User nhận email và submit token qua Auth `POST /api/v1/auth/verify-email`.
