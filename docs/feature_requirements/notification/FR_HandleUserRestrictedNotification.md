# Functional Requirement - Handle User Restricted Notification

## 1. Feature Overview

Notify target user when Admin publishes `USER_RESTRICTED`.

## 2. Actors

- **Admin Service:** Publishes restriction event.
- **Notification Service:** Creates notification.
- **Target User:** Recipient.

## 3. Scope

**In Scope:**

- Create restriction notice.
- Send push/email according to critical policy.
- Include user-safe capability restriction summary.

**Out of Scope:**

- Applying restriction to Auth/Social/Commerce.
- Permission evaluation.

## 4. Event Contract

Required payload:

- `enforcement_id`
- `target_user_id`
- `restricted_capabilities` optional
- `reason` user-safe
- `expires_at` optional

## 5. Business Rules

- Admin owns restriction decision.
- Notification content should describe restriction at high level.
- Internal notes must not be exposed.
- Reference: `USER_ENFORCEMENT/enforcement_id`.
- Duplicate event is idempotent.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing target user -> failed.
- Unsafe reason/capability text -> sanitize.
- Provider failures -> retry by channel.

## 8. Acceptance Criteria

- Target user receives restriction notification.
- Content is user-safe and high-level.
- No upstream service state is mutated.

