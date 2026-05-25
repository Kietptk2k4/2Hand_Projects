# Send Review Reminder Notification – Internal & Behavior

## 1. Business Goal

Gửi nhắc đánh giá **optional** cho buyer khi Commerce xác nhận order item còn reviewable và chưa có review. Notification Service không đọc Commerce DB — chỉ consume event / internal ingest.

## 2. Trigger

- Kafka: `commerce.review.reminder` → `COMMERCE_REVIEW_REMINDER` (alias `REVIEW_REMINDER`)
- Internal ingest: `eventType` = `REVIEW_REMINDER`
- Optional scheduler trên Commerce publish event với `eventKey` deterministic

## 3. Flow

1. **Ingest:** `CommerceReviewReminderPayloadNormalizer` map `item_id` → `order_item_id`, gộp `review_exists`/`has_review` → `already_reviewed`.
2. **Idempotency (event):** `eventKey` = `notification.review_reminder.{orderItemId}.{reminderDay}` (`ReviewReminderEventKeyPolicy`).
3. **Worker:** `ReviewReminderNotificationEventHandler` (`@Order(39)`).
4. **Skip:** `already_reviewed=true` → `NO_OP` (Commerce là source-of-truth).
5. **Channels:** in-app + push (respect user settings).
6. **Reference:** `PRODUCT/{product_id}` nếu có `product_id`, ngược lại `ORDER/{order_id}`.

## 4. Commerce Payload (producer)

```json
{
  "buyer_id": "<uuid>",
  "order_item_id": "<uuid-or-string>",
  "order_id": "<uuid>",
  "order_code": "ORD-500",
  "product_id": "<uuid>",
  "product_name": "Sample Product",
  "reminder_day": 7,
  "already_reviewed": false
}
```

Ingest ví dụ:

```json
{
  "eventType": "REVIEW_REMINDER",
  "sourceService": "COMMERCE",
  "eventKey": "notification.review_reminder.<orderItemId>.7",
  "aggregateType": "ORDER_ITEM",
  "aggregateId": "<orderItemId>",
  "recipientUserId": "<buyerId>",
  "payload": { ... }
}
```

- `buyer_id`, `order_item_id`, `order_id`, `reminder_day` **bắt buộc**.
- Commerce **không** publish reminder sau khi buyer đã review (`already_reviewed: true`).

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app và/hoặc push delivered |
| `COMPLETED` + `NO_OP` handler | Đã review — không tạo notification |
| `FAILED` + `PERMANENT` | Thiếu buyer / order item / order id / reminder_day |
| `FAILED` + `RETRYABLE` | Lỗi DB / push provider |
| Duplicate `eventKey` | Ingest idempotent — không duplicate event |

Email **không** gửi theo default policy.

## 6. Anti-spam

- Một `eventKey` per `(order_item_id, reminder_day)`.
- Commerce chịu trách nhiệm eligibility; Notification không query review table.

## 7. Related FR

- `FR_HandleOrderCompletedNotification` — có thể gửi `show_review_prompt` tại order complete (khác reminder deferred).
- Generic `PushNotificationEventHandler` **loại trừ** `REVIEW_REMINDER`.

## 8. FE / Client

- Deep link: `reference_type=PRODUCT|ORDER`, `reference_id` tương ứng.
- Metadata (payload đã normalize): `order_item_id`, `reminder_day`, `order_code`, `product_name`.
