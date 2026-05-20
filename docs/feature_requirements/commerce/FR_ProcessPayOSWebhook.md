# Functional Requirement - Process PayOS Webhook

## 1. Feature Overview

Xu ly webhook tu payOS de cap nhat payment/order/inventory trong Commerce Service. Webhook la source-of-truth cho payment success; redirect success khong duoc mark payment paid.

## 2. Actors

- **payOS:** Gui webhook.
- **System:** Verify signature, log payload va update domain state.

## 3. Scope

**In Scope:**

- Verify webhook signature.
- Log webhook payload.
- Idempotently update payment status.
- Update order/payment status and inventory.
- Write histories/outbox events.

**Out of Scope:**

- payOS checkout URL creation.
- Refund.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/payments/webhooks/payos`

**Auth:** Provider signature, not JWT.

## 5. Business Rules

- Always store webhook log with signature validity.
- Invalid signature must not update domain state.
- Success event:
  - `payments.status = PAID`
  - `orders.payment_status = PAID`
  - `orders.status = PROCESSING`
  - settle reservation by decreasing `reserved_quantity`
- Failed/cancelled event uses payment failure flow.
- Duplicate webhook must be idempotent.

## 6. Database Impact

- Insert `payment_webhook_logs`.
- Read/update `payments`.
- Update `orders`.
- Update `product_inventories`.
- Insert histories and outbox events.

## 7. Transaction

- Required for domain processing.
- Lock payment row before terminal transition.

## 8. Security

- Verify payOS signature.
- Do not log provider secrets.
- Do not trust redirect result.

## 9. Failure Cases

- Invalid signature -> log and no state update.
- Payment not found -> log unprocessed.
- Duplicate webhook -> no-op.
- Race with expiration -> first terminal transition wins.

## 10. Acceptance Criteria

- Valid success webhook marks payment paid and order processing.
- Invalid webhook does not mutate payment/order.
- Duplicate webhook does not double-settle inventory.
- Webhook payload is logged for debug.

