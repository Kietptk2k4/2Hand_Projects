# Handle Shipment Shipped Notification – Internal & Behavior

## 1. Business Goal

Thông báo **buyer** khi Commerce publish `SHIPMENT_SHIPPED` (alias `COMMERCE_SHIPMENT_SHIPPED`): in-app + push theo default policy.

## 2. Trigger

- Kafka: `commerce.shipment.shipped`
- Internal ingest: `eventType` = `SHIPMENT_SHIPPED` hoặc `COMMERCE_SHIPMENT_SHIPPED`

## 3. Flow

1. **Ingest:** `CommerceShipmentNotificationPayloadNormalizer` sanitize `tracking_code`, strip carrier/internal fields.
2. **Worker:** `ShipmentShippedNotificationEventHandler` (`@Order(40)`).
3. **Reference:** `SHIPMENT/{shipment_id}`.
4. **Recipient:** buyer only (không notify seller).

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "shipment_id": "<uuid>",
  "order_id": "<uuid>",
  "tracking_code": "VN123456"
}
```

- `buyer_id`, `shipment_id` **bắt buộc**.
- `order_id`, `tracking_code` tùy chọn.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id` hoặc `shipment_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB settings; push provider retryable failure |
| `NO_OP` | User tắt in-app + push |

## 6. Security

- `tracking_code` sanitized; không lưu `carrier_raw_response`, webhook secrets.

## 7. Related FR

- `FR_HandleShipmentCreatedNotification` — tạo shipment (in-app only).
- `FR_HandleShipmentDeliveredNotification` — giao hàng.
- Generic `PushNotificationEventHandler` **loại trừ** `SHIPMENT_SHIPPED`.

## 8. FE / Client

- `user_notifications` với `type=SHIPMENT_SHIPPED`, `reference_type=SHIPMENT`.
- `tracking_code` trong metadata khi có.
