# UC - Cart Management

## 1. Overview

Use case nay mo ta cac nghiep vu buyer dung de quan ly cart: tao cart tu dong, them product, cap nhat quantity, remove item, xem cart va validate cart item status. Cart khong reserve inventory; cart chi la y dinh mua. Moi validation quan trong ve product, shop, price va stock phai duoc re-check trong checkout.

## 2. Actors

- **Buyer:** Quan ly cart cua minh.
- **System:** Sync cart item status khi product/shop/inventory thay doi.

## 3. Related Data

- `carts`
- `cart_items`
- `products`
- `product_prices`
- `product_inventories`
- `seller_shops`
- `shop_settings`
- `product_media`

## 4. Business Rules

- Moi user co toi da mot cart.
- Cart item unique theo `(cart_id, product_id)`.
- `quantity > 0`.
- Cart operation khong tru stock, khong tang reserved stock.
- Product add vao cart phai ton tai va co status hop le.
- Cart item status:
  - `ACTIVE`: item hop le.
  - `OUT_OF_STOCK`: stock khong du.
  - `REMOVED`: buyer da xoa khoi cart.
  - `INVALID_PRODUCT`: product/shop khong con hop le.
- Checkout chi duoc thuc hien voi cart items dang hop le tai thoi diem checkout.

## 5. Sub-Use Cases

### 5.1. Get Or Create Cart

**Goal:** Dam bao buyer luon co cart de them/xem san pham.

**Preconditions:**

- Buyer da dang nhap.

**Main Flow:**

1. Buyer thuc hien thao tac can cart.
2. System tim `carts` theo `user_id`.
3. Neu cart ton tai, system dung cart hien co.
4. Neu chua ton tai, system tao cart moi.
5. System tra cart.

**Exception Flow:**

- Concurrent create gay unique conflict -> reload cart theo `user_id`.

**Postconditions:**

- Buyer co mot cart duy nhat.

### 5.2. Add Product To Cart

**Goal:** Buyer them product vao cart.

**Preconditions:**

- Buyer da dang nhap.
- Product ton tai.

**Main Flow:**

1. Buyer gui `product_id` va `quantity`.
2. System get-or-create cart.
3. System load product, shop, active price va inventory.
4. System validate product `ACTIVE`, shop `ACTIVE`, quantity > 0, active price exists.
5. System upsert `cart_items`.
6. Neu item da ton tai, system update quantity/reactivate tuy status hien tai.
7. System tra cart item/cart summary moi.

**Exception Flow:**

- Product not found -> 404.
- Product not purchasable -> 409.
- Quantity invalid -> 400.
- Active price missing -> 409.
- Stock = 0 -> reject add hoac mark `OUT_OF_STOCK` theo API policy; MVP recommended reject add moi.

**Postconditions:**

- Cart item duoc tao/cap nhat.
- Inventory khong thay doi.

### 5.3. Update Cart Item Quantity

**Goal:** Buyer thay doi so luong product trong cart.

**Preconditions:**

- Buyer da dang nhap.
- Cart item thuoc cart cua buyer.

**Main Flow:**

1. Buyer gui cart item id va quantity moi.
2. System load cart item by `cart_id/user_id`.
3. System validate quantity > 0.
4. System revalidate product/shop/inventory.
5. System update quantity.
6. System update item status neu stock/product thay doi.
7. System tra cart moi.

**Exception Flow:**

- Cart item not found -> 404.
- Quantity <= 0 -> 400.
- Cart item `REMOVED` -> 409 unless endpoint explicitly restore.
- Product invalid -> update `INVALID_PRODUCT` hoac reject tuy API policy.
- Quantity vuot stock -> reject hoac mark `OUT_OF_STOCK`; MVP recommended reject direct update.

**Postconditions:**

- Cart item quantity/status duoc cap nhat.
- Inventory khong thay doi.

### 5.4. Remove Cart Item

**Goal:** Buyer xoa product khoi cart.

**Preconditions:**

- Buyer da dang nhap.
- Cart item thuoc cart cua buyer.

**Main Flow:**

1. Buyer request remove cart item.
2. System load cart item by id and owner.
3. System set `status = REMOVED`.
4. System tra success/cart summary.

**Exception Flow:**

- Cart item not found -> 404 hoac idempotent success theo API policy.

**Postconditions:**

- Cart item khong con nam trong active cart.
- Inventory khong thay doi.

### 5.5. View Cart

**Goal:** Buyer xem cart voi thong tin product/price/stock moi nhat.

**Preconditions:**

- Buyer da dang nhap.

**Main Flow:**

1. Buyer request view cart.
2. System load cart va non-removed cart items.
3. System load product, shop, price, inventory, media.
4. System tinh current status va warning cho tung item.
5. System tra cart detail va summary.

**Exception Flow:**

- Buyer chua co cart -> tao cart rong hoac tra cart rong.
- Product/shop da invalid -> item response co unavailable reason va status update neu policy cho phep.

**Postconditions:**

- Co the update cart item status lazy.
- Inventory khong thay doi.

### 5.6. Sync Cart Item Status

**Goal:** System cap nhat cart item khi product/shop/inventory thay doi.

**Preconditions:**

- Co product/shop/inventory thay doi hoac scheduled job.

**Main Flow:**

1. System tim cart items lien quan.
2. Neu product/shop invalid, set `INVALID_PRODUCT`.
3. Neu stock khong du, set `OUT_OF_STOCK`.
4. Neu stock du va product/shop valid, set `ACTIVE` cho item dang `OUT_OF_STOCK`.

**Exception Flow:**

- Job fail giua chung -> retry; checkout van revalidate synchronous.

**Postconditions:**

- Cart UX phan anh availability moi hon.

## 6. Response Guidance

Cart item response nen co:

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

Cart summary nen co:

- `active_item_count`
- `invalid_item_count`
- `subtotal`
- `can_checkout`
- `warnings`

## 7. Security

- JWT required.
- Buyer chi duoc thao tac cart cua minh.
- `user_id` lay tu JWT, khong lay tu request body.

## 8. Acceptance Criteria

- Buyer co toi da mot cart.
- Add same product khong tao duplicate cart item.
- Cart quantity khong duoc <= 0.
- Cart operation khong thay doi inventory.
- Cart view phan anh product/shop/stock/price moi nhat.
- Checkout van revalidate tat ca cart items.

