# Functional Requirement - Remove Product By Admin

## 1. Feature Overview

Cho phep admin remove product vi pham. Product `REMOVED` khong buyer-visible, khong add cart/checkout duoc va seller khong republish duoc.

## 2. Actors

- **Admin/Moderator:** Remove product.
- **System:** Update product status and invalidate availability.

## 3. Scope

**In Scope:**

- Set product status `REMOVED`.
- Invalidate related active cart items if synchronous.
- Emit moderation event.

**Out of Scope:**

- Physical delete product.
- Refund existing orders.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/admin/products/{productId}/remove`

**Auth:** Required (JWT admin permission)

**Request body:**

- `reason`

## 5. Business Rules

- Admin permission required.
- Removed product hidden from discovery.
- Removed product cannot be checkout.
- Seller cannot restore/republish removed product.
- Existing order item snapshots remain unchanged.

## 6. Database Impact

- Read/update `products`.
- Optional update `cart_items.status = INVALID_PRODUCT`.
- Insert outbox event.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT admin required.
- Permission such as `COMMERCE_PRODUCT_REMOVE` required.

## 9. Failure Cases

- Missing permission -> 403.
- Product not found -> 404.
- Already removed -> idempotent success or 409.

## 10. Acceptance Criteria

- Admin can remove product with permission.
- Removed product no longer visible/purchasable.
- Seller cannot republish removed product.

