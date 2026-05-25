# Send Password Reset Email – Internal & Behavior

## 1. Business Goal

Gửi email đặt lại mật khẩu khi Auth Service publish `PASSWORD_RESET_REQUESTED`. Notification Service chỉ deliver link/code — không tạo reset token.

## 2. Trigger

- Kafka topic `auth.password.reset_requested` → event type `PASSWORD_RESET_REQUESTED`
- Internal ingest `POST /api/v1/notification/internal/events` (dev/test)

## 3. Flow

1. **Ingest:** `AuthSecurityEmailNotificationPayloadNormalizer` map payload Auth (`email`, `verification_token` từ forgot-password) → `recipient_email` + `reset_link`, loại bỏ raw token fields, rồi sanitize.
2. **Worker:** `PasswordResetNotificationEventHandler` (@Order 48) resolve `user_id` / `recipient_user_id`, gọi `SendEmailNotificationUseCase`.
3. **Delivery:** Critical override bật email dù user tắt `allow_email`; template `Reset your 2Hands password`.

## 4. Auth Payload (producer)

```json
{
  "user_id": "<uuid>",
  "email": "user@example.com",
  "verification_token": "<raw-reset-token>",
  "verification_token_type": "PASSWORD_RESET"
}
```

Aliases khi producer gửi sẵn: `recipient_email`, `reset_link`, `password_reset_link`, `reset_code`, `reset_token`.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| Event `COMPLETED` | Email provider chấp nhận |
| Event `FAILED` + `PERMANENT` | Thiếu email/reset link, email invalid |
| Event `FAILED` + `RETRYABLE` | Thiếu recipient user id, provider timeout |
| Skip | `NOTIFICATION_EMAIL_ENABLED=false` |

## 6. Security

- Raw `verification_token` / `reset_token` không lưu DB sau ingest.
- Không log reset link/token trong `last_error` (sanitized).

## 7. Configuration

```yaml
notification:
  integrations:
    email:
      enabled: ${NOTIFICATION_EMAIL_ENABLED:false}
      password-reset-link-base-url: ${NOTIFICATION_EMAIL_PASSWORD_RESET_LINK_BASE_URL:https://2hands.vn/reset-password}
```

## 8. FE / Client

- User request forgot-password qua Auth; nhận email và submit token qua Auth reset-password API.
- Notification Service không tiết lộ email có tồn tại hay không (Auth anti-enumeration).
