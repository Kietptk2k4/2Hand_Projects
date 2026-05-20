# Functional Requirement - Store Notification Event

## 1. Feature Overview

Persist consumed producer event into `notification_events` as durable internal queue record.

## 2. Actors

- **Notification Consumer:** Stores event.
- **Notification DB:** Enforces idempotency.

## 3. Scope

**In Scope:**

- Insert `notification_events`.
- Deduplicate by producer event id or event key.
- Store sanitized payload.

**Out of Scope:**

- Event processing.
- Push/email delivery.

## 4. Database Contract

Insert fields:

- `source_event_id`
- `event_key`
- `event_type`
- `source_service`
- `aggregate_type`
- `aggregate_id`
- `actor_id`
- `recipient_user_id` when single recipient is known
- `payload`
- `status = PENDING`
- `retry_count = 0`

## 5. Business Rules

- Unique `(source_service, source_event_id)` if `source_event_id` exists.
- Unique `(source_service, event_key)` if `event_key` exists.
- Duplicate insert is treated as successful ingestion.
- Missing both `source_event_id` and `event_key` should fail unless event type has another deterministic idempotency key.

## 6. Transaction

- Insert/dedup should run in one short DB transaction.

## 7. Security

- Payload must be sanitized before persist when possible.
- `last_error` must not include sensitive values.

## 8. Failure Cases

- Unique conflict -> success/idempotent.
- DB unavailable -> fail and trigger broker redelivery.
- Invalid payload -> failed/rejected by policy.

## 9. Acceptance Criteria

- Valid event is stored with `PENDING`.
- Same producer event stores only one row.
- Event is queryable by status for processing worker.

