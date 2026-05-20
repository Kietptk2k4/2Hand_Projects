# Functional Requirement - Moderate Review

## 1. Feature Overview

Cho phep admin hide hoac restore review. Hidden review khong hien trong public product/shop review list va co the bi loai khoi rating summary.

## 2. Actors

- **Admin/Moderator:** Hide/restore review.
- **System:** Update review status and rating summary.

## 3. Scope

**In Scope:**

- Set review `HIDDEN` or `VISIBLE`.
- Recalculate rating summary if needed.
- Emit moderation event.

**Out of Scope:**

- Physical delete review.
- Edit buyer comment/rating.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/admin/reviews/{reviewId}/moderate`

**Auth:** Required (JWT admin permission)

**Request body:**

- `action`: `HIDE` or `RESTORE`
- `reason`

## 5. Business Rules

- Admin permission required.
- Hidden review excluded from public lists.
- Seller cannot hide review via this admin endpoint.
- If rating summary uses visible reviews only, recalculate after moderation.

## 6. Database Impact

- Read/update `reviews`.
- Update `seller_shops.rating_avg/rating_count` if needed.
- Insert outbox event.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT admin required.
- Permission such as `COMMERCE_REVIEW_HIDE` required.

## 9. Failure Cases

- Missing permission -> 403.
- Review not found -> 404.
- Invalid action -> 400.

## 10. Acceptance Criteria

- Admin hides/restores review with permission.
- Hidden review disappears from public review list.
- Rating summary updates according policy.

