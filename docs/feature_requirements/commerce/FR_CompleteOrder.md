# Functional Requirement - Complete Order

## 1. Feature Overview

Hoan tat order khi tat ca order items da completed va payment da paid. Feature nay co the duoc trigger boi buyer confirm received hoac background job auto-complete.

## 2. Actors

- **Buyer:** Trigger qua confirm received.
- **System:** Auto complete delivered order sau configured window.

## 3. Scope

**In Scope:**

- Recompute order completion eligibility.
- Mark order `COMPLETED`.
- Set `completed_at`.
- Write status history/outbox.

**Out of Scope:**

- Review creation.
- Payout/refund.

## 4. API Contract

**Endpoint:** Internal use case; optional `POST /commerce/api/v1/orders/{orderId}/complete` for system/admin only.

**Auth:** System/internal; buyer uses confirm-received endpoint.

## 5. Business Rules

- Order completed iff:
  - all `order_items.status = COMPLETED`
  - `orders.payment_status = PAID`
- Shipment `DELIVERED` alone is not enough.
- Completion should be idempotent.

## 6. Database Impact

- Read `orders`, `order_items`, `payments`.
- Update `orders.status = COMPLETED`, `completed_at`.
- Insert `order_status_history`.
- Insert outbox event.

## 7. Transaction

- Required.

## 8. Security

- Internal/system use case should not be public buyer mutation except through confirm received.

## 9. Failure Cases

- Payment not paid -> not complete.
- Some order items not completed -> not complete.
- Order already completed -> idempotent success.

## 10. Acceptance Criteria

- Order completes only when invariant satisfied.
- `completed_at` is set once.
- Duplicate complete call does not corrupt state.
- Outbox event emitted when transition occurs.

