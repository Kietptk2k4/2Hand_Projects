# Kafka — Hạng mục 1: Outbox publisher thật

Tài liệu mô tả publish outbox lên Kafka từ **auth**, **social**, **commerce**, **admin**. Phụ thuộc [hạng mục 0](kafka_section_0.md) (broker local).

---

## Mục tiêu và phạm vi

### Làm (hạng mục 1)

- `KafkaOutboxEventPublisher` thay stub `LoggingOutboxEventPublisher` khi bật `*_KAFKA_PRODUCER_ENABLED=true`.
- Message value = **JSON envelope** (`event_id`, `event_type`, `event_key`, `occurred_at`, `payload`, …).
- Outbox scheduler (`*_OUTBOX_PUBLISH_ENABLED`) gọi publisher; thành công → `PUBLISHED`, lỗi broker → `FAILED`.
- Env mẫu trong `Services/*/.env.example`.

### Không làm (hạng mục 2+)

- Không bật `NOTIFICATION_KAFKA_CONSUMER_ENABLED`, `SOCIAL_KAFKA_CONSUMER_ENABLED`.
- Không SMTP, không verify-email API end-to-end.
- Không shared Gradle module chung giữa các service.

---

## Luồng

```text
API / use case (transaction)
  → INSERT outbox_events (PENDING)
  → commit

Scheduler (*_OUTBOX_PUBLISH_ENABLED=true)
  → claim PENDING rows
  → KafkaOutboxEventPublisher.publish()
       → resolve topic (*OutboxTopicResolver)
       → build envelope JSON (*OutboxMessageBuilder)
       → kafkaTemplate.send(topic, key, json).get(timeout)
  → mark PUBLISHED | FAILED
```

---

## File / class theo service

| Service | Config | Publisher | Message builder |
|---------|--------|-----------|-----------------|
| **auth** | `AuthKafkaProducerConfig`, `AuthKafkaProducerProperties` | `KafkaOutboxEventPublisher` | `AuthOutboxMessageBuilder`, `AuthOutboxEventKeyResolver` |
| **admin** | `AdminKafkaProducerConfig`, `AdminKafkaProducerProperties` | `KafkaOutboxEventPublisher` | `AdminOutboxMessageBuilder` (có sẵn) |
| **commerce** | `CommerceKafkaProducerConfig`, `CommerceKafkaProducerProperties` | `KafkaOutboxEventPublisher` | `CommerceOutboxMessageBuilder` (có sẵn) |
| **social** | `SocialKafkaProducerConfig`, `SocialKafkaProducerProperties` | `KafkaOutboxEventPublisher` | `SocialOutboxMessageBuilder`, `SocialOutboxEventKeyResolver` |

Stub khi producer tắt: `LoggingOutboxEventPublisher` (`@ConditionalOnProperty` `producer.enabled=false`, `matchIfMissing=true`).

---

## Biến môi trường

| Service | Bootstrap | Bật producer | Bật scheduler publish |
|---------|-----------|--------------|-------------------------|
| auth | `KAFKA_BOOTSTRAP_SERVERS` | `AUTH_KAFKA_PRODUCER_ENABLED` | `AUTH_OUTBOX_PUBLISH_ENABLED` |
| social | `KAFKA_BOOTSTRAP_SERVERS` | `SOCIAL_KAFKA_PRODUCER_ENABLED` | `SOCIAL_OUTBOX_PUBLISH_ENABLED` |
| commerce | `KAFKA_BOOTSTRAP_SERVERS` | `COMMERCE_KAFKA_PRODUCER_ENABLED` | `COMMERCE_OUTBOX_PUBLISH_ENABLED` |
| admin | `KAFKA_BOOTSTRAP_SERVERS` | `ADMIN_KAFKA_PRODUCER_ENABLED` | `ADMIN_OUTBOX_PUBLISH_ENABLED` |

**Test publish auth (local):** đặt trong `.env` (không commit):

```env
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
AUTH_KAFKA_PRODUCER_ENABLED=true
AUTH_OUTBOX_PUBLISH_ENABLED=true
```

Restart `auth-service`, gọi register hoặc resend email verification.

Producer Kafka (dev): `acks=1`, `send-timeout-ms` mặc định **10000** (`*_KAFKA_PRODUCER_SEND_TIMEOUT_MS`).

---

## Envelope JSON (value trên topic)

```json
{
  "event_id": "uuid-outbox-row",
  "event_type": "EMAIL_VERIFICATION_REQUESTED",
  "event_key": "auth.email.verification.requested:{userId}",
  "source": "auth",
  "occurred_at": "2026-06-04T08:00:00Z",
  "payload": { }
}
```

- **Kafka key:** `event_key` (commerce/admin/social/auth đều resolve key); auth fallback `event.id()` nếu không có `user_id` trong payload.
- **Topic:** `{service}.{domain}.{action}` — map trong `*OutboxTopicResolver` (xem [kafka_section_0.md](kafka_section_0.md)).
- Consumer hạng mục 2: [`ConsumeDomainEvent-internal-and-behavior.md`](../api_fe_behavior/notification_api_fe_behavior/ConsumeDomainEvent-internal-and-behavior.md), social `AuthUserEventMessageParser`.

**Admin:** `OutboxPublishPayloadGuard` chặn payload chứa field nhạy cảm (`password`, `token`, …) trước khi gửi.

**Logging:** INFO chỉ `outboxEventId`, `eventType`, `topic` — không log OTP/token trong payload.

---

## Verify

```bash
cd Infrastructure
docker compose up -d kafka kafka-ui
```

1. Bật env auth (xem bảng trên), restart auth-service.
2. `POST /api/v1/auth/register` hoặc resend verification.
3. SQL: `SELECT id, event_type, status FROM outbox_events ORDER BY created_at DESC LIMIT 5;` → `PUBLISHED`.
4. Kafka UI http://localhost:8080 → topic `auth.email.verification_requested` → message có `event_id`, `event_type`, `payload`.
5. Unit/integration tests: `./gradlew test` trong từng service (mock publisher trong integration test).

---

## Việc chưa làm (hạng mục 2+)

| Hạng mục | Nội dung |
|----------|----------|
| [2A](kafka_section_2.md) | Notification consume auth + process events (đã document; code có sẵn) |
| 2B | SMTP / MailHog, email gửi thật |
| [3A](kafka_section_3.md) | Social consume Auth user projection (`user_projections`) |
| [4A](kafka_section_4.md) | Social publish engagement → Notification in-app + push |
| [5A](kafka_section_5.md) | Commerce publish order/payment/shipment → Notification |
| 4+ | Social payload 4B, E2E 4C; Commerce payload 5B, shipment 5C, FCM prod, DLQ |

---

## Liên kết

- [kafka_section_0.md](kafka_section_0.md) — Docker broker, auto-create topic
- [kafka_section_2.md](kafka_section_2.md) — Notification consume + process (2A)
- [kafka_section_3.md](kafka_section_3.md) — Auth → Social user projection (3A)
- [kafka_section_4.md](kafka_section_4.md) — Social → Notification engagement (4A)
- [kafka_section_5.md](kafka_section_5.md) — Commerce → Notification order/payment/shipment (5A)
- [event-driven-architecture.md](../architecture/event-driven-architecture.md)
