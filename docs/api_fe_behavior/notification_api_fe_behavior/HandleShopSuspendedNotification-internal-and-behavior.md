# Handle Shop Suspended Notification – Internal & Behavior

## 1. Business Goal

Thông báo **shop owner** khi Admin publish `SHOP_SUSPENDED`: in-app + push + email theo account-critical policy. Notification Service không thay đổi trạng thái shop trên Commerce.

## 2. Trigger

- Kafka: `admin.shop.suspended`
- Internal ingest: `eventType` = `SHOP_SUSPENDED`

## 3. Flow

1. **Ingest:** `AdminShopModerationPayloadNormalizer` map `owner_id` → `shop_owner_id`, `reason` → `suspension_reason` / `enforcement_reason`, `expires_at` → `suspension_expires_at`; loại `suspended_by`, `note`.
2. **Worker:** `ShopSuspendedNotificationEventHandler` (`@Order(48)`) — in-app + push.
3. **Email:** `ShopSuspendedEmailNotificationEventHandler` (`@Order(49)`) — critical email override.
4. **Reference:** `SHOP/{shop_id}`.

## 4. Admin Payload (producer)

```json
{
  "shop_id": "<uuid>",
  "shop_owner_id": "<uuid>",
  "email": "owner@example.com",
  "reason": "Policy violation",
  "expires_at": "2026-12-31T00:00:00Z",
  "suspended_by": "<admin-uuid>"
}
```

- `shop_id` và `shop_owner_id` **bắt buộc** để deliver.
- `suspended_by`, `note`, `internal_note` không lưu/hiển thị.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app/push/email delivered (theo channel) |
| `FAILED` + `PERMANENT` | Thiếu `shop_owner_id` hoặc `shop_id`, thiếu `recipient_email` (email) |
| `FAILED` + `RETRYABLE` | Lỗi DB / provider |
| `NO_OP` | User tắt tất cả channel |

`NotificationCriticalOverridePolicy` bật push + email kể cả khi user tắt setting.

## 6. Security

- Reason sanitize qua `AccountEnforcementEmailReasonPolicy`.
- Không expose admin internal notes.

## 7. Related FR

- `FR_HandleUserSuspendedNotification`, `FR_HandleProductRemovedNotification`.
- Generic `PushNotificationEventHandler` / `EmailNotificationEventHandler` **loại trừ** `SHOP_SUSPENDED`.

## 8. FE / Client

- Deep link: `reference_type=SHOP`, `reference_id={shop_id}`.
- Metadata: `suspension_reason`, `suspension_expires_at`.
