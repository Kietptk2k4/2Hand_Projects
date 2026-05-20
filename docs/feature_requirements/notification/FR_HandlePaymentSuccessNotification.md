# Functional Requirement - Handle Payment Success Notification

## 1. Feature Overview

Notify buyer when Commerce publishes `PAYMENT_SUCCESS`.

## 2. Actors

- **Commerce Service:** Publishes payment event.
- **Notification Service:** Notifies buyer.
- **Buyer:** Recipient.

## 3. Scope

**In Scope:**

- Create payment success in-app notification.
- Send push and email by default.
- Deduplicate duplicate payment events.

**Out of Scope:**

- Payment status update.
- Provider webhook verification.

## 4. Event Contract

Required payload:

- `payment_id`
- `order_id`
- `buyer_id`
- `amount`
- `payment_method`

## 5. Business Rules

- Commerce owns payment truth.
- Duplicate payment webhook-derived event must not notify twice.
- Reference: `PAYMENT/payment_id` or `ORDER/order_id`.
- Email content must not include provider secret/raw payload.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing buyer id -> failed.
- Missing payment/order reference -> failed.
- Email/push failure -> retry delivery by policy.

## 8. Acceptance Criteria

- Buyer receives payment success notification.
- Payment success email is sent when possible.
- Duplicate event is idempotent.

