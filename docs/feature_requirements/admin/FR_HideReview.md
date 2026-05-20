# Functional Requirement - Hide Review

## 1. Feature Overview

Cho phep admin hide review vi pham. Admin Service logs decision and publishes `REVIEW_HIDDEN`; Commerce Service sets review status hidden.

## 2. Actors

- **Admin/Moderator:** Hide review.
- **Commerce Service:** Apply review status.

## 3. Scope

**In Scope:**

- Log review hide.
- Publish `REVIEW_HIDDEN`.

**Out of Scope:**

- Edit rating/comment.
- Physical delete review.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/reviews/{reviewId}/hide`

**Auth:** Required, permission `REVIEW_HIDE`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Hidden review remains stored.
- Commerce recalculates rating summary if needed.

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

- Hide decision is logged.
- `REVIEW_HIDDEN` event is published.
- Commerce owns final review status.

