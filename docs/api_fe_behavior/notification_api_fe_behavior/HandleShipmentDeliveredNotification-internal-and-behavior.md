# Handle Shipment Delivered Notification – Internal & Behavior

## 1. Business Goal

Thông báo **buyer** khi Commerce publish `SHIPMENT_DELIVERED` (alias `COMMERCE_SHIPMENT_DELIVERED`): in-app + push. Metadata hỗ trợ deep link và prompt xác nhận nhận hàng/đánh giá (nếu Commerce gửi).

## 2. Trigger

- Kafka: `commerce.shipment.delivered`
- Internal ingest: `eventType` = `SHIPMENT_DELIVERED` hoặc `COMMERCE_SHIPMENT_DELIVERED`

## 3. Flow

1. **Ingest:** `CommerceShipmentNotificationPayloadNormalizer` sanitize `delivered_at`, `tracking_code`; map `show_confirm_receipt` → `prompt_confirm_receipt`; strip carrier/internal fields.
2. **Worker:** `ShipmentDeliveredNotificationEventHandler` (`@Order(41)`).
3. **Reference:** `SHIPMENT/{shipment_id}` nếu có `shipment_id`; ngược lại `ORDER/{order_id}`.
4. **Recipient:** buyer only.

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "shipment_id": "<uuid>",
  "order_id": "<uuid>",
  "delivered_at": "2026-05-25T10:00:00Z",
  "show_confirm_receipt": true,
  "show_review_prompt": false
}
```

- `buyer_id` **bắt buộc**.
- Ít nhất một trong `shipment_id` / `order_id` **bắt buộc**.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id`, thiếu reference |
| `FAILED` + `RETRYABLE` | Lỗi DB; push provider retryable |
| `NO_OP` | User tắt in-app + push |

## 6. Security

- Không lưu `carrier_raw_response`, webhook secrets.
- `delivered_at` và flags prompt được sanitize/normalize.

## 7. Related FR

- `FR_HandleShipmentCreatedNotification`, `FR_HandleShipmentShippedNotification`.
- Generic `PushNotificationEventHandler` **loại trừ** `SHIPMENT_DELIVERED`.

## 8. FE / Client

- Deep link qua `reference_type` + `reference_id` (`SHIPMENT` hoặc `ORDER`).
- Đọc `delivered_at`, `prompt_confirm_receipt`, `show_review_prompt` từ notification metadata.
