# Functional Requirement - Pause Product

## 1. Feature Overview

Cho phep seller tam dung ban product cua shop minh. Product `PAUSED` khong hien trong buyer discovery va khong add cart/checkout duoc.

## 2. Actors

- **Seller:** Pause product.
- **System:** Update status and invalidate availability.

## 3. Scope

**In Scope:**

- Set product status `PAUSED`.
- Block buyer discovery/add cart/checkout.
- Trigger cart invalidation if configured.

**Out of Scope:**

- Archive/delete product.
- Admin remove.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/products/{productId}/pause`

**Auth:** Required (JWT seller)

## 5. Business Rules

- Seller can pause only own product.
- Allowed from `ACTIVE` or `OUT_OF_STOCK`.
- Product `REMOVED/ARCHIVED` cannot be paused.
- Existing orders are not affected.
- Active cart items can become `INVALID_PRODUCT`.

## 6. Database Impact

- Read/update `products`.
- Optional update `cart_items`.
- Insert outbox event.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership check by seller/shop.

## 9. Failure Cases

- Product not found/not owned -> 404.
- Invalid current status -> 409.

## 10. Acceptance Criteria

- Seller pauses own active product.
- Paused product is hidden from buyer discovery.
- Paused product cannot be added to cart or checkout.

