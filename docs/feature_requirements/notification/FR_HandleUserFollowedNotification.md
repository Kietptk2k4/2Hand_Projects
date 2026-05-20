# Functional Requirement - Handle User Followed Notification

## 1. Feature Overview

Create notification when a user follows another user.

## 2. Actors

- **Social Service:** Publishes `USER_FOLLOWED`.
- **Notification Service:** Notifies followed user.
- **Followed User:** Recipient.

## 3. Scope

**In Scope:**

- Resolve followed user.
- Create in-app/push notification.
- Skip self-follow if emitted.

**Out of Scope:**

- Follow relationship mutation.
- Social graph queries.

## 4. Event Contract

Required payload:

- `actor_id`
- `followed_user_id`

## 5. Business Rules

- If `actor_id == followed_user_id`, skip.
- Default channels: in-app + push.
- Email is not sent.
- Reference: `USER/actor_id`.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing followed user -> failed.
- Duplicate event -> idempotent success.

## 8. Acceptance Criteria

- Followed user receives notification.
- Self-follow does not notify.
- User settings are respected.

