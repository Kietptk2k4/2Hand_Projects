# Functional Requirement - Reserve Inventory

## 1. Feature Overview

Reserve inventory la internal feature cua checkout, dam bao selected products duoc giu stock atomically khi buyer tao order. Cart khong reserve stock; checkout moi reserve stock.

## 2. Actors

- **Buyer:** Trigger reserve through checkout.
- **System:** Lock inventory and move available stock to reserved stock.

## 3. Scope

**In Scope:**

- Validate stock availability.
- Atomically decrease `stock_quantity`.
- Atomically increase `reserved_quantity`.
- Emit inventory reserved event.

**Out of Scope:**

- Seller manual stock update.
- Release/settle inventory after payment result.
- Multi-warehouse stock.

## 4. API Contract

**Endpoint:** Internal use case inside checkout.

**Input:**

- `order_id` or checkout command.
- selected product ids and quantities.

## 5. Business Rules

- Reserve only during checkout transaction.
- `stock_quantity >= quantity` required.
- If any item lacks stock, entire checkout fails.
- Reserve all selected products atomically.
- Use row lock or conditional update to avoid oversell.
- Deterministic lock order by `product_id` recommended.

## 6. Database Impact

- Read/update `product_inventories`.
- Insert outbox event `COMMERCE_INVENTORY_RESERVED`.

## 7. Transaction

- Required.
- Must be atomic with order/payment creation.

## 8. Security

- Internal use case.
- Caller checkout must already validate buyer/cart ownership.

## 9. Failure Cases

- Insufficient stock -> 409.
- Concurrent update conflict -> 409/retry.
- Missing inventory record -> 409.

## 10. Acceptance Criteria

- Checkout cannot oversell under concurrent requests.
- Reservation moves quantity from stock to reserved exactly once.
- Partial reservation rolls back if checkout fails.

