# Functional Requirement - Handle User Banned Notification

## 1. Feature Overview

Notify target user when Admin publishes `USER_BANNED`.

## 2. Actors

- **Admin Service:** Publishes ban enforcement event.
- **Notification Service:** Sends in-app, push, and email notification.
- **Target User:** Recipient.

## 3. Scope

**In Scope:**

- Create account ban in-app notification.
- Send push and email by account-critical policy.
- Include user-safe reason/duration.

**Out of Scope:**

- Banning user.
- Revoking sessions.
- Updating Auth status.

## 4. Event Contract

Required payload:

- `enforcement_id`
- `target_user_id` (or `user_id` at ingest)
- `reason` user-safe
- `expires_at` optional

## 5. Business Rules

- Admin Service owns enforcement decision.
- Banned user can still receive account-critical notification.
- Internal admin notes must not be exposed.
- Email can override disabled email setting if account-critical policy applies.
- Reference: `USER_ENFORCEMENT/enforcement_id`.
- `event_type` stored as `USER_BANNED` (distinct from `USER_SUSPENDED`).

## 6. Database Impact

- Insert `user_notifications` if allowed/required.
- Update `notification_events`.

## 7. Failure Cases

- Missing target user -> failed permanent.
- Unsafe reason -> sanitize or fail.
- Email provider failure -> retry email delivery.

## 8. Acceptance Criteria

- Target user receives ban notice with ban-specific copy.
- Notice does not expose internal admin notes.
- Notification Service does not mutate Auth/Admin state.
