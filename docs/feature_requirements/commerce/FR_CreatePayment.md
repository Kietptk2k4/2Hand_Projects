# Functional Requirement - Create Payment

## 1. Feature Overview

Tao payment record cho order trong checkout. Commerce Service own payment trong MVP, ho tro `COD` va `PAYOS`.

## 2. Actors

- **Buyer:** Trigger payment creation through checkout.
- **System:** Create payment for order.

## 3. Scope

**In Scope:**

- Create one payment per order.
- Store payment method, amount, payer and status.
- Write payment history/outbox.

**Out of Scope:**

- payOS checkout URL creation.
- Webhook processing.
- Refund.

## 4. API Contract

**Endpoint:** Internal use case inside checkout.

**Input:**

- `order_id`
- `payer_id`
- `amount`
- `payment_method`
- `currency`
- `idempotency_key` optional

## 5. Business Rules

- `payments.order_id` unique.
- `amount > 0`.
- Amount must equal `orders.final_amount`.
- Initial status `PENDING`.
- Method must be `COD` or `PAYOS`.
- payOS provider fields nullable at creation.

## 6. Database Impact

- Insert `payments`.
- Insert `payment_status_history`.
- Insert outbox event.

## 7. Transaction

- Must be inside checkout transaction.

## 8. Security

- `payer_id` derived from buyer/order context.
- Do not accept arbitrary payer id from client.

## 9. Failure Cases

- Duplicate payment for order -> 409/internal conflict.
- Invalid amount/method -> 400/internal validation.

## 10. Acceptance Criteria

- Payment created once per order.
- Initial payment status is pending.
- Payment amount matches order final amount.
- Payment history/outbox are written.

