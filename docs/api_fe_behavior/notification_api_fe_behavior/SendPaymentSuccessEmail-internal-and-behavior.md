# Send Payment Success Email – Internal & Behavior

## 1. Business Goal

Gửi email xác nhận thanh toán cho **buyer** khi Commerce publish `PAYMENT_SUCCESS` (alias `COMMERCE_PAYMENT_PAID`). Notification Service không cập nhật trạng thái payment.

## 2. Trigger

- Kafka: `commerce.payment.paid`
- Internal ingest: `eventType` = `PAYMENT_SUCCESS` hoặc `COMMERCE_PAYMENT_PAID`

## 3. Flow

1. **Ingest:** `CommerceOrderNotificationPayloadNormalizer` map email → `recipient_email`, `order_id` → `order_code`; strip secret fields; giữ `payment_method` hiển thị an toàn.
2. **Worker:** `ProcessNotificationEventUseCase` chạy nhiều handler theo `@Order`:
   - `@Order(36)` `PaymentSuccessNotificationEventHandler` — in-app + push.
   - `@Order(37)` `PaymentSuccessEmailNotificationEventHandler` — email buyer.
3. **Template:** `PAYMENT_SUCCESS` — `recipient_email`, `order_code`, tùy chọn `payment_summary_line` (amount + payment method).

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

- `recipient_email` (hoặc `email`) **bắt buộc** cho email.
- `order_code` hoặc `order_id` **bắt buộc** cho subject/body.
- `payment_id` hoặc `order_id` **bắt buộc** cho validation handler (reference).

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | Email sent hoặc skipped cùng các handler khác |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id`, `recipient_email`, `order_code` |
| `FAILED` + `RETRYABLE` | Provider timeout |
| Skip | `NOTIFICATION_EMAIL_ENABLED=false` hoặc user tắt `allow_email` |

## 6. Security

- Không đưa `provider_secret`, `raw_webhook`, … vào payload lưu DB hoặc email.
- Amount/method chỉ từ payload Commerce đã tin cậy.

## 7. Related FR

- `FR_HandlePaymentSuccessNotification` — in-app + push.
- Generic `EmailNotificationEventHandler` **loại trừ** `PAYMENT_SUCCESS`.

## 8. FE / Client

- Buyer nhận email; chi tiết payment trên Commerce UI.
