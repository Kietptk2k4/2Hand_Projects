# Handle Order Completed Notification – Internal & Behavior

## 1. Business Goal

Thông báo **buyer** khi Commerce publish `ORDER_COMPLETED` (alias `COMMERCE_ORDER_COMPLETED`): in-app + push. Metadata hỗ trợ deep link order detail và prompt đánh giá (nếu Commerce gửi).

## 2. Trigger

- Kafka: `commerce.order.completed`
- Internal ingest: `eventType` = `ORDER_COMPLETED` hoặc `COMMERCE_ORDER_COMPLETED`

## 3. Flow

1. **Ingest:** `CommerceOrderCompletedPayloadNormalizer` map `order_id` → `order_code`; sanitize `completed_at`; chuyển `reviewable_item_ids` → `reviewable_item_count` + `has_reviewable_items`; giữ `show_review_prompt`.
2. **Worker:** `OrderCompletedNotificationEventHandler` (`@Order(42)`).
3. **Reference:** `ORDER/{order_id}`.
4. **Recipient:** buyer only.

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "order_id": "<uuid>",
  "order_code": "ORD-100",
  "completed_at": "2026-05-25T12:00:00Z",
  "show_review_prompt": true,
  "reviewable_item_ids": ["item-1", "item-2"]
}
```

- `buyer_id`, `order_id` **bắt buộc**.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id` hoặc `order_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB; push provider retryable |
| `NO_OP` | User tắt in-app + push |

Email **không** gửi theo MVP default policy.

## 6. Security

- Không lưu `internal_note`, settlement raw data.
- `reviewable_item_ids` không lưu nguyên mảng — chỉ count + flag.

## 7. Related FR

- `FR_HandleOrderCreatedNotification` và các FR shipment/payment commerce khác.
- Generic `PushNotificationEventHandler` **loại trừ** `ORDER_COMPLETED`.

## 8. FE / Client

- Deep link: `reference_type=ORDER`, `reference_id={order_id}`.
- Metadata: `completed_at`, `show_review_prompt`, `has_reviewable_items`, `reviewable_item_count`.
