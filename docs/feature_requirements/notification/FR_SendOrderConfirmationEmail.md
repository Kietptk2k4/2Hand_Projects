# Functional Requirement - Send Order Confirmation Email

## 1. Feature Overview

Send order confirmation email to buyer when Commerce publishes `ORDER_CREATED`.

## 2. Actors

- **Commerce Service:** Publishes order event.
- **Notification Service:** Sends email.
- **Buyer:** Receives order confirmation.

## 3. Scope

**In Scope:**

- Render order confirmation template.
- Include safe order summary.
- Send buyer email.

**Out of Scope:**

- Order mutation.
- Seller order processing.
- Invoice generation.

## 4. Trigger

- Event type `ORDER_CREATED` or configured Commerce alias.

## 5. Business Rules

- Commerce owns order state.
- Buyer email is sent by default for order confirmation.
- Seller notification is in-app/push by default, not email unless policy changes.
- Email must not include excessive PII or internal cost data.

## 6. Database Impact

- Read/update `notification_events`.
- Optional delivery status update.

## 7. Failure Cases

- Missing buyer email -> failed.
- Missing order code/id -> failed.
- Provider timeout/rate limit -> retry.

## 8. Security

- Do not expose seller internal notes or payment provider secrets.

## 9. Acceptance Criteria

- Buyer receives order confirmation email.
- Commerce remains order source-of-truth.
- Duplicate order event does not duplicate notification intent.

