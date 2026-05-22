# Publish Commerce Events – API & Behavior

## 1. Business Goal

Đảm bảo mọi domain event của Commerce Service được publish lên message broker một cách tin cậy sau khi giao dịch nghiệp vụ commit, thông qua **Transactional Outbox Pattern** (`outbox_events`).

## 2. API Contract

Đây là **internal worker behavior**, không có endpoint FE.

### Publish scheduler

| Config key | Default | Mô tả |
|------------|---------|-------|
| `commerce.outbox.publish.enabled` | `false` | Bật/tắt job publish pending events. |
| `commerce.outbox.publish.cron` | `0/10 * * * * *` | Lịch chạy (mỗi 10 giây). |
| `commerce.outbox.publish.max-retries` | `5` | Số lần retry tối đa (`retry_count` trước khi bỏ qua). |
| `commerce.outbox.publish.batch-size` | `50` | Số event tối đa mỗi lần poll. |

Env override: `COMMERCE_OUTBOX_PUBLISH_ENABLED`, `COMMERCE_OUTBOX_PUBLISH_CRON`, ...

### Retry scheduler (FR companion)

| Config key | Default | Mô tả |
|------------|---------|-------|
| `commerce.outbox.retry.enabled` | `false` | Bật/tắt job retry failed/stale processing. |
| `commerce.outbox.retry.cron` | `0 */1 * * * *` | Lịch chạy (mỗi phút). |
| `commerce.outbox.retry.pending-timeout-seconds` | `300` | Stale `PENDING`/`PROCESSING` được reclaim. |

## 3. Response – Success

Không có HTTP response. Quan sát qua log:

```
Outbox publish success. outboxEventId=..., eventType=COMMERCE_ORDER_CREATED, eventKey=..., aggregateId=..., newStatus=PUBLISHED
Outbox publish job completed. processedEvents=3
```

Sau publish thành công, bản ghi `outbox_events` có `status = PUBLISHED`, `published_at` được set, `last_error` cleared.

## 4. Response – Error

| Kết quả | DB state | Log |
|---------|----------|-----|
| Broker/publisher lỗi | `FAILED`, `retry_count++`, `last_error` | `Outbox publish failed...` |
| Vượt `max-retries` | Giữ `FAILED`, không được claim lại | `Outbox publish reached max retries...` |
| Event type không map topic | `FAILED` | Publisher throw unsupported event type |

## 5. Business Rules

### Ghi event (use case nghiệp vụ)

- Checkout, payment, shipment, product, review, shop moderation → insert `outbox_events` với `status = PENDING` **trong cùng transaction** với thao tác domain.
- `event_key` deterministic (vd: `order:{order_id}:created`, `payment:{payment_id}:paid`).
- `source`: `commerce` | `payment` | `shipment` tùy ngữ cảnh.
- Không đưa secret/token/credentials vào payload.

### Publish worker (`PublishCommerceEventsUseCase`)

1. Poll `PENDING` với `retry_count < max_retries` (`FOR UPDATE SKIP LOCKED`).
2. Đánh dấu `PROCESSING`.
3. Build envelope chuẩn (`event_id`, `event_type`, `event_key`, `aggregate_id`, `source`, `occurred_at`, `payload`).
4. Publish qua `OutboxEventPublisher` (MVP: logging stub; production: Kafka/RabbitMQ).
5. ACK thành công → `PUBLISHED` + `published_at`.
6. Lỗi → `FAILED`, tăng `retry_count`, ghi `last_error`.

### State machine

`PENDING` → `PROCESSING` → `PUBLISHED` (success)  
`PENDING` → `PROCESSING` → `FAILED` (error, retry qua `RetryCommerceOutboxEventsUseCase`)

## 6. Topic mapping (MVP)

| `event_type` | Kafka topic |
|--------------|-------------|
| `COMMERCE_ORDER_CREATED` | `commerce.order.created` |
| `COMMERCE_PAYMENT_CREATED` | `commerce.payment.created` |
| `COMMERCE_PAYMENT_PAID` | `commerce.payment.paid` |
| `COMMERCE_SHIPMENT_STATUS_CHANGED` | `commerce.shipment.status_changed` |
| `COMMERCE_INVENTORY_RELEASED` | `commerce.inventory.released` |
| `COMMERCE_PRODUCT_CREATED` | `commerce.product.created` |
| `COMMERCE_REVIEW_CREATED` | `commerce.review.created` |

Đầy đủ mapping: `CommerceOutboxTopicResolver`.

## 7. Edge Cases

- **Job disabled:** Scheduler no-op; event vẫn `PENDING` cho đến khi bật worker.
- **Publish batch:** Mỗi event xử lý độc lập; lỗi 1 event không chặn các event khác trong batch.
- **At-least-once:** Consumer downstream phải idempotent theo `event_key`.
- **Duplicate `event_key`:** DB unique constraint → insert outbox fail (transaction rollback).

## 8. Data Dependencies

| Storage | Table | Action |
|---------|-------|--------|
| PostgreSQL | `outbox_events` | INSERT (domain txn), UPDATE status on publish/retry |

## 9. Related FR / UC

- `docs/feature_requirements/commerce/FR_PublishCommerceEvents.md`
- `docs/business_flow/commerce_business_flow/outbox-event-flow.md`
- `docs/use_cases/commerce_use_cases/uc-event-publishing.md`
