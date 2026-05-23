# Consume Domain Event – Internal & Behavior

## 1. Business Goal

Notification Service nhận domain events từ Auth, Social, Commerce và Admin qua Kafka, validate envelope, rồi persist vào `notification_events` (`PENDING`) để worker xử lý sau.

## 2. Contract

### Transport

- **Broker:** Kafka (manual ack)
- **Consumer group:** `notification-domain-events` (configurable)
- **Enable flag:** `NOTIFICATION_KAFKA_CONSUMER_ENABLED=false` mặc định

### Message envelope (JSON)

| Field | Required | Notes |
|-------|----------|-------|
| `event_id` | Yes | Map sang `source_event_id` |
| `event_type` | Yes* | `UPPER_SNAKE_CASE` hoặc alias Commerce; có thể fallback theo topic |
| `source_service` | Yes* | `AUTH`, `SOCIAL`, `COMMERCE`, `ADMIN`, `SYSTEM`; có thể suy từ topic prefix |
| `event_key` | Recommended | Fallback idempotency key |
| `aggregate_type` | No | |
| `aggregate_id` | No | |
| `actor_id` | No | |
| `recipient_user_ids` | No | Phần tử đầu map sang `recipient_user_id` |
| `payload` | Yes | Object JSON; sanitize trước persist |
| `occurred_at` | No | ISO-8601 instant |

\* Có thể suy từ topic nếu thiếu trong body.

### Dev fallback

`POST /api/v1/notification/internal/events` vẫn dùng cho local ingest khi Kafka chưa bật.

## 3. Processing Flow

1. Kafka listener nhận message.
2. `DomainEventMessageParser` parse + validate envelope.
3. `NotificationEventTypeAliasResolver` map alias (vd. `COMMERCE_PAYMENT_PAID` → `PAYMENT_SUCCESS`).
4. `ConsumeDomainEventUseCase` gọi `IngestNotificationEventUseCase` → `StoreNotificationEventUseCase`.
5. Ack **sau** insert/dedup thành công hoặc khi message invalid (poison pill).

## 4. Ack Policy

| Outcome | Ack broker? |
|---------|-------------|
| Inserted `PENDING` | Yes |
| Duplicate `(source_service, source_event_id/event_key)` | Yes |
| Invalid/malformed envelope | Yes (log sanitized error) |
| DB unavailable / transient failure | No (redelivery) |

## 5. Business Rules

- Không tạo `user_notifications` tại bước consume.
- Không log raw payload có thể chứa token/OTP/secret.
- Không ack trước khi durable insert/dedup hoàn tất.
- `source_service` phải thuộc allowlist.

## 6. Edge Cases

- Duplicate broker delivery → một row `notification_events`.
- Topic fallback khi thiếu `event_type` (vd. `commerce.payment.paid`).
- Missing cả `event_id` và `event_key` → reject + ack.

## 7. Data Dependencies

- Table: `notification_events`
- Unique indexes: `uq_notification_events_source_event`, `uq_notification_events_event_key`

## 8. Ops Integration Notes

- Bật consumer: `NOTIFICATION_KAFKA_CONSUMER_ENABLED=true`, `NOTIFICATION_KAFKA_BOOTSTRAP_SERVERS=...`
- Topic list cấu hình tại `notification.kafka.consumer.topics` trong `application.yml`
- Production vẫn ưu tiên Kafka; internal API chỉ cho dev/integration test
