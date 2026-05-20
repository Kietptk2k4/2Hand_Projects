# Functional Requirement - Remove Cart Item

## 1. Feature Overview

Cho phep buyer xoa product khoi cart cua minh. MVP su dung soft state `cart_items.status = REMOVED` de giu trace va tranh hard delete bat buoc.

## 2. Actors

- **Buyer:** Remove item khoi cart.
- **System:** Validate ownership va update status.

## 3. Scope

**In Scope:**

- Remove cart item by id.
- Mark item `REMOVED`.
- Return updated cart summary optional.

**Out of Scope:**

- Delete product.
- Release inventory.
- Checkout cancellation.

## 4. API Contract

**Endpoint:** `DELETE /commerce/api/v1/cart/items/{cartItemId}`

**Auth:** Required (JWT)

**Response data:**

- success flag.
- optional cart summary.

## 5. Business Rules

- Buyer can remove only own cart item.
- Remove is idempotent if item already `REMOVED`.
- Remove cart item does not change inventory.
- Removed item excluded from active cart view.
- Adding same product later can reactivate row due unique `(cart_id, product_id)`.

## 6. Database Impact

- Read `cart_items` scoped by buyer cart.
- Update `cart_items.status = REMOVED`.
- Update `updated_at`.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership checked through `carts.user_id`.

## 9. Failure Cases

- Cart item not found/not owned -> 404 or idempotent success according API policy.
- DB failure -> 500.

## 10. Acceptance Criteria

- Buyer can remove own cart item.
- Removed item no longer appears in active cart.
- Removing item does not alter inventory.
- Other user's cart item cannot be removed.

