# Functional Requirement - Publish Admin Events

## 1. Feature Overview

Admin outbox worker publish cac domain/integration events cua Admin Service len message broker theo Outbox Pattern.

## 2. Actors

- **Outbox Worker:** Polls and publishes events.
- **Message Broker:** Receives events.
- **Consumer Services:** Auth, Commerce, Social, Notification.

## 3. Scope

**In Scope:**

- Poll pending outbox events.
- Publish to broker topic.
- Mark events as published.
- Track retry count and error.

**Out of Scope:**

- Consumer processing.
- Manual event editing.

## 4. Trigger

- Scheduled worker or continuous polling worker.

## 5. Business Rules

- Events are created in same transaction as domain state changes.
- Worker publishes only `PENDING` or retryable `FAILED` events.
- Publish operation must be idempotent from consumer perspective using event id.
- Event topic follows naming convention, e.g. `admin.user.enforcement`.

## 6. Database Impact

- Read `outbox_events`.
- Update status, `published_at`, `retry_count`, `last_error`.

## 7. Transaction

- Lock small batch with skip-locked strategy if supported.
- Mark published only after broker acknowledge.

## 8. Security

- Worker uses service credentials.
- Payload must not contain secrets.

## 9. Failure Cases

- Broker unavailable -> mark/retry later.
- Serialization error -> mark failed with error.

## 10. Acceptance Criteria

- Pending events are eventually published.
- Failed publishes are retried safely.
- Published events record `published_at`.

