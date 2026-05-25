# Handle Product Removed Notification – Internal & Behavior

## 1. Business Goal

Thông báo **seller** khi Admin publish `PRODUCT_REMOVED`: in-app + push. **Không gửi email** theo MVP default policy. Notification Service không thay đổi trạng thái product trên Commerce.

## 2. Trigger

- Kafka: `admin.product.removed`
- Internal ingest: `eventType` = `PRODUCT_REMOVED`

## 3. Flow

1. **Ingest:** `AdminProductModerationPayloadNormalizer` map `seller_id` → `seller_user_id`, `reason` → `removal_reason` (user-safe); loại `removed_by`, `note`, `moderation_log_id`.
2. **Worker:** `ProductRemovedNotificationEventHandler` (`@Order(44)`) — in-app + push cho seller.
3. **Reference:** `PRODUCT/{product_id}`.

## 4. Admin Payload (producer)

```json
{
  "product_id": "<uuid>",
  "seller_user_id": "<uuid>",
  "reason": "Counterfeit listing",
  "reason_code": "POLICY_VIOLATION",
  "removed_by": "<admin-uuid>",
  "removed_at": "2026-05-25T12:00:00Z"
}
```

- `product_id` và `seller_user_id` (hoặc `seller_id` khi ingest) **bắt buộc** để deliver.
- `removed_by`, `note`, `internal_note` không lưu/hiển thị.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered |
| `FAILED` + `PERMANENT` | Thiếu `seller_user_id` hoặc `product_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB settings, push provider |
| `NO_OP` | User tắt in-app + push và không có device token |

## 6. Security

- `AccountEnforcementEmailReasonPolicy` chặn reason nội bộ; fallback `reason_code` khi cần.
- Metadata in-app chỉ chứa payload đã normalize.

## 7. Related FR

- `FR_HandleUserSuspendedNotification`, `FR_HandleUserRestrictedNotification` — admin enforcement khác.
- Generic `PushNotificationEventHandler` **loại trừ** `PRODUCT_REMOVED`.

## 8. FE / Client

- Deep link: `reference_type=PRODUCT`, `reference_id={product_id}`.
- Metadata: `removal_reason`, `product_id`, `product_name` (nếu producer gửi).
