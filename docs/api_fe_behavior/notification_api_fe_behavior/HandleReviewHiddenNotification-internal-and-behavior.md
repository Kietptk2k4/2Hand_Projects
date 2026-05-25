# Handle Review Hidden Notification – Internal & Behavior

## 1. Business Goal

Thông báo **review author** (và **seller** tùy chọn) khi Admin publish `REVIEW_HIDDEN`: **in-app only** theo MVP default policy. Không push/email. Notification Service không mutate review trên Commerce.

## 2. Trigger

- Kafka: `admin.review.hidden`
- Internal ingest: `eventType` = `REVIEW_HIDDEN`

## 3. Flow

1. **Ingest:** `AdminReviewModerationPayloadNormalizer` map `author_id` → `review_author_id`, `reason` → `hidden_reason` (user-safe); loại `hidden_by`, `note`, `moderation_log_id`.
2. **Worker:** `ReviewHiddenNotificationEventHandler` (`@Order(43)`) — in-app cho từng recipient.
3. **Reference:** `REVIEW/{review_id}`.
4. Seller (nếu có và khác author) dùng template variant `seller`.

## 4. Admin Payload (producer)

```json
{
  "review_id": "<uuid>",
  "review_author_id": "<uuid>",
  "seller_user_id": "<uuid>",
  "reason": "Inappropriate content",
  "reason_code": "POLICY_VIOLATION",
  "hidden_by": "<admin-uuid>"
}
```

- `review_id` bắt buộc.
- Cần ít nhất một trong `review_author_id` hoặc `seller_user_id`.
- `hidden_by`, `note`, `internal_note` không lưu/hiển thị.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | In-app delivered cho một hoặc nhiều recipient |
| `FAILED` + `PERMANENT` | Thiếu recipient hoặc `review_id` |
| `FAILED` + `RETRYABLE` | Lỗi DB settings |
| `NO_OP` | Tất cả recipient tắt in-app |

Push **không** gửi (`NotificationDefaultChannelPolicy`: in-app only).

## 6. Security

- `AccountEnforcementEmailReasonPolicy` sanitize `hidden_reason`.
- Metadata không chứa ghi chú admin nội bộ.

## 7. Related FR

- `FR_HandleProductRemovedNotification` — admin moderation khác.
- Generic `PushNotificationEventHandler` **loại trừ** `REVIEW_HIDDEN`.

## 8. FE / Client

- Deep link: `reference_type=REVIEW`, `reference_id={review_id}`.
- Metadata: `hidden_reason`, `review_id`.
