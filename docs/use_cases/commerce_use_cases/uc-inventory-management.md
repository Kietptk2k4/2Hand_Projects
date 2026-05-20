# UC - Inventory Management

## 1. Overview

Use case nay mo ta nghiep vu quan ly inventory va stock reservation. Inventory la invariant quan trong cua checkout: cart khong reserve stock, checkout moi reserve stock, payment success settle reservation, payment fail/cancel/expire release reservation.

## 2. Actors

- **Seller:** Cap nhat stock quantity va low stock threshold.
- **Buyer:** Indirectly reserve stock khi checkout.
- **System:** Settle/release reservation, sync out-of-stock/cart.

## 3. Related Data

- `product_inventories`
- `products`
- `cart_items`
- `orders`
- `order_items`
- `payments`
- `outbox_events`

## 4. Business Rules

- `stock_quantity >= 0`.
- `reserved_quantity >= 0`.
- `low_stock_threshold >= 0`.
- Checkout reserve:
  - `stock_quantity -= quantity`
  - `reserved_quantity += quantity`
- Payment success:
  - `reserved_quantity -= quantity`
- Payment fail/cancel/expire:
  - `reserved_quantity -= quantity`
  - `stock_quantity += quantity`
- Payment success never decreases `stock_quantity` again.
- Checkout must lock inventory rows or use conditional update to avoid oversell.

## 5. Sub-Use Cases

### 5.1. Update Stock Quantity

**Preconditions:** Seller owns product.

**Main Flow:**

1. Seller submits new stock quantity/threshold.
2. System validates ownership and non-negative values.
3. System updates `product_inventories`.
4. System updates product status `ACTIVE/OUT_OF_STOCK` if needed.
5. System writes event if needed.

**Exception Flow:** Seller not owner -> 403/404; negative stock -> 400.

### 5.2. Reserve Inventory

**Preconditions:** Checkout selected valid product and quantity.

**Main Flow:**

1. System locks inventory rows sorted by product id.
2. System validates `stock_quantity >= quantity`.
3. System decreases `stock_quantity`.
4. System increases `reserved_quantity`.
5. System creates order/payment in same transaction.

**Exception Flow:** Insufficient stock -> 409; concurrent conflict -> retry/reject.

### 5.3. Settle Reservation

**Preconditions:** Payment success or COD fulfillment commit policy.

**Main Flow:**

1. System loads order items.
2. System locks inventory rows.
3. System decreases `reserved_quantity` by item quantities.
4. System writes outbox event.

**Exception Flow:** Duplicate payment success -> no-op; reserved quantity insufficient -> data corruption alert.

### 5.4. Release Reservation

**Preconditions:** Payment failed/cancelled/expired or order cancelled before fulfillment.

**Main Flow:**

1. System loads order items.
2. System locks inventory rows.
3. System decreases `reserved_quantity`.
4. System increases `stock_quantity`.
5. System writes outbox event.

**Exception Flow:** Already released -> no-op; shipment already started -> reject normal release.

### 5.5. Check Low Stock And Out Of Stock

**Main Flow:**

1. System checks inventory values.
2. If `stock_quantity = 0`, mark product `OUT_OF_STOCK`.
3. If `stock_quantity > 0` and product otherwise valid, product can return `ACTIVE`.
4. If `stock_quantity <= low_stock_threshold`, mark/show low stock warning.

### 5.6. Sync Cart Item Status

**Main Flow:**

1. System finds cart items for changed product.
2. If product/shop invalid, set `INVALID_PRODUCT`.
3. If stock insufficient, set `OUT_OF_STOCK`.
4. If stock sufficient and product/shop valid, set `ACTIVE`.

## 6. Acceptance Criteria

- Checkout cannot oversell under concurrent requests.
- Cart operation never changes inventory.
- Payment success only reduces reserved quantity.
- Payment failure/expiration releases reserved stock exactly once.
- Product/cart out-of-stock status reflects inventory.

