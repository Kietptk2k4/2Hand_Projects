# Functional Requirement - Restore Comment

## 1. Feature Overview

Cho phep admin restore Social comment da bi hide/remove neu Social Service cho phep.

## 2. Actors

- **Admin/Moderator:** Restore comment.
- **Social Service:** Validate and apply restore.

## 3. Scope

**In Scope:**

- Log comment restore.
- Publish comment restore/moderation event.

**Out of Scope:**

- Edit comment content.
- Restore deleted parent post automatically.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/social/comments/{commentId}/restore`

**Auth:** Required, social moderation permission.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Restore must not bypass Social policy.
- Social owns final status.

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
- Social rejects restore -> 409.

## 10. Acceptance Criteria

- Comment restore is logged.
- Restore event is published.
- Social applies final restore decision.

