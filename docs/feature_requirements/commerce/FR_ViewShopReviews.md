# Functional Requirement - View Shop Reviews

## 1. Feature Overview

Cho phep seller xem review cua shop minh de theo doi rating, feedback va reply khi can.

## 2. Actors

- **Seller:** Xem review shop.
- **System:** Query reviews scoped by seller/shop.

## 3. Scope

**In Scope:**

- List reviews by seller/shop.
- Filter by rating/status if policy allows.
- Include review media and seller reply.
- Include rating summary.

**Out of Scope:**

- Create buyer review.
- Moderate review.
- Reply review; xem `FR_ReplyToReview.md`.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/seller/reviews`

**Auth:** Required (JWT seller)

**Query params:**

- `rating`
- `status`
- `page` / `cursor`
- `limit`

## 5. Business Rules

- Seller sees only reviews of own shop.
- Public hidden review visibility to seller depends policy; MVP can return visible reviews only unless seller support view is needed.
- Seller cannot edit buyer rating/comment from this endpoint.

## 6. Database Impact

- Read `seller_shops`.
- Read `reviews`.
- Read `review_media`.
- Read `review_replies`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Seller ownership check by shop/seller id.

## 9. Failure Cases

- Seller has no shop -> 404/409.
- Invalid filter -> 400.

## 10. Acceptance Criteria

- Seller sees only own shop reviews.
- Review media/reply included.
- Seller cannot mutate review through this endpoint.

