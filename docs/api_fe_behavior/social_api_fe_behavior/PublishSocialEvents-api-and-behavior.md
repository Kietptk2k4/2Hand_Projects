# Publish Social Events – API & Behavior

## 1. Business Goal
Đảm bảo các domain event của Social Service (`POST_LIKED`, `COMMENT_CREATED`, `USER_FOLLOWED`) được publish lên message broker một cách tin cậy sau khi giao dịch nghiệp vụ commit, thông qua **Transactional Outbox Pattern**.

## 2. API Contract

Đây là **internal worker behavior**, không có endpoint FE.

### Scheduler

| Config key | Default | Mô tả |
|------------|---------|-------|
| `social.outbox.publish.enabled` | `false` | Bật/tắt job publish. |
| `social.outbox.publish.cron` | `0/10 * * * * *` | Lịch chạy (mỗi 10 giây). |
| `social.outbox.publish.max-retries` | `5` | Số lần retry tối đa (`retry_count` trước khi bỏ qua). |
| `social.outbox.publish.batch-size` | `50` | Số event tối đa mỗi lần poll. |

Env override: `SOCIAL_OUTBOX_PUBLISH_ENABLED`, `SOCIAL_OUTBOX_PUBLISH_CRON`, ...

## 3. Response – Success

Không có HTTP response. Quan sát qua log:

```
Outbox publish success. outboxEventId=..., eventType=POST_LIKED, aggregateId=..., newStatus=PUBLISHED
Outbox publish job completed. processedEvents=3
```

Sau publish thành công, bản ghi `outbox_events` có `status = PUBLISHED`, `published_at` được set, `last_error` cleared.

## 4. Response – Error

| Kết quả | DB state | Log |
|---------|----------|-----|
| Broker/publisher lỗi | `FAILED`, `retry_count++`, `last_error` | `Outbox publish failed...` |
| Vượt `max-retries` | Giữ `FAILED`, không được claim lại | `Outbox publish reached max retries...` |

## 5. Business Rules

### Ghi event (đã có từ use case nghiệp vụ)

- Like post, reply comment, follow user → insert `outbox_events` với `status = PENDING` **trong cùng transaction** với thao tác domain.
- Payload JSON có metadata tối thiểu (`post_id`, `user_id`, `comment_id`, `follower_id`, ...).
- `aggregate_id` = ID thực thể chính (postId, commentId, followeeId).
- Không đưa dữ liệu nhạy cảm (password, token) vào payload.

### Publish worker

1. Poll `PENDING` với `retry_count < max_retries` (`FOR UPDATE SKIP LOCKED`).
2. Đánh dấu `PROCESSING`.
3. Publish qua `OutboxEventPublisher` (MVP: logging stub; production: Kafka/RabbitMQ).
4. ACK thành công → `PUBLISHED` + `published_at`.
5. Lỗi → `FAILED`, tăng `retry_count`, ghi `last_error`.

### Topic mapping (MVP)

| `event_type` | Kafka topic |
|--------------|-------------|
| `POST_LIKED` | `social.post.liked` |
| `COMMENT_CREATED` | `social.comment.created` |
| `USER_FOLLOWED` | `social.user.followed` |

### State machine

`PENDING` → `PROCESSING` → `PUBLISHED` (success)  
`PENDING` → `PROCESSING` → `FAILED` (error, có thể retry qua FR_RetryFailedOutboxEvents)

## 6. Edge Cases

- **Job disabled:** Scheduler no-op; event vẫn `PENDING` cho đến khi bật worker.
- **Publish đúng 1 lần trong transaction batch:** Mỗi event xử lý độc lập; lỗi 1 event không chặn các event khác trong batch.
- **At-least-once:** Consumer downstream phải idempotent.
- **Unsupported event type:** Publisher throw → event `FAILED`.
- **Unlike / unfollow / idempotent follow:** Không ghi outbox (đúng nghiệp vụ).

## 7. Data Dependencies

| Storage | Table | Action |
|---------|-------|--------|
| PostgreSQL | `outbox_events` | Claim `PENDING`, update `PROCESSING` / `PUBLISHED` / `FAILED` |

Domain writes (cùng transaction khi tạo event):

| Storage | Collection/Table |
|---------|----------------|
| PostgreSQL | `post_likes`, `follows` |
| MongoDB | `comments` |

## 8. FE Integration Notes

- FE **không** gọi trực tiếp worker publish.
- Notification/in-app push do **Notification Service** consume topic tương ứng.
- Khi debug: kiểm tra `outbox_events` trong PostgreSQL social DB.
- Tham chiếu: `docs/business_flow/social_business_flow/outbox-event-flow.md`, `docs/architecture/event-driven-architecture.md`.
