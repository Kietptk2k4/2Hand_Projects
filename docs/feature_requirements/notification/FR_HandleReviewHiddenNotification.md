# Functional Requirement - Handle Review Hidden Notification

## 1. Feature Overview

Notify review author, and optionally seller, when Admin publishes `REVIEW_HIDDEN`.

## 2. Actors

- **Admin Service:** Publishes moderation event.
- **Notification Service:** Creates notification.
- **Review Author/Seller:** Recipients by policy.

## 3. Scope

**In Scope:**

- Create review hidden notification if enabled.
- Include user-safe moderation reason.

**Out of Scope:**

- Hiding/restoring review.
- Commerce review mutation.
- Mandatory email.

## 4. Event Contract

Required payload:

- `review_id`
- `review_author_id`
- `seller_user_id` optional
- `reason` optional user-safe

## 5. Business Rules

- MVP optional event handler.
- Default channels: in-app only.
- Internal moderation notes must not be exposed.
- Reference: `REVIEW/review_id`.
- Duplicate event is idempotent.

## 6. Database Impact

- Insert `user_notifications` if enabled and allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing review author and no optional recipient -> failed or skipped by policy.
- Unsafe reason -> sanitize or omit.

## 8. Acceptance Criteria

- Review author receives notice when policy enables this handler.
- Internal notes are hidden.
- Commerce review state is not mutated.

