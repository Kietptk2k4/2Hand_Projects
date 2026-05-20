# Functional Requirement - Retry Admin Outbox Events

## 1. Feature Overview

Cho phep worker retry cac Admin outbox events bi fail tam thoi de dam bao event eventually delivered.

## 2. Actors

- **Outbox Worker:** Retries failed events.
- **Admin Operator:** Optionally monitors retry state.

## 3. Scope

**In Scope:**

- Select failed retryable events.
- Apply retry backoff and max retry policy.
- Update retry metadata.

**Out of Scope:**

- Manual payload correction.
- Dead-letter UI.

## 4. Trigger

- Scheduled retry worker.

## 5. Business Rules

- Retry only events under max retry threshold.
- Non-retryable serialization/schema errors should stay failed for manual investigation.
- Backoff should prevent hot loop.
- Event id remains stable across retries.

## 6. Database Impact

- Read `outbox_events`.
- Update `retry_count`, `last_error`, `status`, `published_at`.

## 7. Transaction

- Lock retry batch.
- Mark published only after successful broker ack.

## 8. Security

- Internal worker only.
- Do not expose event payload to unauthorized users.

## 9. Failure Cases

- Broker still unavailable -> retry_count increments.
- Max retries exceeded -> status remains failed/dead-letter by policy.

## 10. Acceptance Criteria

- Retryable failed events are retried.
- Max retry policy is enforced.
- Duplicate consumer effects are prevented by stable event id/idempotency.

