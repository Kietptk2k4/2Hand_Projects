# Functional Requirement - Process Notification Event

## 1. Feature Overview

Processing worker routes `notification_events` into in-app, push and email delivery according to event type, recipients and settings.

## 2. Actors

- **Notification Worker:** Processes event.
- **Notification DB:** Stores event and notifications.
- **External Providers:** FCM/email when applicable.

## 3. Scope

**In Scope:**

- Lock pending/failed event.
- Resolve handler and recipients.
- Apply delivery rules/settings.
- Create/send notification by channel.
- Mark event completed or failed.

**Out of Scope:**

- Producer domain validation.
- Direct DB access to other services.

## 4. Business Rules

- Process only `PENDING` or retryable `FAILED` events.
- Use row lock/skip locked to avoid concurrent processing.
- Unsupported event type fails with sanitized error.
- Missing recipient fails unless event type allows no-op completion.
- Self notification rules apply to social events.
- Handler must be idempotent.

## 5. Database Impact

- Update `notification_events.status`.
- Insert `user_notifications` when in-app channel allowed.
- Update `user_notifications.delivery_status` where applicable.

## 6. Transaction

- DB state changes should be atomic per event where possible.
- Avoid long DB transaction while waiting for external providers.

## 7. Failure Cases

- Handler missing -> `FAILED`.
- Recipient missing -> `FAILED`.
- Duplicate user notification -> treat as success.
- Provider transient failure -> retryable failed delivery/event.

## 8. Acceptance Criteria

- Worker processes pending events.
- Event handler applies channel policy and settings.
- Successful event ends as `COMPLETED`.
- Failed event records retry metadata.

