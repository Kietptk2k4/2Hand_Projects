# UC - Device Token Management

## 1. Overview

Use case nay mo ta cach app/client dang ky, cap nhat va revoke device token de Notification Service co the gui push qua FCM.

## 2. Actors

- **User Client:** Mobile/web app.
- **Notification API:** Protected endpoints.
- **Notification Worker:** Deactivate invalid token tu provider response.
- **FCM Provider:** Tra invalid/unregistered token.

## 3. Related Data

- `user_device_tokens`

## 4. Preconditions

- User da authenticated bang JWT.
- Client co `device_type` va `device_token`.

## 5. Business Rules

- `device_token` unique.
- Register token la upsert.
- `user_id` lay tu JWT, khong lay tu request body.
- Logout/revoke set `is_active = false`.
- Invalid provider token set `is_active = false`.
- Khong log full device token.

## 6. Sub-Use Cases

### UC-TOKEN-01: Register Device Token

Main flow:

1. Client calls `POST /device-tokens`.
2. API authenticates JWT.
3. API validates `device_type` and `device_token`.
4. API upserts by `device_token`.
5. API sets `user_id`, `device_type`, `is_active = true`, `last_used_at`, `updated_at`.

Postconditions:

- Token active va available cho push.

### UC-TOKEN-02: Revoke Device Token

Main flow:

1. Client calls `DELETE /device-tokens/{deviceToken}`.
2. API authenticates JWT.
3. API validates token ownership.
4. API sets `is_active = false`.

Postconditions:

- Token khong nhan push nua.

### UC-TOKEN-03: Deactivate Invalid Token

Main flow:

1. Push delivery receives invalid/unregistered token from FCM.
2. Worker finds token row.
3. Worker sets `is_active = false`.

Postconditions:

- Future push ignores invalid token.

## 7. Failure Cases

- Invalid JWT.
- Invalid `device_type`.
- Empty/malformed token.
- Token belongs to another user.
- DB unique conflict during concurrent upsert.

## 8. Security

- Token is sensitive.
- User can revoke only own token.
- Rate limit register endpoint.
- Use token hash/partial token only for logs.

## 9. Acceptance Criteria

- Login/register activates token.
- Logout/revoke deactivates token.
- Invalid FCM token is cleaned up.
- Push sends only to active tokens.
- Token cannot be managed by another user.

