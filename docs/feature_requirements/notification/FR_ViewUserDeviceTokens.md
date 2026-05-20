# Functional Requirement - View User Device Tokens

## 1. Feature Overview

Allow authenticated user to view own registered device tokens/devices in masked form.

## 2. Actors

- **User:** Views own devices.
- **Notification API:** Returns token metadata.

## 3. Scope

**In Scope:**

- List current user's tokens.
- Show device type, active state and timestamps.
- Mask token value.

**Out of Scope:**

- Admin device investigation.
- Exposing raw token.

## 4. API Contract

**Endpoint:** `GET /notification/api/v1/device-tokens`

**Auth:** Required user JWT.

## 5. Business Rules

- Return only `user_id = current_user_id`.
- Do not return full `device_token`.
- Include `is_active`, `device_type`, `last_used_at`, `created_at`.
- Sort newest/active first.

## 6. Database Impact

- Read `user_device_tokens`.

## 7. Security

- Device token is sensitive.
- Mask token or return token id only.

## 8. Failure Cases

- Unauthorized -> 401.
- DB unavailable -> 503.

## 9. Acceptance Criteria

- User sees own device tokens/devices.
- Raw token is not exposed.
- Other users' tokens are never returned.

