# Functional Requirement - Validate Cart Items

## 1. Feature Overview

Cho phep system validate cart items truoc checkout hoac khi buyer view cart, nham phat hien product invalid, shop invalid, price missing/changed va stock insufficient. Feature nay khong tao order va khong reserve stock.

## 2. Actors

- **Buyer:** Nhan ket qua validation de biet item nao co the checkout.
- **System:** Revalidate cart items dua tren current catalog/inventory state.

## 3. Scope

**In Scope:**

- Validate selected/all cart items.
- Update item status `ACTIVE`, `OUT_OF_STOCK`, `INVALID_PRODUCT` neu policy cho phep.
- Return warnings and blocking reasons.

**Out of Scope:**

- Checkout.
- Inventory reservation.
- Payment.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/cart/validate`

**Auth:** Required (JWT)

**Request body:**

- `cart_item_ids` optional; neu empty validate all active items.

**Response data:**

- `valid_items[]`
- `invalid_items[]`
  - `cart_item_id`
  - `reason`
  - `current_status`
- `can_checkout`

## 5. Business Rules

- Buyer chi validate cart cua minh.
- `REMOVED` item khong checkout duoc.
- Product phai `ACTIVE`.
- Shop phai `ACTIVE`.
- Active price phai ton tai.
- `stock_quantity >= quantity`.
- Vacation shop co the block checkout theo MVP policy.
- Validation khong reserve stock.

## 6. Database Impact

- Read `cart_items`.
- Read product/shop/category/price/inventory.
- Optional update `cart_items.status`.

## 7. Transaction

- Read-only neu chi return result.
- Write transaction required neu persist status changes.

## 8. Security

- JWT required.
- Cart item ownership required.

## 9. Failure Cases

- Cart item not owned/not found -> 404.
- Empty cart -> valid response with `can_checkout = false`.

## 10. Acceptance Criteria

- Invalid product/shop/stock duoc detect.
- Validation result noi ro reason.
- Inventory khong thay doi.
- Checkout khong duoc bo qua validation nay.

