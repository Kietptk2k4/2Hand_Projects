# Functional Requirement - Handle Comment Created Notification

## 1. Feature Overview

Notify post author when another user comments on their post.

## 2. Actors

- **Social Service:** Publishes `COMMENT_CREATED`.
- **Notification Service:** Notifies post author.
- **Post Author:** Recipient.

## 3. Scope

**In Scope:**

- Resolve `post_author_id`.
- Skip comment by post author.
- Create in-app/push notification.

**Out of Scope:**

- Comment creation/moderation.
- Social DB reads.

## 4. Event Contract

Required payload:

- `actor_id`
- `post_id`
- `comment_id`
- `post_author_id`

## 5. Business Rules

- If `actor_id == post_author_id`, skip.
- Default channels: in-app + push.
- Reference: `POST/post_id` or `COMMENT/comment_id`.
- Comment content should be omitted or truncated/sanitized.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing post author -> failed.
- Missing post/comment id -> failed.
- Duplicate event -> no duplicate notification.

## 8. Acceptance Criteria

- Post author receives notification for external comment.
- Self-comment does not notify.
- Private/unsafe comment body is not exposed.

