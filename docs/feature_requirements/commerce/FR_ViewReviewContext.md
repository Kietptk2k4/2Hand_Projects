# Functional Requirement - View Review Context

## 1. Feature Overview

Cho phep buyer lay context (snapshot san pham/order item) truoc khi tao review, bao gom trang thai item va co review hay chua.

## 2. Actors

- **Buyer:** Xem context de viet review.
- **System:** Load order item snapshots va review existence.

## 3. Scope

**In Scope:**

- Load context by `order_item_id`.
- Return product/shop snapshots, item status, `has_review`.

**Out of Scope:**

- Create/update review.
- Public review listing.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/reviews/context?order_item_id={uuid}`

**Auth:** Required (JWT buyer)

## 5. Business Rules

- Buyer must own order containing the order item.
- Read-only.
- Item must exist; completion check enforced by FE/create flow.

## 6. Database Impact

- Read `order_items`, `orders`, optional `reviews`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Ownership derived from order buyer.

## 9. Failure Cases

- Missing `order_item_id` -> 400.
- Item not found/not owned -> 404.

## 10. Acceptance Criteria

- Buyer can load review form context for own completed-eligible item.
- Duplicate review flag returned when review exists.
- Unauthorized users cannot access item context.

## 11. Related

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewReviewContext-api-and-behavior.md`
- Create: `FR_CreateProductReview.md`