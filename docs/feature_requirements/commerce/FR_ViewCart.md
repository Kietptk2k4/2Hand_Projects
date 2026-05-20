# Functional Requirement - View Cart

## 1. Feature Overview

Cho phep buyer xem cart hien tai voi danh sach cart items, gia hien tai, stock summary, product availability va cac warning can thiet truoc checkout. View cart co the lazy-validate item status nhung khong reserve inventory.

## 2. Actors

- **Buyer:** Xem cart cua minh.
- **System:** Enrich cart items voi product/shop/price/inventory moi nhat.

## 3. Scope

**In Scope:**

- Get-or-create cart rong neu buyer chua co cart.
- Load non-removed cart items.
- Return current product price, stock, status va warnings.
- Compute subtotal va `can_checkout`.

**Out of Scope:**

- Checkout.
- Inventory reservation.
- Shipping fee final.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/cart`

**Auth:** Required (JWT)

**Response data:**

- `cart_id`
- `items[]`
  - `cart_item_id`
  - `product_id`
  - `seller_id`
  - `shop_id`
  - `product_name`
  - `image_url`
  - `quantity`
  - `status`
  - `effective_price`
  - `in_stock`
  - `available_quantity`
  - `unavailable_reason`
- `summary`
  - `active_item_count`
  - `invalid_item_count`
  - `subtotal`
  - `can_checkout`
  - `warnings`

## 5. Business Rules

- Buyer chi xem cart cua minh.
- Removed items khong hien trong active cart.
- Cart view khong reserve stock.
- Cart view phai reflect current product/shop/price/inventory.
- Product invalid -> item unavailable.
- Stock insufficient -> item `OUT_OF_STOCK`.
- Checkout van phai revalidate lai.

## 6. Database Impact

- Read/insert `carts` neu chua co cart.
- Read `cart_items`.
- Read `products`, `seller_shops`, `product_prices`, `product_inventories`, `product_media`.
- Optional update `cart_items.status` lazy.

## 7. Transaction

- Read-only neu khong lazy-update status.
- Write transaction required neu get-or-create cart hoac update item status.

## 8. Security

- JWT required.
- `user_id` lay tu JWT.

## 9. Failure Cases

- Unauthenticated -> 401.
- DB failure -> 500.

## 10. Acceptance Criteria

- Buyer xem duoc cart cua minh.
- Cart item response co price/stock/status moi nhat.
- Removed items khong hien trong active cart.
- Inventory khong thay doi khi view cart.

