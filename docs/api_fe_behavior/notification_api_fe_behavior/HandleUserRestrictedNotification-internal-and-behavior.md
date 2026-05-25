# Handle User Restricted Notification – Internal & Behavior

## 1. Business Goal

Thông báo **target user** khi Admin publish `USER_RESTRICTED`: in-app + push theo account-critical policy. Email do `AccountEnforcementNotificationEventHandler` (`FR_SendAccountEnforcementEmail`) xử lý riêng.

## 2. Trigger

- Kafka: `admin.user.restricted`
- Internal ingest: `eventType` = `USER_RESTRICTED`

## 3. Flow

1. **Ingest:** `AccountEnforcementEmailPayloadNormalizer` map `user_id` → `target_user_id`, reason user-safe, `restricted_capabilities` → `restricted_capabilities_summary` (high-level); loại raw array và trường admin nội bộ.
2. **Worker:** `UserRestrictedNotificationEventHandler` (`@Order(45)`) — in-app + push.
3. **Email:** `AccountEnforcementNotificationEventHandler` (`@Order(47)`).
4. **Reference:** `USER_ENFORCEMENT/{enforcement_id}`.

## 4. Admin Payload (producer)

```json
{
  "user_id": "<uuid>",
  "enforcement_id": "<uuid>",
  "reason_code": "POLICY_VIOLATION",
  "description": "User-safe summary",
  "restricted_capabilities": ["POST_CREATE", "COMMENT_CREATE"],
  "expires_at": "2026-12-31T00:00:00Z",
  "enforced_by": "<admin-uuid>"
}
```

- `user_id` / `target_user_id` và `enforcement_id` **bắt buộc**.
- `restricted_capabilities` tùy chọn; lưu dưới dạng `restricted_capabilities_summary` (vd. "Creating posts, Commenting").

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered |
| `FAILED` + `PERMANENT` | Thiếu `target_user_id` hoặc `enforcement_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB / push provider |
| `NO_OP` | User tắt in-app và không có device token |

## 6. Security

- `AccountEnforcementEmailReasonPolicy` và `AccountEnforcementRestrictedCapabilitiesPolicy` chặn nội dung nội bộ.
- Không lưu `restricted_capabilities` raw sau ingest.

## 7. Related FR

- `FR_HandleUserSuspendedNotification`, `FR_SendAccountEnforcementEmail`.
- Generic `PushNotificationEventHandler` **loại trừ** `USER_RESTRICTED`.

## 8. FE / Client

- Deep link: `reference_type=USER_ENFORCEMENT`, `reference_id={enforcement_id}`.
- Metadata: `enforcement_reason`, `enforcement_expires_at`, `restricted_capabilities_summary`.
