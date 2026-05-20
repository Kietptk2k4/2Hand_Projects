# Functional Requirement - Handle Order Created Notification

## 1. Feature Overview

Notify buyer and sellers when Commerce publishes `ORDER_CREATED`.

## 2. Actors

- **Commerce Service:** Publishes order event.
- **Notification Service:** Creates notifications.
- **Buyer/Seller:** Recipients.

## 3. Scope

**In Scope:**

- Notify buyer order placed.
- Notify sellers new order if seller ids provided.
- Send buyer email confirmation by policy.

**Out of Scope:**

- Order creation.
- Seller fulfillment.
- Commerce DB mutation.

## 4. Event Contract

Required payload:

- `order_id`
- `order_code` recommended
- `buyer_id`
- `seller_ids` when seller notification required
- `total_amount` optional safe summary

## 5. Business Rules

- Buyer default channels: in-app + push + email.
- Seller default channels: in-app + push.
- One event can create multiple notifications.
- Reference: `ORDER/order_id`.
- Duplicate event must not duplicate notifications.

## 6. Database Impact

- Insert `user_notifications` per recipient if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing buyer id -> failed.
- Missing seller ids -> seller notification skipped or failed by policy.
- Email failure -> retry email/delivery.

## 8. Acceptance Criteria

- Buyer receives order created notification.
- Sellers receive new order notification when payload provides seller ids.
- Commerce state is not modified.

