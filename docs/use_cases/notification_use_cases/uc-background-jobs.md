# UC - Background Jobs

## 1. Overview

Use case nay mo ta cac background jobs cua Notification Service: process pending events, retry failed events, recover stale processing, retry failed delivery, cleanup invalid tokens va optional cleanup old notifications/review reminder.

## 2. Actors

- **Scheduler:** Trigger jobs.
- **Notification Worker:** Execute jobs.
- **Notification DB:** Stores event/delivery/token state.
- **External Providers:** FCM/email provider.

## 3. Related Data

- `notification_events`
- `user_notifications`
- `user_device_tokens`

## 4. Preconditions

- Worker has DB access.
- Job schedule configured.
- Retry/backoff policy configured.

## 5. Business Rules

- Jobs must be idempotent.
- Process in small batches.
- Use row locking/skip locked where possible.
- Do not retry beyond `max_retry_count`.
- Invalid device token is deactivated, not hard deleted.
- Optional cleanup must follow retention policy.

## 6. Sub-Use Cases

### UC-JOB-01: Process Pending Events

Main flow:

1. Scheduler/worker selects `PENDING` events.
2. Worker locks batch.
3. Worker processes events through routing handlers.
4. Worker marks rows `COMPLETED` or `FAILED`.

### UC-JOB-02: Retry Failed Events

Main flow:

1. Worker selects `FAILED` events with retry budget.
2. Worker applies backoff.
3. Worker reprocesses event idempotently.
4. Worker updates status/retry metadata.

### UC-JOB-03: Recover Stale Processing

Main flow:

1. Worker finds `PROCESSING` rows with old `locked_at`.
2. Worker marks them `FAILED` retryable.
3. Worker clears lock metadata.

### UC-JOB-04: Cleanup Invalid Or Stale Tokens

Main flow:

1. Worker finds tokens marked invalid/stale.
2. Worker sets `is_active = false`.
3. Worker updates `updated_at`.

### UC-JOB-05: Optional Cleanup Old Notifications

Main flow:

1. Worker finds notifications older than retention.
2. Worker soft/hard deletes according to retention policy.
3. Worker avoids deleting audit-critical records unless allowed.

## 7. Failure Cases

- Worker crash.
- DB lock contention.
- Provider unavailable.
- Poison event.
- Retention config missing.

## 8. Security

- Jobs are internal only.
- Logs must be sanitized.
- Job should not expose payload/token data to unauthorized users.

## 9. Acceptance Criteria

- Pending events are eventually processed.
- Retryable failed events are retried safely.
- Stale processing rows do not remain stuck.
- Invalid tokens are deactivated.
- Jobs can rerun without duplicate notifications.

