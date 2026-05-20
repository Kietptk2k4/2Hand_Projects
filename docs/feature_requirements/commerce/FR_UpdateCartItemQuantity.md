# Functional Requirement - Update Cart Item Quantity

## 1. Feature Overview

Cho phep buyer cap nhat so luong cua mot cart item trong cart cua minh. Feature phai revalidate product/shop/stock hien tai nhung khong reserve inventory.

## 2. Actors

- **Buyer:** Cap nhat quantity cart item.
- **System:** Validate ownership, quantity va availability.

## 3. Scope

**In Scope:**

- Update quantity.
- Revalidate product/shop/inventory.
- Update cart item status neu can.

**Out of Scope:**

- Remove item.
- Checkout.
- Inventory reservation.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/cart/items/{cartItemId}`

**Auth:** Required (JWT)

**Request body:**

- `quantity`

**Response data:**

- updated cart item.
- cart summary optional.

## 5. Business Rules

- Buyer can update only own cart item.
- Quantity must be > 0.
- `REMOVED` item cannot be updated unless restore endpoint explicitly supports it.
- Product/shop must still be valid for item to remain `ACTIVE`.
- If stock insufficient, API can reject or mark `OUT_OF_STOCK`; MVP recommended reject direct update above stock.
- Updating quantity does not change inventory.

## 6. Database Impact

- Read `cart_items` scoped by buyer cart.
- Read product/shop/inventory/price for validation.
- Update `cart_items.quantity`.
- Update `cart_items.status` if needed.

## 7. Transaction

- Write transaction required.
- No inventory lock required.

## 8. Security

- JWT required.
- Cart item ownership must be checked through cart user id.

## 9. Failure Cases

- Cart item not found/not owned -> 404.
- Quantity <= 0 -> 400.
- Item removed -> 409.
- Product invalid -> 409 or update status `INVALID_PRODUCT`.
- Stock insufficient -> 409.

## 10. Acceptance Criteria

- Buyer can update own active cart item quantity.
- Quantity <= 0 rejected.
- Other user's cart item cannot be updated.
- Inventory quantities remain unchanged.
- Stock/product invalidity is reflected in response/status.

