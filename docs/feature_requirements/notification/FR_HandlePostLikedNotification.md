# Functional Requirement - Handle Post Liked Notification

## 1. Feature Overview

Create notification for post author when Social publishes `POST_LIKED`.

## 2. Actors

- **Social Service:** Publishes event.
- **Notification Service:** Notifies post author.
- **Post Author:** Recipient.

## 3. Scope

**In Scope:**

- Resolve recipient from `post_author_id`.
- Skip self-like.
- Create in-app/push notification.

**Out of Scope:**

- Like creation/removal.
- Reading Social DB.

## 4. Event Contract

Required payload:

- `actor_id`
- `post_id`
- `post_author_id`
- actor display summary optional

## 5. Business Rules

- If `actor_id == post_author_id`, skip and complete event.
- Default channels: in-app + push.
- Email is not sent.
- Reference: `POST/post_id`.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing `post_author_id` -> failed.
- Missing `post_id` -> failed.
- Duplicate event -> no duplicate notification.

## 8. Acceptance Criteria

- Post author receives notification for external like.
- Self-like does not notify.
- Social DB is not accessed directly.

