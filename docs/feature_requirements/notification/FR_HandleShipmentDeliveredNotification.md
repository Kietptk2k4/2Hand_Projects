# Functional Requirement - Handle Shipment Delivered Notification

## 1. Feature Overview

Notify buyer when Commerce publishes `SHIPMENT_DELIVERED`.

## 2. Actors

- **Commerce Service:** Publishes delivery event.
- **Notification Service:** Notifies buyer.
- **Buyer:** Recipient.

## 3. Scope

**In Scope:**

- Create delivered notification.
- Send push by default.
- Support review/confirm receipt prompt metadata if provided.

**Out of Scope:**

- Confirm received action.
- Auto-complete order.
- Review creation.

## 4. Event Contract

Required payload:

- `shipment_id`
- `order_id`
- `buyer_id`
- `delivered_at` optional

## 5. Business Rules

- Default channels: in-app + push.
- Reference: `SHIPMENT/shipment_id` or `ORDER/order_id`.
- Notification may include safe prompt to confirm receipt/review.
- Duplicate event is idempotent.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing buyer id -> failed.
- Missing shipment/order reference -> failed.
- Push failure -> retry by policy.

## 8. Acceptance Criteria

- Buyer receives delivered notification.
- Notification can deep link to order/shipment.
- Delivery event retry does not duplicate notification.

