# Functional Requirement (FR) - Publish Outbox Events

## 1. Feature Overview

Auth Service worker poll bang `OUTBOX_EVENTS`, publish event len message broker theo Outbox Pattern. Day la **publish side** cua transactional outbox; di kem `FR_RetryFailedOutboxEvents` (retry lane).

Muc tieu:

- Dam bao event domain (`USER_CREATED`, `EMAIL_VERIFICATION_REQUESTED`, `PASSWORD_CHANGED`, ...) duoc gui sau khi commit DB thanh cong.
- At-least-once delivery; consumer phai idempotent.

## 2. Actors

- **Outbox Worker (System):** Scheduler/background job trong Auth Service.
- **Message Broker:** Kafka/RabbitMQ.
- **Consumer Services:** Notification, Social, Commerce, Admin (tuy event type).

## 3. Scope

- **In Scope:**
  - Poll `OUTBOX_EVENTS` status `PENDING` (va co the `PROCESSING` stale).
  - Claim batch → `PROCESSING`.
  - Publish payload len broker.
  - Thanh cong → `PUBLISHED`, set `published_at`.
  - That bai → `FAILED`, tang `retry_count`, ghi `last_error`.
  - Topic naming convention (vi du `auth.user.created`).
- **Out of Scope:**
  - Consumer implementation.
  - UI van hanh outbox.
  - Dead-letter UI.

## 4. Preconditions

- Bang `OUTBOX_EVENTS` ton tai voi state machine: `PENDING`, `PROCESSING`, `PUBLISHED`, `FAILED`.
- Broker reachable (hoac event giu `PENDING`/`FAILED` cho retry).
- Worker enabled qua config (`AUTH_OUTBOX_PUBLISH_ENABLED=true`).

## 5. Business Rules

- Event chi duoc insert trong **cung transaction** voi domain write (register, verify, profile update, ...).
- Worker chi publish event da commit.
- Publish idempotent theo `event_id` / aggregate key phia consumer.
- Batch size gioi han (vi du 50–100 / lan poll).
- Skip-locked hoac tuong duong de tranh double publish khi nhieu worker.
- Khong publish payload chua password/token/OTP.
- Event types MVP (khong day du): `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, `EMAIL_VERIFICATION_REQUESTED`, `PASSWORD_RESET_REQUESTED`, `PASSWORD_CHANGED`.

## 6. API Contract

**Khong co public API bat buoc.** Trigger bang scheduler noi bo.

**Optional (van hanh):** `POST /api/v1/admin/outbox/publish` — chi khi co RBAC cao; khong bat buoc MVP.

## 7. State Machine

```text
PENDING -> PROCESSING -> PUBLISHED
PROCESSING -> FAILED -> (retry worker) -> PROCESSING
```

## 8. Workflow

1. Use case ghi domain + insert `OUTBOX_EVENTS` (`PENDING`) trong 1 transaction.
2. Worker poll `PENDING` (va timeout `PROCESSING`).
3. Mark `PROCESSING`.
4. Publish broker.
5. ACK → `PUBLISHED` + `published_at`.
6. NACK/exception → `FAILED`, `retry_count++`, `last_error`.

## 9. Database Impact

- Read/update `OUTBOX_EVENTS`: `status`, `published_at`, `retry_count`, `last_error`.

## 10. Transaction

- Moi event update status trong transaction ngan.
- Khong publish truoc khi domain transaction commit.

## 11. Security

- Worker dung service credentials toi broker.
- Redact secret trong `last_error`.
- Khong expose outbox payload ra public API.

## 12. Relationship with FR_RetryFailedOutboxEvents

| FR | Vai tro |
|----|---------|
| **FR_PublishOutboxEvents** | Poll `PENDING`, publish lan dau |
| **FR_RetryFailedOutboxEvents** | Retry `FAILED` / `PENDING` timeout |

Co the implement chung 1 worker voi 2 mode hoac 2 scheduler — tai lieu tach de ro nghia nghiep vu.

## 13. Acceptance Criteria

- **AC1:** Event insert cung transaction domain → eventual publish.
- **AC2:** Publish thanh cong → `PUBLISHED`.
- **AC3:** Broker down → event giu `PENDING`/`FAILED`, khong mat ban ghi.
- **AC4:** Khong co dual-write (DB commit roi publish ngoai outbox).
- **AC5:** Payload khong chua secret.

## 14. Related

- `FR_RetryFailedOutboxEvents.md`
- `docs/business_flow/auth_business_flow/outbox-event-flow.md`
- `docs/use_cases/auth_use_cases/uc-event-publishing.md`
- `docs/database/auth_schema.md` (`OUTBOX_EVENTS`)
