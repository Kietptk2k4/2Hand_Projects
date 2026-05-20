# Functional Requirement - Handle Shipment Shipped Notification

## 1. Feature Overview

Notify buyer when Commerce publishes `SHIPMENT_SHIPPED`.

## 2. Actors

- **Commerce Service:** Publishes shipment event.
- **Notification Service:** Notifies buyer.
- **Buyer:** Recipient.

## 3. Scope

**In Scope:**

- Create shipment shipped notification.
- Send push by default.
- Include tracking reference if safe.

**Out of Scope:**

- Carrier polling.
- Shipment state mutation.

## 4. Event Contract

Required payload:

- `shipment_id`
- `order_id`
- `buyer_id`
- `tracking_code` optional

## 5. Business Rules

- Default channels: in-app + push.
- Reference: `SHIPMENT/shipment_id`.
- Tracking code must not expose private carrier credentials.
- Duplicate event is idempotent.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing buyer id -> failed.
- Missing shipment id -> failed.
- Push failure -> retry push delivery.

## 8. Acceptance Criteria

- Buyer receives shipped notification.
- Push is sent when allowed and token exists.
- Commerce shipment data is not mutated.

