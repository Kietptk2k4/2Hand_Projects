# Functional Requirement - Retry Failed Notification Events

## 1. Feature Overview

Background worker retries failed notification events that still have retry budget.

## 2. Actors

- **Scheduler:** Triggers retry.
- **Notification Worker:** Reprocesses failed events.
- **Notification DB:** Stores retry state.

## 3. Scope

**In Scope:**

- Select retryable `FAILED` events.
- Apply backoff/max retry.
- Reprocess idempotently.
- Update status after retry.

**Out of Scope:**

- Manual payload editing.
- Dead-letter queue.

## 4. Business Rules

- Retry only `status = FAILED AND retry_count < max_retry_count`.
- Permanent validation errors should not retry forever.
- Retry must not create duplicate `user_notifications`.
- Stale `PROCESSING` recovery can convert stuck rows to retryable failed.

## 5. Database Impact

- Read/update `notification_events`.
- May insert/update `user_notifications` idempotently during reprocessing.

## 6. Transaction

- Select with row locking if possible.
- Process in small batches.

## 7. Failure Cases

- Retry fails again -> increment retry metadata.
- Max retries exceeded -> remain `FAILED`.
- Duplicate notification conflict -> treat as success.

## 8. Acceptance Criteria

- Retryable failed events are retried.
- Max retry policy is enforced.
- Retry is safe after partial prior processing.

