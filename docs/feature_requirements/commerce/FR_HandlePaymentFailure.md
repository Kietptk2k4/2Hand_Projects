# Functional Requirement - Handle Payment Failure

## 1. Feature Overview

Xu ly payment failed/cancelled/expired cho order dang cho thanh toan. Feature nay cap nhat payment/order status va release reserved inventory neu payment chua success.

## 2. Actors

- **System:** Xu ly provider failure, buyer cancel hoac expiration job.
- **Buyer:** Bi anh huong khi payment fail/cancel/expire.

## 3. Scope

**In Scope:**

- Mark payment `FAILED`, `CANCELLED`, or `EXPIRED`.
- Cancel awaiting order neu con du dieu kien.
- Release reserved inventory.
- Write status histories and outbox events.

**Out of Scope:**

- Refund after paid payment.
- Dispute handling.

## 4. Trigger

- payOS webhook failure/cancelled.
- Payment expiration job.
- Provider callback indicates failure.

## 5. Business Rules

- Only `PENDING` payment can transition to failure terminal state.
- If order is `CREATED/AWAITING_PAYMENT`, mark order `CANCELLED`.
- Release inventory exactly once:
  - `reserved_quantity -= quantity`
  - `stock_quantity += quantity`
- If payment already `PAID`, do not auto-fail; log conflict for support.

## 6. Database Impact

- Update `payments`.
- Update `orders`.
- Update `order_items` if cancelling.
- Update `product_inventories`.
- Insert `payment_status_history`, `order_status_history`, outbox events.

## 7. Transaction

- Required.
- Lock payment/order/inventory rows to avoid race with success webhook.

## 8. Security

- System/provider flow only.
- Provider webhook must be signature-verified before trusted status update.

## 9. Failure Cases

- Payment already terminal -> idempotent no-op or conflict log.
- Inventory release already done -> no-op if state proves released.
- Order no longer cancellable -> log support warning.

## 10. Acceptance Criteria

- Failed/cancelled/expired pending payment updates payment status.
- Awaiting order is cancelled.
- Reserved stock is released once.
- Paid payment is not overwritten by failure webhook.

