# Functional Requirement - Mark Notification Event Completed

## 1. Feature Overview

Mark `notification_events` row as `COMPLETED` after all required channel work for that event is done or intentionally skipped.

## 2. Actors

- **Notification Worker:** Completes event.
- **Notification DB:** Persists terminal state.

## 3. Scope

**In Scope:**

- Update event status.
- Set `processed_at`.
- Clear worker lock metadata.
- Clear retryable error if appropriate.

**Out of Scope:**

- Reprocessing completed event.

## 4. Business Rules

- `COMPLETED` is terminal in MVP.
- Self-skipped/no-op event can be completed.
- Event should be completed only after required notifications are created/sent or intentionally skipped.
- `processed_at` must be non-null for completed event.

## 5. Database Impact

Update `notification_events`:

- `status = COMPLETED`
- `processed_at = now()`
- `locked_at = null`
- `locked_by = null`
- `last_error = null` optional

## 6. Transaction

- Should be in same transaction as final DB-side processing result.

## 7. Failure Cases

- Event not found -> no update/alert.
- Event processed by another worker -> optimistic/lock conflict.
- DB update failure -> event may retry and must remain idempotent.

## 8. Acceptance Criteria

- Completed event has `processed_at`.
- Completed event is not selected by normal retry worker.
- Lock metadata is cleared.

