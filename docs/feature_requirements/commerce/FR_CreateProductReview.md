# Functional Requirement - Create Product Review

## 1. Feature Overview

Cho phep buyer tao review cho product sau khi order item da `COMPLETED`. Review gan voi `order_item_id`, buyer va seller; mot order item chi co toi da mot review.

## 2. Actors

- **Buyer:** Tao review.
- **System:** Validate purchase/completion and update rating summary.

## 3. Scope

**In Scope:**

- Create review with rating/comment.
- Validate review permission.
- Optional attach media if included by API policy.
- Update rating summary.

**Out of Scope:**

- Seller reply.
- Review moderation.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/reviews`

**Auth:** Required (JWT)

**Request body:**

- `order_item_id`
- `rating`
- `comment` optional

## 5. Business Rules

- Order item must be `COMPLETED`.
- Buyer must match order buyer.
- One review per order item.
- Rating must be between 1 and 5.
- `seller_id` derived from order item, not client body.
- Review starts as `VISIBLE`.

## 6. Database Impact

- Read `order_items`, `orders`.
- Insert `reviews`.
- Optional insert `review_media`.
- Update rating summary if stored.
- Insert outbox event.

## 7. Transaction

- Required.

## 8. Security

- JWT required.
- Ownership derived from order buyer.

## 9. Failure Cases

- Order item not found/not owned -> 404.
- Order item not completed -> 409.
- Duplicate review -> 409.
- Invalid rating -> 400.

## 10. Acceptance Criteria

- Buyer can review own completed order item.
- Buyer cannot review incomplete item.
- Duplicate review blocked.
- Rating summary updates.

