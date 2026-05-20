# Functional Requirement - Send Payment Success Email

## 1. Feature Overview

Send payment success confirmation email to buyer when Commerce publishes `PAYMENT_SUCCESS`.

## 2. Actors

- **Commerce Service:** Publishes payment event.
- **Notification Service:** Sends email.
- **Buyer:** Receives confirmation.

## 3. Scope

**In Scope:**

- Render payment success email.
- Include safe order/payment summary.
- Send email if allowed by policy/settings.

**Out of Scope:**

- Payment state update.
- Refund or support handling.
- Exposing provider raw payload.

## 4. Trigger

- Event type `PAYMENT_SUCCESS` or configured Commerce alias.

## 5. Business Rules

- Commerce owns payment truth.
- Duplicate payment event must not send duplicate user-facing notification.
- Email content may include order code, amount and payment method, but not provider secrets/raw webhook payload.
- Default policy enables email for payment success.

## 6. Database Impact

- Read `notification_events`.
- Read `user_notification_settings`.
- Optional update delivery status.

## 7. Failure Cases

- Missing buyer email/recipient -> failed.
- Missing payment/order reference -> failed.
- Provider transient failure -> retry.

## 8. Security

- Do not include card/provider secret.
- Amount/order info must come from trusted Commerce payload.

## 9. Acceptance Criteria

- Buyer receives payment success email.
- Duplicate payment event does not duplicate email intent.
- Commerce DB is not mutated.

