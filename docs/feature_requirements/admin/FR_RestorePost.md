# Functional Requirement - Restore Post

## 1. Feature Overview

Cho phep admin restore Social post da bi moderation hide/remove neu Social Service cho phep.

## 2. Actors

- **Admin/Moderator:** Restore post.
- **Social Service:** Validate and apply restore.

## 3. Scope

**In Scope:**

- Log post restore.
- Publish post restore/moderation event.

**Out of Scope:**

- Edit post content.
- Restore deleted user account.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/social/posts/{postId}/restore`

**Auth:** Required, social moderation permission.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Restore must not bypass Social content/user policy.
- Social owns final state.

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
- Social rejects restore -> 409.

## 10. Acceptance Criteria

- Post restore is logged.
- Restore event is published.
- Social applies final restore decision.

