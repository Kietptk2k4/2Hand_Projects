# Send Order Confirmation Email – Internal & Behavior

## 1. Business Goal

Gửi email xác nhận đơn hàng cho **buyer** khi Commerce publish `ORDER_CREATED` (hoặc alias `COMMERCE_ORDER_CREATED`). Notification Service không thay đổi trạng thái đơn hàng.

## 2. Trigger

- Kafka: `commerce.order.created` (tương lai)
- Internal ingest (dev/test): `eventType` = `ORDER_CREATED` hoặc `COMMERCE_ORDER_CREATED`

## 3. Flow

1. **Ingest:** `CommerceOrderNotificationPayloadNormalizer` map `email`/`buyer_email` → `recipient_email`, `order_id` → `order_code` khi thiếu; loại `payment_method` khỏi payload lưu DB.
2. **Worker:** `ProcessNotificationEventUseCase` chạy **nhiều handler** theo `@Order`:
   - `@Order(34)` `OrderCreatedNotificationEventHandler` — in-app + push (buyer + sellers).
   - `@Order(35)` `OrderConfirmationNotificationEventHandler` — email buyer qua `SendEmailNotificationUseCase`.
3. **Template:** `ORDER_CREATED` — `recipient_email`, `order_code`, tùy chọn `order_summary_line` từ `final_amount`/`total_amount` (không PII nhạy cảm).

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "order_id": "<uuid>",
  "order_code": "ORD-100",
  "recipient_email": "buyer@example.com",
  "final_amount": "100000",
  "seller_ids": ["<seller-uuid>"]
}
```

- `recipient_email` (hoặc `email`) **bắt buộc** cho email.
- `order_code` hoặc `order_id` **bắt buộc** cho subject/body.
- Không đưa secret payment provider, ghi chú nội bộ seller vào email.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app/push và/hoặc email xử lý xong (handler trả `SUCCESS`/`NO_OP`) |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id`, thiếu `recipient_email`/`order_code` |
| `FAILED` + `RETRYABLE` | Lỗi DB delivery settings, provider timeout |
| Skip email | `NOTIFICATION_EMAIL_ENABLED=false` hoặc user tắt `allow_email` → handler email `NO_OP` |

Nếu handler in-app thành công nhưng email thất bại → event `FAILED` (retry toàn event; in-app idempotent).

## 6. Security

- Payload lưu DB không chứa `payment_method`.
- Seller chỉ nhận in-app/push (`templateVariant=seller`), không email qua FR này.

## 7. Related FR

- `FR_HandleOrderCreatedNotification` — kênh in-app + push.
- Generic `EmailNotificationEventHandler` **loại trừ** `ORDER_CREATED`.

## 8. FE / Client

- Commerce/Mobile không gọi Notification Service trực tiếp cho email.
- Buyer nhận email; chi tiết đơn trên Commerce UI.
