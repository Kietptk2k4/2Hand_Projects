# Functional Requirement - Confirm Order Received

## 1. Feature Overview

Cho phep buyer xac nhan da nhan hang khi shipment/order items da delivered. Confirmation chuyen order items delivered sang completed va voi COD thi mark payment paid.

## 2. Actors

- **Buyer:** Xac nhan da nhan hang.
- **System:** Complete delivered items and update payment/order state.

## 3. Scope

**In Scope:**

- Confirm received for buyer order.
- Mark delivered order items `COMPLETED`.
- Mark COD payment `PAID`.
- Complete order if all conditions met.

**Out of Scope:**

- Dispute/refund.
- Review creation.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/orders/{orderId}/confirm-received`

**Auth:** Required (JWT)

## 5. Business Rules

- Buyer can confirm only own order.
- Order items must be `DELIVERED`.
- Seller cannot confirm received for buyer.
- For COD, confirmation sets payment `PAID`.
- Order completed only if all items completed and payment paid.

## 6. Database Impact

- Update `order_items`.
- Update `payments` for COD.
- Update `orders` if completed.
- Insert histories/outbox events.

## 7. Transaction

- Required.

## 8. Security

- JWT required.
- Ownership check by `orders.buyer_id`.

## 9. Failure Cases

- Order not found/not owned -> 404.
- Items not delivered -> 409.
- Payment state invalid -> 409.

## 10. Acceptance Criteria

- Delivered items become completed after buyer confirm.
- COD payment becomes paid.
- Order completes only when all items completed and payment paid.
- Non-owner cannot confirm.

