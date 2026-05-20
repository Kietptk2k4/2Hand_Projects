# Functional Requirement - Handle Order Completed Notification

## 1. Feature Overview

Notify buyer when Commerce publishes `ORDER_COMPLETED`.

## 2. Actors

- **Commerce Service:** Publishes order completion event.
- **Notification Service:** Creates notification.
- **Buyer:** Recipient.

## 3. Scope

**In Scope:**

- Create order completed in-app notification.
- Send push by default.
- Include safe prompt for review if policy allows.

**Out of Scope:**

- Completing order.
- Creating review.
- Payment settlement.

## 4. Event Contract

Required payload:

- `order_id`
- `buyer_id`
- `completed_at` optional
- `reviewable_items` optional

## 5. Business Rules

- Commerce owns order completion truth.
- Default channels: in-app + push.
- Reference: `ORDER/order_id`.
- Email is not sent by default in MVP.
- Duplicate event must not create duplicate notification.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing buyer id -> failed.
- Missing order id -> failed.
- Push failure -> retry by delivery policy.

## 8. Acceptance Criteria

- Buyer receives order completed notification.
- Notification can deep link to order detail.
- Retried/duplicate event is idempotent.

