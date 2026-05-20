# Functional Requirement - Moderate Post

## 1. Feature Overview

Cho phep admin hide hoac remove Social post vi pham. Admin Service logs moderation decision and publishes event; Social Service owns post state.

## 2. Actors

- **Admin/Moderator:** Moderate post.
- **Social Service:** Apply post visibility/status.

## 3. Scope

**In Scope:**

- Hide post.
- Remove post.
- Log moderation.
- Publish `POST_MODERATED`.

**Out of Scope:**

- Edit post content.
- Direct Social DB update.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/social/posts/{postId}/moderate`

**Auth:** Required, social moderation permission.

**Request body:**

- `action`: `HIDE` or `REMOVE`
- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Admin Service does not mutate Social DB.
- Social applies final state and feed visibility.

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
- Post not found -> 404 if synchronous validation.
- Invalid action -> 400.

## 10. Acceptance Criteria

- Post moderation action is logged.
- `POST_MODERATED` event is published.
- Social owns final post state.

