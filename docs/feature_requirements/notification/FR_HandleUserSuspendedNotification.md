# Functional Requirement - Handle User Suspended Notification

## 1. Feature Overview

Notify target user when Admin publishes `USER_SUSPENDED`.

## 2. Actors

- **Admin Service:** Publishes suspension event.
- **Notification Service:** Sends notification/email.
- **Target User:** Recipient.

## 3. Scope

**In Scope:**

- Create account suspension in-app notification.
- Send push and email by critical policy.
- Include user-safe reason/duration.

**Out of Scope:**

- Suspending user.
- Revoking sessions.
- Updating Auth status.

## 4. Event Contract

Required payload:

- `enforcement_id`
- `target_user_id`
- `reason` user-safe
- `expires_at` optional

## 5. Business Rules

- Admin Service owns enforcement decision.
- Suspended user can still receive account-critical notification.
- Internal admin notes must not be exposed.
- Email can override disabled email setting if account-critical policy applies.
- Reference: `USER_ENFORCEMENT/enforcement_id`.

## 6. Database Impact

- Insert `user_notifications` if allowed/required.
- Update `notification_events`.

## 7. Failure Cases

- Missing target user -> failed.
- Unsafe reason -> sanitize or fail.
- Email provider failure -> retry email delivery.

## 8. Acceptance Criteria

- Target user receives suspension notice.
- Notice does not expose internal admin notes.
- Notification Service does not mutate Auth/Admin state.

