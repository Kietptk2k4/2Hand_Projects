# UC - Background Jobs

## 1. Overview

Use case nay mo ta background jobs cua Admin Service: expire user enforcements va retry/publish outbox events.

## 2. Actors

- **System Scheduler/Worker:** Chay jobs.
- **Outbox Worker:** Publish events.
- **Consumer Services:** Consume admin events.

## 3. Related Data

- `user_enforcements`
- `user_enforcement_logs`
- `outbox_events`

## 4. Business Rules

- Jobs must be idempotent.
- Use small batches and row locking.
- Expired enforcement publishes event.
- Outbox publish is at-least-once.

## 5. Sub-Use Cases

### 5.1. Expire User Enforcements

**Main Flow:**

1. Job finds `ACTIVE` enforcements with expired `expires_at`.
2. Job marks them `EXPIRED`.
3. Job writes enforcement log.
4. Job writes outbox event `USER_ENFORCEMENT_EXPIRED`.

### 5.2. Retry Admin Outbox Events

**Main Flow:**

1. Worker claims pending/failed events.
2. Worker publishes to broker.
3. Worker marks published or failed.

### 5.3. Recover Stale Processing Events

**Main Flow:** Worker resets stale `PROCESSING` events to retryable state.

## 6. Acceptance Criteria

- Temporary enforcement expires automatically.
- Permanent enforcement never auto-expires.
- Jobs can retry safely.
- Outbox failed events are not lost.

