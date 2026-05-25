# Handle Payment Success Notification – Internal & Behavior

## 1. Business Goal

Thông báo **buyer** khi Commerce publish `PAYMENT_SUCCESS` (alias `COMMERCE_PAYMENT_PAID`): in-app + push theo default policy. Email: `FR_SendPaymentSuccessEmail` (`PaymentSuccessEmailNotificationEventHandler`).

## 2. Trigger

- Kafka: `commerce.payment.paid` → alias `COMMERCE_PAYMENT_PAID` → canonical `PAYMENT_SUCCESS`
- Internal ingest: `eventType` = `PAYMENT_SUCCESS` hoặc `COMMERCE_PAYMENT_PAID`

## 3. Flow

1. **Ingest:** `CommerceOrderNotificationPayloadNormalizer` map email → `recipient_email`, `order_id` → `order_code`; strip secret fields (`provider_secret`, `raw_webhook`, …); giữ `payment_method` hiển thị an toàn.
2. **Worker:** `PaymentSuccessNotificationEventHandler` (`@Order(36)`) sau alias resolve.
3. **Channels:** In-app + push cho buyer; không notify seller.
4. **Reference:** `PAYMENT/{payment_id}` nếu có `payment_id`; ngược lại `ORDER/{order_id}`.

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "payment_id": "<uuid>",
  "order_id": "<uuid>",
  "order_code": "ORD-100",
  "amount": "100000",
  "payment_method": "COD",
  "recipient_email": "buyer@example.com"
}
```

Bắt buộc: `buyer_id`, và ít nhất một trong `payment_id` / `order_id`.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered; email (nếu bật) qua handler email |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id`, thiếu `payment_id`/`order_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB settings, push provider retryable |
| `NO_OP` | User tắt hết in-app + push |

Duplicate event: idempotent qua `CreateIdempotentUserNotificationUseCase` (cùng `notification_event_id` + user + type + reference).

## 6. Security

- Không lưu provider secret / raw webhook trong payload DB.
- Buyer-only; không leak seller internal data.

## 7. Related FR

- `FR_SendPaymentSuccessEmail` — email buyer (hiện có thể qua `EmailNotificationEventHandler` generic).
- Generic push **loại trừ** `PAYMENT_SUCCESS` (dedicated commerce handler).

## 8. FE / Client

- Mobile/web đọc `user_notifications` với `type=PAYMENT_SUCCESS`, `reference_type` `PAYMENT` hoặc `ORDER`.
