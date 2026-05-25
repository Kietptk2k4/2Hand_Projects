# Handle Shipment Created Notification – Internal & Behavior

## 1. Business Goal

Thông báo **buyer** và **seller** (tùy chọn) khi Commerce publish `SHIPMENT_CREATED` (alias `COMMERCE_SHIPMENT_CREATED`). Chỉ **in-app** theo MVP default policy — không push/email mặc định.

## 2. Trigger

- Kafka: `commerce.shipment.created`
- Internal ingest: `eventType` = `SHIPMENT_CREATED` hoặc `COMMERCE_SHIPMENT_CREATED`

## 3. Flow

1. **Ingest:** `CommerceShipmentNotificationPayloadNormalizer` sanitize `tracking_code`, strip internal/carrier raw fields.
2. **Worker:** `ShipmentCreatedNotificationEventHandler` (`@Order(39)`).
3. **Reference:** `SHIPMENT/{shipment_id}` cho mọi notification.
4. **Recipients:** buyer bắt buộc; seller nếu `seller_id` có và khác buyer (template `seller`).

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "seller_id": "<uuid>",
  "shipment_id": "<uuid>",
  "order_id": "<uuid>",
  "tracking_code": "VN123456"
}
```

- `buyer_id`, `shipment_id` **bắt buộc**.
- `seller_id`, `order_id`, `tracking_code` tùy chọn.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app created cho buyer và/hoặc seller |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id` hoặc `shipment_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB delivery settings |
| `NO_OP` | User tắt in-app |

## 6. Security

- `tracking_code` được sanitize (độ dài, loại `<` `>`).
- Không lưu `internal_note`, `carrier_raw_response`, webhook secrets.

## 7. Related FR

- `FR_HandleShipmentShippedNotification`, `FR_HandleShipmentDeliveredNotification` — các bước shipment sau.
- Dedicated commerce handler; không qua generic push/email.

## 8. FE / Client

- Đọc `user_notifications` với `type=SHIPMENT_CREATED`, `reference_type=SHIPMENT`.
- `tracking_code` trong metadata nếu Commerce gửi.
