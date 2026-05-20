# Functional Requirement - Register Device Token

## 1. Feature Overview

Register or reactivate user's push device token for FCM push delivery.

## 2. Actors

- **User Client:** Sends token after login.
- **Notification API:** Stores token.

## 3. Scope

**In Scope:**

- Validate `device_type` and `device_token`.
- Upsert by `device_token`.
- Mark token active.

**Out of Scope:**

- APNS-specific behavior.
- Device fingerprinting.

## 4. API Contract

**Endpoint:** `POST /notification/api/v1/device-tokens`

**Auth:** Required user JWT.

**Request body:**

- `device_type`: `IOS`, `ANDROID`, `WEB`
- `device_token`

## 5. Business Rules

- `user_id` comes from JWT.
- `device_token` is unique.
- Register is upsert by token.
- Set `is_active = true`, update `last_used_at` and `updated_at`.
- Do not log full token.

## 6. Database Impact

- Insert/update `user_device_tokens`.

## 7. Transaction

- Single upsert transaction.

## 8. Failure Cases

- Invalid device type -> 400.
- Missing token -> 400.
- Unauthorized -> 401.
- Token conflict -> resolve via upsert/security policy.

## 9. Acceptance Criteria

- Token is stored active for current user.
- Re-registering same token is idempotent.
- Token can be used by push delivery.

