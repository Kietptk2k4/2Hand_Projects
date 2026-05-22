# Retry Commerce Outbox Events – API & Behavior

## 1. Business Goal

Tự động retry các outbox event Commerce bị lỗi publish hoặc kẹt ở `PENDING`/`PROCESSING` quá lâu, đảm bảo domain events không mất khi broker/network lỗi tạm thời (at-least-once delivery).

## 2. API Contract

**Internal scheduled job** — không có endpoint FE.

### Retry scheduler

| Config key | Default | Mô tả |
|------------|---------|-------|
| `commerce.outbox.retry.enabled` | `false` | Bật/tắt job retry. |
| `commerce.outbox.retry.cron` | `0 */1 * * * *` | Lịch chạy (mỗi phút). |
| `commerce.outbox.retry.max-retries` | `5` | Ngưỡng `retry_count`; từ ngưỡng trở lên không pick nữa. |
| `commerce.outbox.retry.pending-timeout-seconds` | `300` | `PENDING`/`PROCESSING` cũ hơn N giây được coi là stuck và retry. |
| `commerce.outbox.retry.batch-size` | `50` | Số event tối đa mỗi lần poll. |

Env override: `COMMERCE_OUTBOX_RETRY_ENABLED`, `COMMERCE_OUTBOX_RETRY_CRON`, ...

## 3. Response – Success

Quan sát qua log (không có HTTP body):

```
Outbox retry publish success. outboxEventId=..., eventType=COMMERCE_ORDER_CREATED, eventKey=..., aggregateId=..., retryCount=2, newStatus=PUBLISHED
Outbox retry job completed. processedEvents=1
```

DB: `status = PUBLISHED`, `published_at` set, `last_error` cleared.

## 4. Response – Error

| Kết quả | DB | Log |
|---------|-----|-----|
| Publish lỗi | `FAILED`, `retry_count++`, `last_error` | `Outbox retry publish failed...` |
| `retry_count >= max_retries` sau lỗi | Giữ `FAILED`, không pick auto | `Outbox retry reached max retries...` |

## 5. Business Rules

### Candidate selection

- `FAILED` với `retry_count < max_retries`
- `PENDING` với `created_at <= now - pending-timeout-seconds` và `retry_count < max_retries`
- `PROCESSING` với `created_at <= now - pending-timeout-seconds` và `retry_count < max_retries` (stale worker recovery)

**Không** pick: `PUBLISHED`, `PENDING` mới (do publish worker xử lý), event đã `retry_count >= max_retries`.

### Processing

1. Claim batch (`FOR UPDATE SKIP LOCKED`) → `PROCESSING`
2. Build envelope và publish qua `OutboxEventPublisher` (cùng abstraction với `PublishCommerceEventsUseCase`)
3. Success → `PUBLISHED`, clear `last_error`
4. Failure → `FAILED`, tăng `retry_count`, cập nhật `last_error`

### Phân vai với Publish worker

| Worker | Cron mặc định | Mục đích |
|--------|---------------|----------|
| `PublishCommerceEventsUseCase` | `0/10 * * * * *` | Publish `PENDING` mới ngay |
| `RetryCommerceOutboxEventsUseCase` | `0 */1 * * * *` | Retry `FAILED` + stuck `PENDING`/`PROCESSING` |

## 6. Edge Cases

- **Scheduler disabled:** Không chạy retry; event `FAILED` giữ nguyên cho đến khi bật worker hoặc repair thủ công.
- **Publish + retry cùng bật:** Tránh duplicate effort — `PENDING` mới do publish job; retry chỉ đón event quá timeout hoặc `FAILED`.
- **Worker crash sau publish, trước mark published:** Stale `PROCESSING` được retry job reclaim.
- **Vượt max retries:** Event ở `FAILED` vĩnh viễn (MVP) — cần vận hành / dead-letter sau.
- **At-least-once:** Consumer phải idempotent theo `event_id` / `event_key`.

## 7. Data Dependencies

| Storage | Table | Action |
|---------|-------|--------|
| PostgreSQL | `outbox_events` | Read claim, update status / retry_count / last_error / published_at |

## 8. Related

- FR: `docs/feature_requirements/commerce/FR_RetryCommerceOutboxEvents.md`
- Publish worker: `PublishCommerceEvents-api-and-behavior.md`
- Flow: `docs/business_flow/commerce_business_flow/outbox-event-flow.md`
