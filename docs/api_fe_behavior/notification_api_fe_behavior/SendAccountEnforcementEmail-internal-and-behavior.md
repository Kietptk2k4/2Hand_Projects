# Send Account Enforcement Email – Internal & Behavior

## 1. Business Goal

Gửi email thông báo enforcement khi Admin Service publish `USER_SUSPENDED` hoặc `USER_RESTRICTED`. Notification Service không thay đổi trạng thái Auth/Admin.

## 2. Trigger

- Kafka: `admin.user.suspended`, `admin.user.restricted`
- Internal ingest (dev/test)

## 3. Flow

1. **Ingest:** `AccountEnforcementEmailPayloadNormalizer` map payload Admin → `target_user_id`, `recipient_email`, `enforcement_reason`, `enforcement_expires_at`; loại `enforced_by`, `note`, `description` thô.
2. **Worker:** `AccountEnforcementNotificationEventHandler` (@Order 47) → `SendEmailNotificationUseCase`.
3. **Delivery:** Account-critical override (`NotificationCriticalOverridePolicy`) bật email kể cả khi user đã tắt `allow_email` hoặc đang suspended.

## 4. Admin Payload (producer)

```json
{
  "user_id": "<uuid>",
  "enforcement_id": "<uuid>",
  "reason_code": "SPAM_ABUSE",
  "description": "User-safe reason shown to user",
  "expires_at": "2026-12-31T00:00:00Z",
  "enforced_by": "<admin-uuid>"
}
```

`enforced_by`, `note`, `internal_note` không được đưa vào email. Mô tả chứa từ khóa nội bộ (`internal`, `confidential`, …) được thay bằng reason từ `reason_code`.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | Email accepted by provider |
| `FAILED` + `PERMANENT` | Thiếu `recipient_email`, email invalid |
| `FAILED` + `RETRYABLE` | Thiếu recipient user id, provider timeout |
| Skip | `NOTIFICATION_EMAIL_ENABLED=false` |

## 6. Security

- Không expose admin internal notes trong email hoặc `last_error`.
- Suspended user vẫn nhận email account-critical.

## 7. Scope note

- `SHOP_SUSPENDED` **không** thuộc FR này — vẫn qua `EmailNotificationEventHandler` generic.
- In-app/push enforcement: `FR_HandleUserSuspendedNotification`, `FR_HandleUserRestrictedNotification`.

## 8. FE / Client

- Admin portal không gọi Notification Service trực tiếp.
- User nhận email; chi tiết enforcement trên Auth/Admin UI riêng.
