# Functional Requirement - View Current User Enforcement

## 1. Feature Overview

Cho phep admin xem enforcement hien tai dang `ACTIVE` cua user de biet user co dang bi suspend, ban hoac restrict hay khong.

## 2. Actors

- **Admin/Support/Moderator:** Xem current enforcement.
- **System:** Query active enforcement state.

## 3. Scope

**In Scope:**

- Return active enforcement list for user.
- Include action type, reason, expires_at and enforced_by.

**Out of Scope:**

- Enforcement history detail.
- Create/revoke enforcement.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/users/{userId}/enforcements/current`

**Auth:** Required, permission `USER_ENFORCEMENT_READ`.

## 5. Business Rules

- Only `status = ACTIVE` records are returned.
- Expired records should be handled by expiration job; if stale, response may flag them.
- Multiple active enforcements can exist if policy allows different action types.

## 6. Database Impact

- Read `user_enforcements`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT admin required.
- Permission required.

## 9. Failure Cases

- User not found -> 404.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Active enforcements are returned accurately.
- Revoked/expired enforcements are excluded.
- Unauthorized access is denied.

