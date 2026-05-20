# Functional Requirement - View Order Detail

## 1. Feature Overview

Cho phep buyer xem chi tiet order cua minh, bao gom order items snapshot, payment, shipment, tracking va shipping address snapshot.

## 2. Actors

- **Buyer:** Xem chi tiet order.
- **System:** Load aggregate detail.

## 3. Scope

**In Scope:**

- View order detail by id.
- Include item snapshots.
- Include payment status.
- Include shipment and address snapshot.

**Out of Scope:**

- Seller-scoped order detail.
- Admin support view.
- Order mutation.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/orders/{orderId}`

**Auth:** Required (JWT)

## 5. Business Rules

- Buyer can view only own order.
- Response uses order item snapshots, not current product mutable data.
- Include current order/payment/shipment statuses.

## 6. Database Impact

- Read `orders`.
- Read `order_items`.
- Read `payments`.
- Read `shipments`.
- Read `shipping_address_snapshots`.
- Optional read histories.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Ownership check by `orders.buyer_id`.

## 9. Failure Cases

- Order not found/not owned -> 404.

## 10. Acceptance Criteria

- Buyer sees full detail of own order.
- Other buyer order not accessible.
- Item data comes from snapshots.
- Payment/shipment summaries are included.

