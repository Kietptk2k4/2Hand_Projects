# Functional Requirement - Auto Complete Delivered Order

## 1. Feature Overview

System job tu dong complete order items da `DELIVERED` sau thoi gian cho phep neu buyer khong confirm thu cong. MVP default la `DELIVERED + 7 days => COMPLETED`.

## 2. Actors

- **System:** Scheduled/background job.
- **Buyer:** Co order delivered nhung chua confirm.
- **Seller:** Duoc hoan tat fulfillment sau auto complete.

## 3. Scope

**In Scope:**

- Find delivered order items older than configured window.
- Mark order items `COMPLETED`.
- Mark COD payment `PAID` if MVP policy allows.
- Complete order if all items completed and payment paid.
- Write histories/outbox events.

**Out of Scope:**

- Dispute/refund hold.
- Seller payout settlement.

## 4. Trigger

- Scheduled job hourly/daily.
- Configured completion window, default 7 days after delivery.

## 5. Business Rules

- Only order items `DELIVERED` are eligible.
- Skip items under future dispute/refund hold.
- Order completed iff all order items `COMPLETED` and payment status `PAID`.
- Shipment `DELIVERED` alone does not immediately complete order.
- Job must be idempotent.

## 6. Database Impact

- Read/update `order_items`.
- Read/update `payments` for COD policy.
- Read/update `orders`.
- Insert status histories and outbox events.

## 7. Transaction

- Required per order/order item batch.
- Lock order/order item rows before transition.

## 8. Security

- System/internal job only.
- No public API.

## 9. Failure Cases

- Payment not paid and not COD-completable -> do not complete order.
- Order already completed -> no-op.
- Some items not completed -> order remains processing.

## 10. Acceptance Criteria

- Delivered items older than window become completed.
- COD payment can be marked paid according to MVP policy.
- Order completes only when invariant is satisfied.
- Retry is safe and idempotent.

