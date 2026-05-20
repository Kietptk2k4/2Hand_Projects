# Functional Requirement - Auto Cancel Unpaid Order

## 1. Feature Overview

System job tu dong huy order chua thanh toan sau thoi gian cho phep. Feature nay ap dung chu yeu cho payOS order dang `AWAITING_PAYMENT` va payment `PENDING`, dong thoi release reserved inventory.

## 2. Actors

- **System:** Scheduled/background job.
- **Buyer:** Bi anh huong khi order qua han thanh toan.

## 3. Scope

**In Scope:**

- Find stale unpaid orders.
- Cancel order/payment if still pending.
- Release reserved inventory.
- Write histories and outbox events.

**Out of Scope:**

- COD payment expiration by age.
- Refund after payment paid.
- Shipment cancellation after fulfillment started.

## 4. Trigger

- Scheduled job every configured interval.
- Payment/order TTL expired.

## 5. Business Rules

- Only orders `CREATED/AWAITING_PAYMENT` with payment `PENDING` are eligible.
- Do not auto-cancel COD only because payment is old.
- Do not cancel if shipment has started.
- Release reserved inventory exactly once.
- Job must be idempotent.

## 6. Database Impact

- Read/update `orders`.
- Read/update `payments`.
- Update `order_items`.
- Update `product_inventories`.
- Insert order/payment status histories.
- Insert outbox events.

## 7. Transaction

- Required per batch/order.
- Lock order/payment rows before transition.

## 8. Security

- System/internal job only.
- No public API.

## 9. Failure Cases

- Payment already paid -> no-op.
- Order already terminal -> no-op.
- Shipment already started -> skip and log.
- Inventory release conflict -> log support error.

## 10. Acceptance Criteria

- Expired unpaid payOS orders are cancelled.
- Reserved stock is released once.
- COD orders are not expired solely by age.
- Job retry does not duplicate state changes.

