# Functional Requirement - Reply To Review

## 1. Feature Overview

Cho phep seller phan hoi review cua buyer tren shop/product cua minh. MVP chi cho mot reply cho moi review.

## 2. Actors

- **Seller:** Reply review cua shop minh.
- **Buyer:** Xem seller reply tren review.
- **System:** Validate ownership and persist reply.

## 3. Scope

**In Scope:**

- Seller create reply for review.
- Validate review belongs to seller shop.
- Return reply in product/shop review view.

**Out of Scope:**

- Multi-reply thread.
- Seller edit/delete reply.
- Buyer comment thread.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/reviews/{reviewId}/reply`

**Auth:** Required (JWT seller)

**Request body:**

- `content`

## 5. Business Rules

- Seller can reply only review belonging to own shop.
- Review must exist.
- Review should be `VISIBLE` in MVP.
- One reply per review.
- Content required and must not be blank.
- Seller cannot modify buyer rating/comment.

## 6. Database Impact

- Read `reviews`.
- Read seller/shop ownership context.
- Insert `review_replies`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Seller ownership check required.

## 9. Failure Cases

- Review not found -> 404.
- Seller not owner -> 403/404.
- Review hidden -> 409.
- Reply already exists -> 409.
- Empty content -> 400.

## 10. Acceptance Criteria

- Seller replies only own shop review.
- Duplicate reply is blocked.
- Reply appears in public review response when review visible.

