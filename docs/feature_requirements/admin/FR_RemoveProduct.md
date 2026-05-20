# Functional Requirement - Remove Product

## 1. Feature Overview

Cho phep admin remove product vi pham. Admin Service ghi moderation/audit log va publish `PRODUCT_REMOVED`; Commerce Service own product status update.

## 2. Actors

- **Admin/Moderator:** Remove product.
- **Commerce Service:** Apply product status `REMOVED`.
- **Admin Service:** Store decision and publish event.

## 3. Scope

**In Scope:**

- Log product moderation remove.
- Log admin action.
- Publish `PRODUCT_REMOVED`.

**Out of Scope:**

- Direct Commerce DB update.
- Refund existing orders.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/products/{productId}/remove`

**Auth:** Required, permission `PRODUCT_REMOVE`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Admin Service does not mutate Commerce DB directly.
- Commerce should hide product, block checkout and invalidate cart items.

## 6. Database Impact

- Insert `content_moderation_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required for local logs and outbox.

## 8. Security

- Permission required.
- Admin id from JWT.

## 9. Failure Cases

- Missing permission -> 403.
- Product not found according Commerce validation -> 404 if synchronous validation is used.

## 10. Acceptance Criteria

- Product remove decision is logged.
- `PRODUCT_REMOVED` event is written to outbox.
- Commerce owns final product state.

