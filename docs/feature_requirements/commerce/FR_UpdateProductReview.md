# Functional Requirement - Update Product Review

## 1. Feature Overview

Cho phep buyer cap nhat review cua minh, bao gom rating va comment. Neu rating thay doi, system phai cap nhat rating summary.

## 2. Actors

- **Buyer:** Cap nhat review cua minh.
- **System:** Validate ownership and recalculate rating.

## 3. Scope

**In Scope:**

- Update rating/comment.
- Recalculate rating summary if rating changed.

**Out of Scope:**

- Seller reply update.
- Admin moderation.
- Media upload; xem `FR_UploadReviewMedia.md`.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/reviews/{reviewId}`

**Auth:** Required (JWT)

**Request body:**

- `rating` optional
- `comment` optional

## 5. Business Rules

- Buyer can update only own review.
- Rating, if provided, must be 1..5.
- Hidden review update can be rejected in MVP.
- `order_item_id`, `buyer_id`, `seller_id` cannot be changed.

## 6. Database Impact

- Read/update `reviews`.
- Update rating summary if needed.
- Insert outbox event.

## 7. Transaction

- Required.

## 8. Security

- JWT required.
- Ownership check by `reviews.buyer_id`.

## 9. Failure Cases

- Review not found/not owned -> 404.
- Invalid rating -> 400.
- Review hidden -> 409 according MVP policy.

## 10. Acceptance Criteria

- Buyer updates own visible review.
- Other buyer review cannot be updated.
- Rating summary changes when rating changes.
- Immutable review linkage fields cannot change.

