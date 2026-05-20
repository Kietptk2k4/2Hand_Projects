# Functional Requirement - Suspend Shop

## 1. Feature Overview

Cho phep admin suspend Commerce seller shop. Admin Service logs decision and publishes `SHOP_SUSPENDED`; Commerce Service owns shop status and marketplace effects.

## 2. Actors

- **Admin/Moderator:** Suspend shop.
- **Commerce Service:** Apply shop status `SUSPENDED`.

## 3. Scope

**In Scope:**

- Log shop suspend moderation.
- Publish `SHOP_SUSPENDED`.

**Out of Scope:**

- Cancel existing orders.
- Seller payout/refund.
- Direct Commerce DB update.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/shops/{shopId}/suspend`

**Auth:** Required, permission `SHOP_SUSPEND`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Suspended shop should block new product publish and checkout in Commerce.
- Existing orders remain supportable.
- Commerce owns final shop status.

## 6. Database Impact

- Insert `content_moderation_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required.

## 8. Security

- Permission required.

## 9. Failure Cases

- Missing permission -> 403.
- Shop not found -> 404 if synchronous validation.

## 10. Acceptance Criteria

- Shop suspend is logged.
- `SHOP_SUSPENDED` event is published.
- Commerce applies shop marketplace effects.

