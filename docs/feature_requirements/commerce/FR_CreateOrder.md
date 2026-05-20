# Functional Requirement - Create Order

## 1. Feature Overview

Tao order la phan core cua checkout, ghi nhan buyer order, order items snapshot va payment ban dau. Feature nay thuong duoc goi noi bo boi `CheckoutFromCart`, khong nhat thiet la public endpoint rieng.

## 2. Actors

- **Buyer:** Trigger order creation through checkout.
- **System:** Create order aggregate and related records.

## 3. Scope

**In Scope:**

- Create `orders`.
- Create `order_items`.
- Create initial status histories.
- Create payment placeholder through payment use case.

**Out of Scope:**

- Direct order without cart neu MVP khong support.
- Payment provider webhook.
- Shipment creation after order.

## 4. API Contract

**Endpoint:** Internal use case or `POST /commerce/api/v1/orders` if public direct-order is supported.

**Auth:** Required (JWT) if public.

**Input:** Validated checkout command.

**Response data:**

- `order_id`
- `status`
- `payment_status`
- `total_amount`
- `final_amount`
- `order_items[]`

## 5. Business Rules

- `buyer_id` from JWT/checkout context.
- `total_amount >= 0`.
- `final_amount >= 0`.
- Initial status:
  - payOS: `AWAITING_PAYMENT`, payment `PENDING`.
  - COD: `PROCESSING` or configured COD initial state, payment `PENDING`.
- Order item must snapshot product name, image, attributes, price, shop name.
- Multi-seller order items must keep `seller_id`.

## 6. Database Impact

- Insert `orders`.
- Insert `order_items`.
- Insert `order_status_history`.
- Insert outbox events.

## 7. Transaction

- Must run inside checkout transaction.
- Should be atomic with inventory reservation and payment creation.

## 8. Security

- Buyer identity from authenticated context.
- Do not trust client-provided seller/product snapshot fields.

## 9. Failure Cases

- Invalid amount -> 400/internal validation error.
- Snapshot data missing -> 409.
- DB insert failure -> rollback checkout.

## 10. Acceptance Criteria

- Order created with correct buyer, amount and initial status.
- Order items contain immutable snapshots.
- Order creation rolls back if payment/inventory part fails.
- Status history/outbox event are written.

