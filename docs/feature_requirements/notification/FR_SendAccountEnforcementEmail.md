# Functional Requirement - Send Account Enforcement Email

## 1. Feature Overview

Send account enforcement email when Admin publishes user suspension/restriction events.

## 2. Actors

- **Admin Service:** Publishes enforcement event.
- **Notification Service:** Sends email.
- **Target User:** Receives enforcement notice.

## 3. Scope

**In Scope:**

- Send email for `USER_SUSPENDED` and `USER_RESTRICTED`.
- Include user-safe reason and duration if available.

**Out of Scope:**

- Creating/revoking enforcement.
- Updating Auth user status.
- Exposing internal admin notes.

## 4. Trigger

- `USER_SUSPENDED`
- `USER_RESTRICTED`

## 5. Business Rules

- Admin Service owns enforcement decision.
- Suspended user can still receive account-critical email.
- User-facing reason must be sanitized.
- Internal moderation/admin notes must not be included.
- Email may override normal email preference if policy marks it account-critical.

## 6. Database Impact

- Read/update `notification_events`.
- Optional delivery status update.

## 7. Failure Cases

- Missing target email/user -> failed.
- Unsafe reason payload -> sanitize or fail.
- Provider transient error -> retry.

## 8. Security

- Do not expose admin internal note.
- Do not mutate Auth/Admin state.

## 9. Acceptance Criteria

- Target user receives enforcement email.
- Critical delivery works even for suspended user.
- Email content is user-safe.

