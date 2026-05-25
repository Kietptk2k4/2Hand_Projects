# Handle User Suspended Notification – Internal & Behavior

## 1. Business Goal

Thông báo **target user** khi Admin publish `USER_SUSPENDED`: in-app + push theo account-critical policy. Email do `AccountEnforcementNotificationEventHandler` (`FR_SendAccountEnforcementEmail`) xử lý riêng.

## 2. Trigger

- Kafka: `admin.user.suspended`
- Internal ingest: `eventType` = `USER_SUSPENDED`

## 3. Flow

1. **Ingest:** `AccountEnforcementEmailPayloadNormalizer` map `user_id` → `target_user_id`, `description` → `enforcement_reason` (user-safe), `expires_at` → `enforcement_expires_at`; loại `enforced_by`, `note`, `internal_note`.
2. **Worker:** `UserSuspendedNotificationEventHandler` (`@Order(46)`) — in-app + push.
3. **Email:** `AccountEnforcementNotificationEventHandler` (`@Order(47)`) — account-critical email override.
4. **Reference:** `USER_ENFORCEMENT/{enforcement_id}`.

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

- `user_id` / `target_user_id` và `enforcement_id` **bắt buộc**.
- `enforced_by`, `note`, `internal_note` không lưu/hiển thị.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered (email handler có thể gửi thêm) |
| `FAILED` + `PERMANENT` | Thiếu `target_user_id` hoặc `enforcement_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB settings, push provider retryable |
| `NO_OP` | User tắt in-app và không có push token (push critical vẫn bật khi có token) |

`NotificationCriticalOverridePolicy` bật push (và email ở handler riêng) kể cả khi user tắt setting.

## 6. Security

- `AccountEnforcementEmailReasonPolicy` chặn mô tả chứa `internal`, `confidential`, `admin_only`.
- Metadata in-app chỉ chứa trường đã normalize; không expose ghi chú admin.

## 7. Related FR

- `FR_SendAccountEnforcementEmail` — email enforcement.
- `FR_HandleUserRestrictedNotification` — restricted flow (`UserRestrictedNotificationEventHandler` @Order 45).
- Generic `PushNotificationEventHandler` **loại trừ** `USER_SUSPENDED`.

## 8. FE / Client

- Deep link: `reference_type=USER_ENFORCEMENT`, `reference_id={enforcement_id}`.
- Metadata: `enforcement_reason`, `enforcement_expires_at`, `reason_code` (nếu có).
