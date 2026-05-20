# Functional Requirement - Archive Product

## 1. Feature Overview

Cho phep seller archive product cua shop minh. Archive la seller soft delete, lam product khong buyer-visible va khong checkout duoc.

## 2. Actors

- **Seller:** Archive product.
- **System:** Update product status and invalidate carts if needed.

## 3. Scope

**In Scope:**

- Set product status `ARCHIVED`.
- Prevent future add cart/checkout.
- Trigger cart invalidation/event if configured.

**Out of Scope:**

- Physical delete product.
- Admin remove.
- Restore archived product.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/products/{productId}/archive`

**Auth:** Required (JWT seller)

## 5. Business Rules

- Seller can archive only own product.
- Allowed from `DRAFT`, `ACTIVE`, `PAUSED`, `OUT_OF_STOCK`.
- `REMOVED` product cannot be archived by seller.
- Archived product hidden from buyer discovery.
- Existing order snapshots remain unchanged.

## 6. Database Impact

- Read/update `products`.
- Optional update related `cart_items` to `INVALID_PRODUCT`.
- Insert outbox event.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership check by seller/shop.

## 9. Failure Cases

- Product not found/not owned -> 404.
- Product removed -> 409.
- Already archived -> idempotent success or 409 by API policy.

## 10. Acceptance Criteria

- Seller archives own product.
- Archived product not visible to buyer and cannot checkout.
- Existing orders are preserved.

