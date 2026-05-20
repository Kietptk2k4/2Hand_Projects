# Functional Requirement - Retry Commerce Outbox Events

## 1. Feature Overview

Outbox worker retry cac commerce events publish that bai, dam bao domain events khong bi mat khi broker/network loi tam thoi.

## 2. Actors

- **Outbox Worker:** Retry failed events.
- **Message Broker:** Nhan event.
- **Consumer Services:** Xu ly event idempotently.

## 3. Scope

**In Scope:**

- Find failed/pending retryable events.
- Mark event `PROCESSING`.
- Republish to broker.
- Mark `PUBLISHED` or `FAILED`.
- Increment retry count and store last error.

**Out of Scope:**

- Consumer retry.
- Dead-letter UI.

## 4. Trigger

- Continuous worker polling.
- Scheduled retry job.
- Manual repair trigger later.

## 5. Business Rules

- Publish is at-least-once.
- Consumers must deduplicate by `event_id` or `event_key`.
- Retry should use backoff.
- Stale `PROCESSING` events should be recoverable.
- Never delete failed event silently.

## 6. Database Impact

- Read/update `outbox_events`.
- Update `status`, `retry_count`, `last_error`, `published_at`.

## 7. Transaction

- Transaction required for claiming events.
- Use row lock/skip locked for concurrent workers.

## 8. Security

- Internal worker only.
- Do not log sensitive payment/provider payload fields.

## 9. Failure Cases

- Broker unavailable -> mark `FAILED`, retry later.
- Worker crashes after publish before mark published -> stale processing recovery can republish.
- Invalid payload -> failed until code/data repair.

## 10. Acceptance Criteria

- Failed events are retried.
- Successful publish marks event `PUBLISHED`.
- Retry count and last error are tracked.
- Duplicate publish is tolerated.

