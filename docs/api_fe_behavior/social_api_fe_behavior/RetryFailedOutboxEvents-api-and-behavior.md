# Retry Failed Outbox Events – API & Behavior

## 1. Business Goal
Tự động retry các outbox event Social Service bị lỗi hoặc kẹt ở `PENDING` quá lâu, đảm bảo không mất thông điệp trong mô hình event-driven (at-least-once delivery).

## 2. API Contract

**Internal scheduled job** — không có endpoint FE.

### Scheduler config

| Config key | Default | Mô tả |
|------------|---------|-------|
| `social.outbox.retry.enabled` | `false` | Bật/tắt job retry. |
| `social.outbox.retry.cron` | `0 */1 * * * *` | Lịch chạy (mỗi phút). |
| `social.outbox.retry.max-retries` | `5` | Ngưỡng `retry_count`; từ ngưỡng trở lên không pick nữa. |
| `social.outbox.retry.pending-timeout-seconds` | `300` | `PENDING` cũ hơn N giây được coi là stuck và retry. |
| `social.outbox.retry.batch-size` | `50` | Số event tối đa mỗi lần poll. |

Env: `SOCIAL_OUTBOX_RETRY_ENABLED`, `SOCIAL_OUTBOX_RETRY_CRON`, ...

## 3. Response – Success

Quan sát qua log (không có HTTP body):

```
Outbox retry publish success. outboxEventId=..., eventType=POST_LIKED, aggregateId=..., retryCount=2, newStatus=PUBLISHED
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

**Không** pick: `PUBLISHED`, `PROCESSING` (trừ khi đã qua claim từ lane khác), event đã `retry_count >= max_retries`.

### Processing

1. Claim batch (`FOR UPDATE SKIP LOCKED`) → `PROCESSING`
2. Publish qua `OutboxEventPublisher` (cùng abstraction với `PublishSocialEvents`)
3. Success → `PUBLISHED`
4. Failure → `FAILED`, tăng `retry_count`, cập nhật `last_error`

### State machine (retry lane)

`FAILED` hoặc timeout `PENDING` → `PROCESSING` → `PUBLISHED` (success)  
`FAILED` hoặc timeout `PENDING` → `PROCESSING` → `FAILED` (failure, `retry_count++`)

### Phân vai với Publish worker

| Worker | Mục đích |
|--------|----------|
| `PublishSocialEvents` (cron nhanh) | Publish `PENDING` mới ngay |
| `RetryFailedOutboxEvents` (cron chậm hơn) | Retry `FAILED` + `PENDING` bị kẹt |

## 6. Edge Cases

- **Scheduler disabled:** Không chạy retry; event `FAILED` giữ nguyên.
- **Publish job và retry job cùng bật:** `PENDING` mới do publish job xử lý; retry chỉ đón `PENDING` quá timeout hoặc `FAILED`.
- **Vượt max retries:** Event ở `FAILED` vĩnh viễn (MVP) — cần can thiệp vận hành / FR sau.
- **At-least-once:** Consumer Notification phải idempotent.

## 7. Data Dependencies

| Storage | Table | Action |
|---------|-------|--------|
| PostgreSQL | `outbox_events` | Read claim, update status / retry_count / last_error / published_at |

## 8. FE Integration Notes

- FE không gọi API retry trực tiếp trong MVP.
- Dashboard admin (nếu có sau) chỉ hiển thị aggregate counts, không expose raw payload nhạy cảm.
- Tham chiếu: `PublishSocialEvents-api-and-behavior.md`, `docs/business_flow/social_business_flow/outbox-event-flow.md`.
