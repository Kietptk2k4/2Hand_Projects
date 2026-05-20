# Functional Requirement - Add Product To Cart

## 1. Feature Overview

Cho phep buyer them product vao cart. Neu buyer chua co cart, system tao cart tu dong. Add to cart khong reserve stock; checkout moi la diem reserve inventory.

## 2. Actors

- **Buyer:** Them product vao cart.
- **System:** Validate product/shop/price/stock va upsert cart item.

## 3. Scope

**In Scope:**

- Add product to buyer cart.
- Get-or-create cart.
- Upsert cart item theo `(cart_id, product_id)`.
- Validate product purchasability co ban.

**Out of Scope:**

- Checkout.
- Inventory reservation.
- Shipping fee.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/cart/items`

**Auth:** Required (JWT)

**Request body:**

- `product_id`
- `quantity`

**Response data:**

- `cart_id`
- `cart_item_id`
- `product_id`
- `quantity`
- `status`
- current product summary.

## 5. Business Rules

- Buyer can add only product `ACTIVE`.
- Shop must be `ACTIVE`.
- Quantity must be > 0.
- Active price must exist.
- If cart item exists, update/reactivate instead of duplicate.
- If item was `REMOVED`, adding same product can reactivate it.
- Add to cart does not change `product_inventories`.
- MVP recommended reject add when stock = 0.

## 6. Database Impact

- Read/insert `carts`.
- Read `products`, `seller_shops`, `product_prices`, `product_inventories`.
- Insert/update `cart_items`.

## 7. Transaction

- Write transaction required for cart create + cart item upsert.
- No inventory lock required.

## 8. Security

- JWT required.
- Buyer can modify only own cart.
- `seller_id` should be derived from product/shop, not trusted from client.

## 9. Failure Cases

- Product not found -> 404.
- Product/shop not purchasable -> 409.
- Quantity invalid -> 400.
- Active price missing -> 409.
- Unauthenticated -> 401.

## 10. Acceptance Criteria

- Product active can be added to cart.
- Same product does not create duplicate item.
- Quantity <= 0 rejected.
- Add to cart does not reserve stock.
- Invalid product/shop cannot be added.

