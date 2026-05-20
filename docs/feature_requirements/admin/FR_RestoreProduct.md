# Functional Requirement - Restore Product

## 1. Feature Overview

Cho phep admin restore product da bi moderation remove/hide theo policy. Commerce Service validate product readiness va own final product status.

## 2. Actors

- **Admin/Moderator:** Restore product.
- **Commerce Service:** Apply restore if valid.
- **Admin Service:** Log and publish event.

## 3. Scope

**In Scope:**

- Log product restore moderation.
- Publish `PRODUCT_RESTORED`.

**Out of Scope:**

- Force product active bypassing Commerce readiness.
- Seller product edit.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/products/{productId}/restore`

**Auth:** Required, permission `PRODUCT_REMOVE` or `PRODUCT_RESTORE`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Restore must not bypass Commerce validation.
- Commerce decides final status: `ACTIVE`, `OUT_OF_STOCK`, `PAUSED`, etc. according domain rules.

## 6. Database Impact

- Insert `content_moderation_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required for local logs/outbox.

## 8. Security

- Permission required.

## 9. Failure Cases

- Missing permission -> 403.
- Product not found -> 404 if synchronous validation.
- Commerce rejects restore -> 409.

## 10. Acceptance Criteria

- Restore action is logged.
- Restore event is published.
- Commerce remains owner of final product state.

