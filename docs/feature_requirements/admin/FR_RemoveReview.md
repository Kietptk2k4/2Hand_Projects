# Functional Requirement - Remove Review

## 1. Feature Overview

Cho phep admin soft remove review vi pham nang. Admin Service logs decision and publishes event; Commerce applies soft delete/status policy.

## 2. Actors

- **Admin/Moderator:** Remove review.
- **Commerce Service:** Apply soft remove.

## 3. Scope

**In Scope:**

- Log review remove.
- Publish review removed/moderated event.

**Out of Scope:**

- Physical delete.
- Edit buyer content.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/reviews/{reviewId}/remove`

**Auth:** Required, permission `REVIEW_HIDE` or `REVIEW_REMOVE`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Remove is soft moderation.
- Reason required.
- Review remains auditable.
- Commerce owns exact status/state.

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
- Review not found -> 404 if synchronous validation.

## 10. Acceptance Criteria

- Remove action is logged.
- Review remove event is published.
- Review is not physically deleted by Admin Service.

