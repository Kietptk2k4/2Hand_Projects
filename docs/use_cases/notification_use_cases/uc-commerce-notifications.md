# UC - Commerce Notifications

## 1. Overview

Use case nay mo ta notification duoc tao tu Commerce events lien quan order, payment, shipment va optional review reminder.

## 2. Actors

- **Commerce Service:** Publish commerce events.
- **Notification Service:** Consume va deliver.
- **Buyer:** Nhan order/payment/shipment notification.
- **Seller:** Nhan new order notification neu applicable.

## 3. Related Data

- `notification_events`
- `user_notifications`
- `user_notification_settings`
- `user_device_tokens`

## 4. Preconditions

- Commerce event co buyer/seller recipient ids.
- Event payload co order/payment/shipment reference.
- Notification routing co alias neu Commerce dung `COMMERCE_*` prefixed event.

## 5. Business Rules

- Commerce Service owns order/payment/shipment state.
- Notification Service khong mutate Commerce DB.
- One commerce event can notify multiple recipients.
- Payment success duplicate khong duoc notify hai lan.
- Payment/order emails chi gui theo critical policy.

## 6. Sub-Use Cases

### UC-COMMERCE-01: Notify Order Created

Main flow:

1. Commerce publishes `ORDER_CREATED`.
2. Notification creates buyer notification reference `ORDER/order_id`.
3. Notification creates seller notifications for each seller id if present.
4. Buyer can receive in-app, push, email.
5. Seller receives in-app/push by default.

### UC-COMMERCE-02: Notify Payment Success

Main flow:

1. Commerce publishes `PAYMENT_SUCCESS`.
2. Notification resolves buyer.
3. Notification creates in-app notification.
4. Notification sends push and email if allowed.
5. Duplicate webhook-derived event is deduped by idempotency key.

### UC-COMMERCE-03: Notify Payment Failed

Main flow:

1. Commerce publishes `PAYMENT_FAILED`.
2. Notification resolves buyer.
3. Notification creates in-app/push notification.
4. Email is not sent by default in MVP.

### UC-COMMERCE-04: Notify Shipment Updates

Main flow:

1. Commerce publishes `SHIPMENT_CREATED`, `SHIPMENT_SHIPPED` or `SHIPMENT_DELIVERED`.
2. Notification resolves buyer.
3. Notification creates notification reference `SHIPMENT/shipment_id`.
4. Shipped/delivered can send push.

### UC-COMMERCE-05: Notify Order Completed

Main flow:

1. Commerce publishes `ORDER_COMPLETED`.
2. Notification notifies buyer.
3. Optional review prompt is handled by separate reminder event/job.

## 7. Failure Cases

- Missing buyer id.
- Missing seller ids for seller notification.
- Unsupported prefixed event without alias mapping.
- Email provider failure.
- Duplicate event.

## 8. Security

- Do not expose payment provider raw payload or secret.
- Seller notification must not leak buyer PII beyond allowed context.
- Commerce owner service authorizes deep link resource access.

## 9. Acceptance Criteria

- Buyer receives order/payment/shipment notifications.
- Seller receives new order notification when payload includes seller ids.
- Duplicate payment success does not create duplicate notification.
- Notification Service does not mutate Commerce data.
- Channel policy matches event criticality.

