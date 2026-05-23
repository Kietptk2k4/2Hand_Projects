# Publish Admin Events – API & Behavior

## 1. Business Goal

Worker poll `outbox_events` status `PENDING` (và retry qua FR_RetryAdminOutboxEvents), publish lên message broker theo Outbox Pattern sau khi transaction domain đã commit.

## 2. API Contract

**Internal worker** — không có endpoint FE.

### Scheduler

| Config | Default | Mô tả |
|--------|---------|--------|
| `admin.outbox.publish.enabled` | `false` | Bật job publish |
| `admin.outbox.publish.cron` | `0/10 * * * * *` | Lịch chạy |
| `admin.outbox.publish.max-retries` | `5` | `retry_count` tối đa trước khi bỏ qua |
| `admin.outbox.publish.batch-size` | `50` | Batch mỗi lần poll |

Env: `ADMIN_OUTBOX_PUBLISH_ENABLED`, `ADMIN_OUTBOX_PUBLISH_CRON`, …

## 3. Publish Flow

1. `claimPublishCandidates` — `PENDING`, `retry_count < max_retries`, `FOR UPDATE SKIP LOCKED`
2. Mark `PROCESSING`
3. `OutboxEventPublisher.publish` (MVP: logging stub; production: Kafka)
4. Success → `PUBLISHED` + `published_at`, clear `last_error`
5. Failure → `FAILED`, `retry_count++`, `last_error`

## 4. Message Envelope (broker)

```json
{
  "event_id": "uuid",
  "event_type": "USER_SUSPENDED",
  "event_key": "admin.user.suspended:{aggregateId}",
  "aggregate_id": "uuid",
  "source": "admin",
  "occurred_at": "2026-05-19T10:00:00Z",
  "payload": { }
}
```

Consumer dedupe bằng `event_id` (at-least-once publish).

## 5. Topic Mapping

| event_type | Topic |
|------------|-------|
| USER_SUSPENDED | admin.user.suspended |
| USER_RESTRICTED | admin.user.restricted |
| PRODUCT_REMOVED | admin.product.removed |
| REVIEW_HIDDEN | admin.review.hidden |
| SHOP_SUSPENDED | admin.shop.suspended |
| SYSTEM_CONFIG_UPDATED | admin.config.updated |
| SYSTEM_ANNOUNCEMENT_PUBLISHED | admin.announcement.published |
| … | Xem `AdminOutboxTopicResolver` |

## 6. Business Rules

- Domain change + `InsertAdminOutboxEventUseCase` trong **cùng transaction**.
- Không publish trước commit.
- Payload không chứa password/token/secret (`OutboxPublishPayloadGuard`).
- Publish chỉ sau broker/publisher ACK (stub: method return = ACK).
- Unsupported `event_type` → publish fail → `FAILED`.

## 7. Write Side (companion)

`InsertAdminOutboxEventUseCase` — insert `PENDING` row:

```java
insertAdminOutboxEventUseCase.execute(
    new InsertAdminOutboxEventCommand("USER_SUSPENDED", userId, payloadJson)
);
```

## 8. Logs

Success:

```
Outbox publish success. outboxEventId=..., eventType=USER_SUSPENDED, topic=admin.user.suspended, aggregateId=..., newStatus=PUBLISHED
```

Failure:

```
Outbox publish failed. ... newStatus=FAILED, error=...
```

## 9. Data Dependencies

| Table | Action |
|-------|--------|
| `outbox_events` | READ claim, UPDATE status/published_at/retry_count/last_error |

## 10. Edge Cases

- Job disabled → events giữ `PENDING`
- Lỗi 1 event không chặn batch còn lại
- `retry_count >= max_retries` → không claim bởi publish job (retry job xử lý FAILED)
