# Functional Requirement - Checkout From Cart

## 1. Feature Overview

Cho phep buyer checkout selected cart items de tao order, order items va payment. Checkout la diem reserve inventory va snapshot product/shop/price data.

## 2. Actors

- **Buyer:** Checkout cart items.
- **System:** Validate cart/product/shop/price/stock/address, reserve inventory va tao order/payment.

## 3. Scope

**In Scope:**

- Checkout selected cart items.
- Validate item availability.
- Calculate amount/shipping fee.
- Reserve inventory.
- Create order, order items, payment.
- Return payment instruction.

**Out of Scope:**

- Payment webhook processing.
- Shipment creation chi tiet.
- Refund/dispute.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/checkout`

**Auth:** Required (JWT)

**Request body:**

- `cart_item_ids`
- `address_id`
- `payment_method`: `COD` or `PAYOS`
- `shipment_type` optional
- `idempotency_key` recommended

**Response data:**

- `order_id`
- `payment_id`
- `payment_method`
- `payment_status`
- `order_status`
- `final_amount`
- `payos_checkout_url` nullable

## 5. Business Rules

- Buyer can checkout only own cart items.
- Cart items must not be `REMOVED`.
- Product must be `ACTIVE`.
- Shop must be `ACTIVE` and not vacation-blocked.
- Active price must exist.
- `stock_quantity >= quantity`.
- Address must belong to buyer.
- Checkout reserves inventory atomically.
- Order item snapshots must be created.

## 6. Database Impact

- Read `cart_items`, `products`, `seller_shops`, `product_prices`, `product_inventories`, `user_addresses`.
- Update `product_inventories`.
- Insert `orders`, `order_items`, `payments`.
- Insert status histories/outbox events.

## 7. Transaction

- Required.
- Must include inventory reservation + order/payment creation + outbox insert.
- Use inventory row locks or conditional updates.

## 8. Security

- JWT required.
- Buyer ownership check for cart items and address.

## 9. Failure Cases

- Invalid cart item/address -> 404/400.
- Product/shop unavailable -> 409.
- Insufficient stock -> 409.
- Payment method invalid -> 400.
- Duplicate idempotency key -> return existing checkout result.

## 10. Acceptance Criteria

- Valid checkout creates order/payment/order items.
- Inventory is reserved exactly once.
- Invalid cart item/product/shop/stock/address blocks checkout.
- Order item snapshots preserve purchase-time data.

