# Functional Requirement - Reopen Shop

## 1. Feature Overview

Cho phep admin reopen shop da bi suspend/close neu policy cho phep. Commerce Service owns final status and product readiness.

## 2. Actors

- **Admin/Moderator:** Reopen shop.
- **Commerce Service:** Apply shop restore/reopen.

## 3. Scope

**In Scope:**

- Log shop reopen action.
- Publish `SHOP_RESTORED`.

**Out of Scope:**

- Auto republish removed/archived products.
- Undo historical moderation logs.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/shops/{shopId}/reopen`

**Auth:** Required, permission `SHOP_SUSPEND` or `SHOP_RESTORE`.

**Request body:**

- `reason`
- `note` optional

## 5. Business Rules

- Reason required.
- Reopen does not automatically republish products.
- Commerce validates final shop transition.
- Existing order snapshots remain unchanged.

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
- Commerce rejects transition -> 409.

## 10. Acceptance Criteria

- Shop reopen action is logged.
- `SHOP_RESTORED` event is published.
- Commerce owns final shop state.

