# Functional Requirement - Handle Payment Failed Notification

## 1. Feature Overview

Notify buyer when Commerce publishes `PAYMENT_FAILED`.

## 2. Actors

- **Commerce Service:** Publishes payment failure event.
- **Notification Service:** Notifies buyer.
- **Buyer:** Recipient.

## 3. Scope

**In Scope:**

- Create in-app notification.
- Send push by default.
- Provide safe failure summary.

**Out of Scope:**

- Payment retry creation.
- Payment status mutation.
- Email by default.

## 4. Event Contract

Required payload:

- `payment_id`
- `order_id`
- `buyer_id`
- `failure_reason` optional user-safe

## 5. Business Rules

- Default channels: in-app + push.
- Email is not sent by default in MVP.
- Failure reason must be user-safe and not include provider internal raw error.
- Reference: `PAYMENT/payment_id` or `ORDER/order_id`.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing buyer id -> failed.
- Unsafe failure reason -> sanitize or omit.
- Duplicate event -> idempotent.

## 8. Acceptance Criteria

- Buyer receives payment failed notification.
- Provider internals are not exposed.
- Commerce state is not mutated.

