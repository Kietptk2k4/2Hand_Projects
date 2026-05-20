# Functional Requirement - Handle Comment Replied Notification

## 1. Feature Overview

Notify parent comment author when another user replies to their comment.

## 2. Actors

- **Social Service:** Publishes `COMMENT_REPLIED`.
- **Notification Service:** Notifies parent comment author.
- **Parent Comment Author:** Recipient.

## 3. Scope

**In Scope:**

- Resolve parent comment author.
- Skip self-reply.
- Create in-app/push notification.

**Out of Scope:**

- Reply creation/moderation.
- Social DB access.

## 4. Event Contract

Required payload:

- `actor_id`
- `comment_id`
- `parent_comment_id`
- `parent_comment_author_id`

## 5. Business Rules

- If `actor_id == parent_comment_author_id`, skip.
- Default channels: in-app + push.
- Reference: `COMMENT/parent_comment_id` or reply `COMMENT/comment_id`.
- Reply snippet must be sanitized if included.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing parent comment author -> failed.
- Missing comment reference -> failed.
- Duplicate event -> idempotent.

## 8. Acceptance Criteria

- Parent comment author receives reply notification.
- Self-reply does not notify.
- User settings are respected.

