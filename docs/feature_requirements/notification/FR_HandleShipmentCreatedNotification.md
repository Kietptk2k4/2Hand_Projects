# Functional Requirement - Handle Shipment Created Notification

## 1. Feature Overview

Notify buyer and optionally seller when Commerce publishes `SHIPMENT_CREATED`.

## 2. Actors

- **Commerce Service:** Publishes shipment event.
- **Notification Service:** Creates notification.
- **Buyer/Seller:** Recipients.

## 3. Scope

**In Scope:**

- Create shipment created in-app notification.
- Reference shipment/order.

**Out of Scope:**

- Creating shipment.
- Tracking carrier status.
- Email by default.

## 4. Event Contract

Required payload:

- `shipment_id`
- `order_id`
- `buyer_id`
- `seller_id` optional
- `tracking_code` optional

## 5. Business Rules

- Default channel: in-app.
- Push optional by policy; MVP can keep shipment created in-app only.
- Reference: `SHIPMENT/shipment_id`.
- Tracking code can be included only if safe.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing buyer id -> failed.
- Missing shipment id -> failed.
- Duplicate event -> idempotent.

## 8. Acceptance Criteria

- Buyer receives shipment created notification.
- Notification references shipment/order.
- Commerce shipment is not mutated.

