# Send Email Notification – Internal & Behavior

## 1. Business Goal

Gửi email notification cho các event critical/system thông qua email provider, sau khi worker xử lý event và channel policy cho phép `EMAIL`.

Không có HTTP API public — use case được gọi từ worker/handler.

## 2. Trigger

- `EmailNotificationEventHandler` xử lý event có `NotificationDefaultChannelPolicy.email = true` (trừ handler riêng: `USER_CREATED`, `EMAIL_VERIFICATION_REQUESTED`, `PASSWORD_RESET_REQUESTED`, `USER_SUSPENDED`, `USER_RESTRICTED`).
- Hoặc gọi trực tiếp `SendEmailNotificationUseCase` từ handler chuyên biệt (Phase 7/8).

## 3. Flow

1. Resolve recipient từ event.
2. `ApplyNotificationDeliveryRulesUseCase` → kiểm tra `allow_email` + critical override.
3. Nếu `NOTIFICATION_EMAIL_ENABLED=false` → **SKIPPED**.
4. Resolve template theo `eventType`.
5. Extract + sanitize biến template từ payload JSON.
6. Gọi `EmailNotificationProvider` **ngoài transaction DB**.
7. Trả `SENT` / `SKIPPED` / `FAILED` với failure policy.

## 4. Supported Email Event Types (MVP)

| eventType | Required payload fields |
|-----------|-------------------------|
| `EMAIL_VERIFICATION_REQUESTED` | `recipient_email`, `verification_link` (hoặc `verification_code`) |
| `PASSWORD_RESET_REQUESTED` | `recipient_email`, `reset_link` |
| `PASSWORD_CHANGED` | `recipient_email` |
| `ORDER_CREATED` | `recipient_email`, `order_code` |
| `PAYMENT_SUCCESS` | `recipient_email`, `order_code` |
| `USER_SUSPENDED` | `recipient_email` |
| `USER_RESTRICTED` | `recipient_email` |
| `SHOP_SUSPENDED` | `recipient_email` |

Payload aliases: `email`, `buyer_email`, `verify_link`, `password_reset_link`, `order_id`, …

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `SENT` | Provider chấp nhận email |
| `SKIPPED` | Channel/policy/integration tắt — không fail event |
| `FAILED` | `RETRYABLE` (timeout/rate limit) hoặc `PERMANENT` (invalid email, missing template/vars) |

## 6. Security

- Không log OTP/token/link/raw auth headers.
- Log email dạng masked (`u***@example.com`).
- Notification Service **không** tạo verification/reset token — chỉ deliver link/code từ producer payload.

## 7. Configuration

```yaml
notification:
  integrations:
    email:
      enabled: ${NOTIFICATION_EMAIL_ENABLED:false}
      from-address: ${NOTIFICATION_EMAIL_FROM:noreply@2hands.vn}
      from-name: ${NOTIFICATION_EMAIL_FROM_NAME:2Hands}
```

MVP provider: `LoggingEmailNotificationProvider` (dev/test). Production có thể thay bằng SMTP/SendGrid adapter implement `EmailNotificationProvider`.

## 8. FE / Client Notes

- Client không gọi trực tiếp use case này.
- Auth/Commerce publish event kèm `recipient_email` và link/token do service owner tạo.
