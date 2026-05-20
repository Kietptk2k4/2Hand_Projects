# Functional Requirement - Cancel Order

## 1. Feature Overview

Cho phep buyer huy order khi order chua bat dau fulfillment. Cancel order phai release reserved inventory neu payment chua success.

## 2. Actors

- **Buyer:** Huy order cua minh.
- **System:** Validate cancellable, update statuses, release inventory.

## 3. Scope

**In Scope:**

- Cancel order in `CREATED/AWAITING_PAYMENT`.
- Cancel pending payment.
- Cancel pending order items.
- Release reserved inventory.

**Out of Scope:**

- Refund after payment/shipment.
- Cancel after pickup/shipping.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/orders/{orderId}/cancel`

**Auth:** Required (JWT)

**Request body:**

- `reason` optional.

## 5. Business Rules

- Buyer can cancel only own order.
- Allowed if `orders.status IN (CREATED, AWAITING_PAYMENT)`.
- Shipment must not exist or all shipments `PENDING`.
- Not allowed if any shipment `PICKING_UP`, `READY_TO_SHIP`, `SHIPPED`, `DELIVERED`.
- Release reserved inventory exactly once.

## 6. Database Impact

- Update `orders`.
- Update `order_items`.
- Update `payments`.
- Update `product_inventories`.
- Insert histories/outbox events.

## 7. Transaction

- Required.
- Must lock order/payment/inventory rows as needed.

## 8. Security

- JWT required.
- Ownership check by `buyer_id`.

## 9. Failure Cases

- Order not found/not owned -> 404.
- Not cancellable -> 409.
- Inventory release conflict -> 500/support.

## 10. Acceptance Criteria

- Cancellable order becomes `CANCELLED`.
- Reserved stock is released.
- Order with started shipment cannot be cancelled.
- Duplicate cancel is idempotent or returns clear conflict.

