# Functional Requirement - Moderate Comment

## 1. Feature Overview

Cho phep admin hide hoac remove Social comment vi pham. Admin Service logs moderation and publishes event; Social Service owns comment state.

## 2. Actors

- **Admin/Moderator:** Moderate comment.
- **Social Service:** Apply comment visibility/status.

## 3. Scope

**In Scope:**

- Hide comment.
- Remove comment.
- Log moderation.
- Publish `COMMENT_MODERATED`.

**Out of Scope:**

- Edit comment content.
- Direct Social DB update.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/social/comments/{commentId}/moderate`

**Auth:** Required, social moderation permission.

**Request body:**

- `action`: `HIDE` or `REMOVE`
- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Admin Service does not mutate Social DB.
- Social owns comment status and counter side effects.

## 6. Database Impact

- Insert `content_moderation_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required.

## 8. Security

- Permission required.

## 9. Failure Cases

- Missing permission -> 403.
- Comment not found -> 404 if synchronous validation.
- Invalid action -> 400.

## 10. Acceptance Criteria

- Comment moderation action is logged.
- `COMMENT_MODERATED` event is published.
- Social owns final comment state.

