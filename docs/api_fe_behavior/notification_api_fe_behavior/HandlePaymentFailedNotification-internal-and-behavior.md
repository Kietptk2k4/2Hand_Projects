# Handle Payment Failed Notification – Internal & Behavior

## 1. Business Goal

Thông báo **buyer** khi Commerce publish `PAYMENT_FAILED` (alias `COMMERCE_PAYMENT_FAILED`): in-app + push. **Không gửi email** theo MVP default policy.

## 2. Trigger

- Kafka: `commerce.payment.failed`
- Internal ingest: `eventType` = `PAYMENT_FAILED` hoặc `COMMERCE_PAYMENT_FAILED`

## 3. Flow

1. **Ingest:** `CommercePaymentFailedPayloadNormalizer` strip secret/provider fields; map `failure_reason` → `user_failure_reason` (user-safe); `order_id` → `order_code` khi thiếu.
2. **Worker:** `PaymentFailedNotificationEventHandler` (`@Order(38)`) — in-app + push cho buyer.
3. **Reference:** `PAYMENT/{payment_id}` hoặc `ORDER/{order_id}`.

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "payment_id": "<uuid>",
  "order_id": "<uuid>",
  "order_code": "ORD-100",
  "failure_reason": "Insufficient balance",
  "reason_code": "INSUFFICIENT_FUNDS"
}
```

- `buyer_id` **bắt buộc**.
- `payment_id` hoặc `order_id` **bắt buộc**.
- `failure_reason` tùy chọn; nội dung provider/internal bị loại hoặc thay bằng `reason_code` an toàn.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered |
| `FAILED` + `PERMANENT` | Thiếu `buyer_id`, thiếu reference |
| `FAILED` + `RETRYABLE` | Lỗi DB settings, push provider |
| `NO_OP` | User tắt in-app + push |

Email channel **tắt** mặc định (`NotificationDefaultChannelPolicy`: in-app + push, no email).

## 6. Security

- Metadata lưu `user_failure_reason`, không lưu `failure_reason` thô / `provider_secret` / `raw_webhook`.
- `PaymentFailedReasonPolicy` chặn từ khóa nội bộ (stripe, webhook, stacktrace, …).

## 7. Related FR

- `FR_HandlePaymentSuccessNotification` / `FR_SendPaymentSuccessEmail` — payment success flows.
- Generic `PushNotificationEventHandler` **loại trừ** `PAYMENT_FAILED`.

## 8. FE / Client

- Hiển thị `user_failure_reason` từ notification metadata nếu cần chi tiết; title/content mặc định từ template.
