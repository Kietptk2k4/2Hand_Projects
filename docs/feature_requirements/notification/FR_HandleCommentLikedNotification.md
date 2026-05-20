# Functional Requirement - Handle Comment Liked Notification

## 1. Feature Overview

Notify comment author when another user likes their comment.

## 2. Actors

- **Social Service:** Publishes `COMMENT_LIKED`.
- **Notification Service:** Notifies comment author.
- **Comment Author:** Recipient.

## 3. Scope

**In Scope:**

- Resolve comment author.
- Skip self-like.
- Create in-app/push notification.

**Out of Scope:**

- Like mutation.
- Social DB read.

## 4. Event Contract

Required payload:

- `actor_id`
- `comment_id`
- `comment_author_id`

## 5. Business Rules

- If `actor_id == comment_author_id`, skip.
- Default channels: in-app + push.
- Email is not sent.
- Reference: `COMMENT/comment_id`.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing comment author -> failed.
- Missing comment id -> failed.
- Duplicate event -> no duplicate notification.

## 8. Acceptance Criteria

- Comment author receives external like notification.
- Self-like does not notify.
- Notification is idempotent.

