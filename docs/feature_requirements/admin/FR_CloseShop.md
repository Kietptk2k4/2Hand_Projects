# Functional Requirement - Close Shop

## 1. Feature Overview

Cho phep admin close Commerce seller shop. Admin Service logs decision and publishes `SHOP_CLOSED`; Commerce Service owns final shop state and marketplace effects.

## 2. Actors

- **Admin/Moderator:** Close shop.
- **Commerce Service:** Apply shop status `CLOSED`.

## 3. Scope

**In Scope:**

- Log shop close moderation.
- Publish `SHOP_CLOSED`.

**Out of Scope:**

- Auto-cancel existing orders.
- Seller payout/refund.
- Direct Commerce DB update.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/shops/{shopId}/close`

**Auth:** Required, permission `SHOP_CLOSE`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Closed shop should block new commerce activity in Commerce.
- Existing orders remain supportable.
- Commerce owns final state.

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

- Close action is logged.
- `SHOP_CLOSED` event is published.
- Admin Service does not mutate Commerce DB.

