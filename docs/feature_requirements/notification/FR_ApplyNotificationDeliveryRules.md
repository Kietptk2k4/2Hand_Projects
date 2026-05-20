# Functional Requirement - Apply Notification Delivery Rules

## 1. Feature Overview

Apply per-event delivery policy to decide whether Notification Service should create in-app notification, send push and/or send email.

## 2. Actors

- **Notification Worker:** Applies delivery rules.
- **User:** Receives only allowed channels.

## 3. Scope

**In Scope:**

- Resolve default channel policy by `event_type`.
- Apply notification settings.
- Apply critical overrides.
- Apply skip rules.

**Out of Scope:**

- Marketing segmentation.
- Notification priority engine.

## 4. Business Rules

- Event type has default channel policy.
- User settings can disable channels unless critical override applies.
- Social self-notifications are skipped.
- No active push token means push skipped, not event failure.
- Email is limited to critical/system events in MVP.

## 5. Database Impact

- Read `user_notification_settings`.
- Read `user_device_tokens` for push eligibility.
- Insert/update `user_notifications` depending selected channels.

## 6. Transaction

- Rule evaluation should happen inside processing flow before channel actions.

## 7. Failure Cases

- Unknown event type -> fail event or ignore by allowlist policy.
- Settings lookup failure -> retry event.
- Invalid channel config -> fail fast.

## 8. Acceptance Criteria

- Each event is routed to correct channels.
- User settings are respected.
- Critical overrides are explicit.
- Delivery decisions are deterministic.

