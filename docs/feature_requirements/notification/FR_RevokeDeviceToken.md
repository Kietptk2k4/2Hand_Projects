# Functional Requirement - Revoke Device Token

## 1. Feature Overview

Deactivate a user's device token, normally when user logs out from a device.

## 2. Actors

- **User Client:** Requests revoke.
- **Notification API:** Deactivates token.

## 3. Scope

**In Scope:**

- Validate token ownership.
- Set `is_active = false`.

**Out of Scope:**

- Hard delete token.
- Revoke Auth session.

## 4. API Contract

**Endpoint:** `DELETE /notification/api/v1/device-tokens/{deviceToken}`

**Auth:** Required user JWT.

## 5. Business Rules

- User can revoke only own token.
- Revoke is soft deactivate.
- Inactive token is ignored by push delivery.
- Operation is idempotent.

## 6. Database Impact

Update `user_device_tokens`:

- `is_active = false`
- `updated_at = now()`

## 7. Transaction

- Single-row update.

## 8. Failure Cases

- Token not found -> 404 or success by idempotent policy.
- Other user's token -> 404/403; prefer 404.
- Unauthorized -> 401.

## 9. Acceptance Criteria

- Revoked token no longer receives push.
- Token row remains for audit/debug.
- User cannot revoke another user's token.

