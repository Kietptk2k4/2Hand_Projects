# Functional Requirement - Revoke User Enforcement

## 1. Feature Overview

Cho phep admin revoke mot enforcement dang `ACTIVE` cua user. Revoke enforcement se publish event de Auth/Social/Commerce cap nhat restriction cache/state.

## 2. Actors

- **Admin/Moderator:** Revoke enforcement.
- **System:** Update enforcement status, write logs and publish event.
- **Consumer Services:** Apply revoke effect.

## 3. Scope

**In Scope:**

- Revoke `ACTIVE` enforcement.
- Write `user_enforcement_logs`.
- Write `admin_action_logs`.
- Publish `USER_ENFORCEMENT_REVOKED`.

**Out of Scope:**

- Delete enforcement record.
- Edit old enforcement reason.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/user-enforcements/{enforcementId}/revoke`

**Auth:** Required, permission `USER_ENFORCEMENT_REVOKE`.

**Request body:**

- `note` optional
- `reason` optional

## 5. Business Rules

- Only `ACTIVE` enforcement can be revoked.
- `REVOKED` and `EXPIRED` enforcement are terminal.
- Revoke must write enforcement log.
- Revoke must publish outbox event.

## 6. Database Impact

- Update `user_enforcements.status = REVOKED`.
- Insert `user_enforcement_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT admin required.
- Permission required.
- Admin id from JWT.

## 9. Failure Cases

- Enforcement not found -> 404.
- Enforcement not active -> 409 or idempotent no-op by API policy.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Active enforcement can be revoked by authorized admin.
- Logs and outbox event are created.
- Consumer services can remove restriction effects.

