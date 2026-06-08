# Functional Requirement - View Product Review For Edit

## 1. Feature Overview

Cho phep buyer lay chi tiet review de pre-fill form edit, bao gom rating, comment, media count va order item snapshots.

## 2. Actors

- **Buyer:** Xem review de sua.
- **System:** Load owned review detail.

## 3. Scope

**In Scope:**

- View owned review by id.
- Return edit-relevant fields and snapshots.
- Return `media_count`.

**Out of Scope:**

- Update review (separate FR).
- Public review detail page.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/reviews/{reviewId}`

**Auth:** Required (JWT buyer)

## 5. Business Rules

- Review must belong to buyer.
- Read-only.
- Route `/reviews/context` must not conflict with `{reviewId}` path.

## 6. Database Impact

- Read `reviews`, `order_items`, optional count `review_media`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Ownership by `reviews.buyer_id`.

## 9. Failure Cases

- Review not found/not owned -> 404.

## 10. Acceptance Criteria

- Buyer can load edit form for own visible review.
- Snapshots included for product summary UI.
- Other users cannot access review detail.

## 11. Related

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewProductReviewForEdit-api-and-behavior.md`
- Update: `FR_UpdateProductReview.md`