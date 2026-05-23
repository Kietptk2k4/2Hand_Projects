# Retry Admin Outbox Events – API & Behavior

## 1. Business Goal

Tự động retry các outbox event Admin bị lỗi publish hoặc kẹt ở `PENDING`/`PROCESSING` quá lâu, đảm bảo domain events moderation/enforcement không mất khi broker/network lỗi tạm thời (at-least-once delivery).

## 2. API Contract

**Internal scheduled job** — không có endpoint FE.

### Retry scheduler

| Config key | Default | Mô tả |
|------------|---------|-------|
| `admin.outbox.retry.enabled` | `false` | Bật/tắt job retry. |
| `admin.outbox.retry.cron` | `0 */1 * * * *` | Lịch chạy (mỗi phút). |
| `admin.outbox.retry.max-retries` | `5` | Ngưỡng `retry_count`; từ ngưỡng trở lên không pick nữa. |
| `admin.outbox.retry.pending-timeout-seconds` | `300` | `PENDING`/`PROCESSING` cũ hơn N giây được coi là stuck và retry. |
| `admin.outbox.retry.batch-size` | `50` | Số event tối đa mỗi lần poll. |
| `admin.outbox.retry.backoff-seconds-per-attempt` | `60` | Delay tối thiểu cho event `FAILED` trước khi retry lại (`retry_count × backoff`). |

Env override: `ADMIN_OUTBOX_RETRY_ENABLED`, `ADMIN_OUTBOX_RETRY_CRON`, `ADMIN_OUTBOX_RETRY_BACKOFF_SECONDS_PER_ATTEMPT`, …

## 3. Response – Success

Quan sát qua log (không có HTTP body):

```
Outbox retry publish success. outboxEventId=..., eventType=USER_SUSPENDED, topic=admin.user.suspended, aggregateId=..., retryCount=2, newStatus=PUBLISHED
Outbox retry job completed. processedEvents=1
```

DB: `status = PUBLISHED`, `published_at` set, `last_error` cleared.

## 4. Response – Error

| Kết quả | DB | Log |
|---------|-----|-----|
| Publish lỗi | `FAILED`, `retry_count++`, `last_error` | `Outbox retry publish failed...` |
| `retry_count >= max_retries` sau lỗi | Giữ `FAILED`, không pick auto | `Outbox retry reached max retries...` |
| Lỗi không retry được (schema/serialization/sensitive) | Giữ `FAILED`, không tăng `retry_count` | `Outbox retry skipped (non-retryable error)...` |
| Backoff chưa đủ | Revert `PROCESSING` → `FAILED` | `Outbox retry skipped (backoff not elapsed)...` (DEBUG) |

## 5. Business Rules

### Candidate selection

- `FAILED` với `retry_count < max_retries`
- `PENDING` với `created_at <= now - pending-timeout-seconds` và `retry_count < max_retries`
- `PROCESSING` với `created_at <= now - pending-timeout-seconds` và `retry_count < max_retries` (stale worker recovery)

**Không** pick: `PUBLISHED`, `PENDING` mới (do publish worker xử lý), event đã `retry_count >= max_retries`.

### Post-claim filtering (`OutboxRetryPolicy`)

- **Non-retryable** `last_error`: unsupported event type, invalid JSON payload, sensitive field, serialization errors → revert `FAILED`, không publish.
- **Backoff**: event `FAILED` (có `last_error`) chỉ retry khi `created_at + retry_count × backoff_seconds <= now`.
- Stale `PENDING`/`PROCESSING` (thường `last_error` null) không bị backoff.

### Processing

1. Claim batch (`FOR UPDATE SKIP LOCKED`) → `PROCESSING`
2. Filter theo policy; skip → `revertProcessingToFailed`
3. Publish qua `OutboxEventPublisher` (cùng abstraction với `PublishAdminEventsUseCase`)
4. Success → `PUBLISHED`, clear `last_error`
5. Failure → `FAILED`, tăng `retry_count`, cập nhật `last_error`

### Phân vai với Publish worker

| Worker | Cron mặc định | Mục đích |
|--------|---------------|----------|
| `PublishAdminEventsUseCase` | `0/10 * * * * *` | Publish `PENDING` mới ngay |
| `RetryAdminOutboxEventsUseCase` | `0 */1 * * * *` | Retry `FAILED` + stuck `PENDING`/`PROCESSING` |

## 6. Edge Cases

- **Scheduler disabled:** Không chạy retry; event `FAILED` giữ nguyên cho đến khi bật worker hoặc repair thủ công.
- **Publish + retry cùng bật:** `PENDING` mới do publish job; retry chỉ đón event quá timeout hoặc `FAILED`.
- **Worker crash sau publish, trước mark published:** Stale `PROCESSING` được retry job reclaim.
- **Vượt max retries:** Event ở `FAILED` vĩnh viễn (MVP) — cần vận hành / dead-letter sau.
- **At-least-once:** Consumer phải idempotent theo `event_id` trong envelope.

## 7. Data Dependencies

| Storage | Table | Action |
|---------|-------|--------|
| PostgreSQL | `outbox_events` | Read claim, update status / retry_count / last_error / published_at |

## 8. Related

- FR: `docs/feature_requirements/admin/FR_RetryAdminOutboxEvents.md`
- Publish worker: `PublishAdminEvents-api-and-behavior.md`
- Flow: `docs/business_flow/admin_business_flow/outbox-event-flow.md`
