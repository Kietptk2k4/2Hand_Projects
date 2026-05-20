# UC - Notification Reliability Idempotency

## 1. Overview

Use case nay mo ta cac invariant bat buoc de Notification Service xu ly event/delivery an toan trong moi truong at-least-once: deduplicate ingestion, idempotent processing, worker crash recovery va permanent failure handling.

## 2. Actors

- **Notification Consumer:** Deduplicate broker messages.
- **Notification Worker:** Process/retry events.
- **Message Broker:** May redeliver messages.
- **External Providers:** May timeout after sending.
- **System Operator:** Monitors failed events.

## 3. Related Data

- `notification_events`
- `user_notifications`
- `user_device_tokens`

## 4. Preconditions

- Unique indexes exist for event and user notification idempotency.
- Workers use locking/retry policy.
- Event payload includes `source_event_id` or `event_key`.

## 5. Business Rules

- Duplicate producer event must not duplicate user-visible notification.
- `COMPLETED` event is terminal unless manual reprocess is designed later.
- Self-skipped event can be `COMPLETED`.
- Poison event must not block unrelated events.
- `last_error` must be sanitized and bounded.
- Event ordering is not guaranteed.

## 6. Sub-Use Cases

### UC-IDEMP-01: Deduplicate Event Ingestion

Main flow:

1. Consumer receives broker message.
2. Consumer inserts `notification_events`.
3. DB unique key detects duplicate if already ingested.
4. Consumer treats duplicate as success and acks.

Postconditions:

- One internal event row per producer event.

### UC-IDEMP-02: Idempotent User Notification Creation

Main flow:

1. Worker processes event.
2. Worker inserts `user_notifications` by event/user/reference key.
3. Duplicate conflict occurs during retry.
4. Worker treats conflict as success.

Postconditions:

- One user-visible notification per event/user/reference.

### UC-IDEMP-03: Recover Stale Processing

Main flow:

1. Recovery job finds `PROCESSING` rows with stale `locked_at`.
2. Job marks row `FAILED` retryable.
3. Normal retry worker reprocesses row.

Postconditions:

- Rows do not remain stuck forever.

### UC-IDEMP-04: Handle Permanent Failure

Main flow:

1. Worker detects non-retryable error or max retries exceeded.
2. Worker sets `status = FAILED`.
3. Worker stores sanitized permanent failure reason.
4. Metrics/logs expose failure for ops.

## 7. Failure Cases

- Missing idempotency key.
- External provider timeout after successful send.
- Worker crash after inserting notification but before marking event completed.
- Out-of-order events.
- Poison payload.

## 8. Security

- Error logging must not include sensitive payload fields.
- Internal retry/recovery endpoints, if any, require service auth.
- Idempotency keys should not encode secrets.

## 9. Acceptance Criteria

- Duplicate broker delivery creates no duplicate notifications.
- Retry after worker crash is safe.
- Stale processing rows recover.
- Permanent failures stop infinite retry.
- One poison event does not block queue processing.

