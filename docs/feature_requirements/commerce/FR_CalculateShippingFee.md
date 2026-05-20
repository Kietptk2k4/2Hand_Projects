# Functional Requirement - Calculate Shipping Fee

## 1. Feature Overview

Tinh phi shipping cho selected cart/order items theo seller group truoc checkout. MVP co the dung GHN fee API hoac mock calculator neu chua tich hop GHN.

## 2. Actors

- **Buyer:** Xem phi ship truoc checkout.
- **System:** Tinh fee theo pickup/destination/weight.
- **GHN:** Provider tinh fee neu enabled.

## 3. Scope

**In Scope:**

- Calculate shipping fee per seller group.
- Use buyer address and seller pickup profile.
- Use product weight and quantity.
- Return estimated delivery date if available.

**Out of Scope:**

- Create shipment.
- Payment.
- Refund shipping fee.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/shipping/fee`

**Auth:** Required (JWT)

**Request body:**

- `cart_item_ids`
- `address_id`
- `shipment_type`

**Response data:**

- `seller_groups[]`
  - `seller_id`
  - `shop_id`
  - `shipping_fee`
  - `shipping_fee_origin`
  - `estimated_delivery_date`
  - `shipment_type`
- `total_shipping_fee`

## 5. Business Rules

- Buyer can calculate fee only for own cart items/address.
- Items grouped by seller.
- Seller must have shipping profile for provider-based fee.
- Weight = sum product weight * quantity.
- If GHN unavailable and mock enabled, return deterministic mock fee.
- Fee quote is not final guarantee; checkout recalculates.

## 6. Database Impact

- Read `cart_items`.
- Read `products`.
- Read `user_addresses`.
- Read `seller_shipping_profiles`.

## 7. Transaction

- Read-only.
- No inventory lock.

## 8. Security

- JWT required.
- Ownership check for cart items/address.

## 9. Failure Cases

- Address/cart item not owned -> 404.
- Missing seller shipping profile -> 409.
- GHN timeout -> 503 or fallback mock.

## 10. Acceptance Criteria

- Fee returned per seller group.
- Total shipping fee is sum of seller group fees.
- Quote does not create order/shipment.
- Checkout recalculates before final order.

