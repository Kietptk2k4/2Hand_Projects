# Functional Requirement - View My Product Review

## 1. Feature Overview

Cho phep buyer xem review cua chinh minh cho mot san pham (neu co), phuc vu UI "Danh gia cua ban" tren trang review san pham.

## 2. Actors

- **Buyer:** Xem review ca nhan theo product.
- **System:** Lookup buyer review by product.

## 3. Scope

**In Scope:**

- Return `has_review` wrapper.
- Return rating/comment/status when review exists.
- Return `can_edit` for VISIBLE reviews.

**Out of Scope:**

- View other buyers reviews.
- Seller/admin moderation.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/me/products/{productId}/review`

**Auth:** Required (JWT buyer)

## 5. Business Rules

- Only current buyer reviews returned.
- `can_edit = true` only for `VISIBLE` owned reviews.
- Product must exist and be visible enough for review page context.

## 6. Database Impact

- Read `reviews`, `order_items`, `products`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Scoped to authenticated buyer.

## 9. Failure Cases

- Product not found -> 404.
- Unauthenticated -> 401.

## 10. Acceptance Criteria

- Buyer sees own review strip when review exists.
- Empty state when no review.
- Hidden review does not allow edit.

## 11. Related

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewMyProductReview-api-and-behavior.md`
- Public list: `FR_ViewProductReviews.md`