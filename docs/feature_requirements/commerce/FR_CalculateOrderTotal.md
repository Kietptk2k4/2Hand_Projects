# Functional Requirement - Calculate Order Total

## 1. Feature Overview

Tinh tong tien order dua tren selected cart items, active product price, quantity va shipping fee. Feature nay duoc dung trong checkout va co the dung cho preview checkout.

## 2. Actors

- **Buyer:** Xem tong tien truoc khi dat hang.
- **System:** Calculate subtotal, shipping fee va final amount.

## 3. Scope

**In Scope:**

- Calculate item subtotal.
- Use active price/sale price.
- Calculate shipping fee by seller group.
- Allocate shipping fee to order items.

**Out of Scope:**

- Voucher/promotion.
- Refund.
- Payment provider fee.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/checkout/quote`

**Auth:** Required (JWT)

**Request body:**

- `cart_item_ids`
- `address_id`
- `shipment_type` optional

**Response data:**

- `items[]`
  - `cart_item_id`
  - `unit_price`
  - `quantity`
  - `item_total`
  - `shipping_fee_allocated`
- `total_amount`
- `shipping_fee`
- `final_amount`
- `seller_shipping_groups[]`

## 5. Business Rules

- Use current active price.
- `effective_price = sale_price` if present, else `price`.
- `item_total = effective_price * quantity`.
- `total_amount = sum(item_total)`.
- `shipping_fee = sum(fee per seller group)`.
- `final_amount = total_amount + shipping_fee`.
- Amounts must be >= 0.
- Quote is not a guarantee; checkout must recalculate/revalidate.

## 6. Database Impact

- Read `cart_items`.
- Read `products`, `product_prices`, `product_inventories`, `seller_shipping_profiles`, `user_addresses`.
- No write.

## 7. Transaction

- Read-only.
- No inventory lock required for quote.

## 8. Security

- JWT required.
- Buyer can quote only own cart items and address.

## 9. Failure Cases

- Cart item/address not owned -> 404.
- Active price missing -> 409.
- Shipping profile missing -> 409.
- Shipping provider unavailable -> fallback mock if configured or 503.

## 10. Acceptance Criteria

- Quote returns subtotal, shipping fee and final amount.
- Quote uses current active prices.
- Quote does not reserve stock.
- Checkout recalculates totals before creating order.

