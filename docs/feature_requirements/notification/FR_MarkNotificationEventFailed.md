# Functional Requirement - Mark Notification Event Failed

## 1. Feature Overview

Mark event processing failure in `notification_events` with retry metadata and sanitized error.

## 2. Actors

- **Notification Worker:** Records failure.
- **Notification DB:** Stores status and error.

## 3. Scope

**In Scope:**

- Set `status = FAILED`.
- Increment retry count for retryable failures.
- Store sanitized `last_error`.
- Clear lock metadata.

**Out of Scope:**

- Manual operator repair UI.
- Real DLQ infrastructure.

## 4. Business Rules

- Retryable failure increments `retry_count`.
- Permanent failure can set `retry_count = max_retry_count`.
- `last_error` must be sanitized and bounded in size.
- Poison event must not block other events.

## 5. Database Impact

Update `notification_events`:

- `status = FAILED`
- `retry_count`
- `last_error`
- `locked_at = null`
- `locked_by = null`

## 6. Transaction

- Failure state should be committed even when handler fails.

## 7. Security

- Never store token, OTP, password, provider credential, raw authorization header in `last_error`.

## 8. Failure Cases

- DB unavailable -> worker logs sanitized error; event may remain stuck until recovery.
- Concurrent worker update -> lock/optimistic conflict.

## 9. Acceptance Criteria

- Failed event is visible for retry/ops.
- Retry count reflects attempt policy.
- Sensitive data is not stored in `last_error`.

