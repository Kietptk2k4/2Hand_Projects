# Functional Requirement - Restore Review

## 1. Feature Overview

Cho phep admin restore review da bi hidden/removed theo moderation policy. Commerce Service own final review status.

## 2. Actors

- **Admin/Moderator:** Restore review.
- **Commerce Service:** Apply review restore.

## 3. Scope

**In Scope:**

- Log review restore.
- Publish `REVIEW_RESTORED`.

**Out of Scope:**

- Edit review content.
- Recalculate rating inside Admin Service.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/reviews/{reviewId}/restore`

**Auth:** Required, permission `REVIEW_HIDE` or `REVIEW_RESTORE`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Commerce validates restore eligibility.
- Admin Service does not mutate Commerce DB.

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
- Commerce rejects restore -> 409.

## 10. Acceptance Criteria

- Restore action is logged.
- Restore event is published.
- Commerce owns final status and rating effects.

