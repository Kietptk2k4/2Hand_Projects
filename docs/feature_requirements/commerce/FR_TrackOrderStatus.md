# Functional Requirement - Track Order Status

## 1. Feature Overview

Cho phep buyer theo doi trang thai order, payment va fulfillment cua order theo lifecycle Commerce.

## 2. Actors

- **Buyer:** Theo doi order status.
- **System:** Return status and timeline.

## 3. Scope

**In Scope:**

- View order current status.
- View payment status.
- View item/shipment status summary.
- Optional status history timeline.

**Out of Scope:**

- Mutate order status.
- Shipment provider polling.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/orders/{orderId}/status`

**Auth:** Required (JWT)

## 5. Business Rules

- Buyer can track only own order.
- Order completed iff all order items completed and payment paid.
- Shipment delivered does not automatically mean order completed.

## 6. Database Impact

- Read `orders`.
- Read `order_items`.
- Read `payments`.
- Read `shipments`.
- Optional read `order_status_history`, `payment_status_history`, `shipment_status_history`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Ownership check required.

## 9. Failure Cases

- Order not found/not owned -> 404.

## 10. Acceptance Criteria

- Buyer sees accurate order/payment/shipment statuses.
- Delivered and completed states are distinguished.
- Other user's order status inaccessible.

