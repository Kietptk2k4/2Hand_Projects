# Functional Requirement - Ensure Notification Idempotency

## 1. Feature Overview

Ensure Notification Service can safely consume duplicate events and retry failed processing without creating duplicate user-visible notifications.

## 2. Actors

- **Notification Consumer:** Deduplicates ingestion.
- **Notification Worker:** Processes idempotently.
- **Notification DB:** Enforces unique keys.

## 3. Scope

**In Scope:**

- Event-level idempotency.
- User notification-level idempotency.
- Retry-safe processing.
- Duplicate conflict handling.

**Out of Scope:**

- Exactly-once broker delivery.
- Provider-level exactly-once email/push guarantees.

## 4. Business Rules

- Same `(source_service, source_event_id)` creates at most one `notification_events`.
- Same `(source_service, event_key)` creates at most one event if `source_event_id` absent.
- Same `(notification_event_id, user_id, type, reference_type, reference_id)` creates at most one user notification.
- Duplicate insert conflict is treated as successful idempotent outcome.
- Completed events are terminal in MVP.
- Retry after worker crash must be safe.

## 5. Database Impact

Requires/uses unique indexes:

- `uq_notification_events_source_event`
- `uq_notification_events_event_key`
- `uq_user_notifications_event_recipient_reference`

## 6. Failure Cases

- Missing idempotency key -> reject/fail event.
- Worker crash after partial insert -> retry hits unique conflict and proceeds.
- Duplicate payment success/order announcement -> no duplicate visible notification.

## 7. Security

- Idempotency keys must not encode secrets.
- Error details on conflicts should not leak payload.

## 8. Acceptance Criteria

- Duplicate broker delivery does not duplicate notifications.
- Event retry after partial success completes safely.
- System announcement fan-out remains one notification per user.
- Payment success duplicates do not notify buyer twice.

