# Functional Requirement - Cleanup Invalid Device Token

## 1. Feature Overview

Deactivate invalid or unregistered device tokens based on FCM provider response or cleanup job.

## 2. Actors

- **Notification Worker:** Cleans token.
- **FCM Provider:** Reports invalid token.

## 3. Scope

**In Scope:**

- Detect invalid/unregistered token.
- Set token inactive.
- Stop future push attempts to that token.

**Out of Scope:**

- Hard delete tokens.
- Device fraud detection.

## 4. Trigger

- Push delivery receives invalid token response.
- Scheduled cleanup detects stale invalid tokens.

## 5. Business Rules

- Invalid token is permanent token failure.
- Set `is_active = false`.
- Do not retry invalid token.
- Do not log full token.

## 6. Database Impact

Update `user_device_tokens`:

- `is_active = false`
- `updated_at = now()`

## 7. Transaction

- Single-row update, can be part of push failure handling.

## 8. Failure Cases

- Token not found -> ignore safely.
- DB update failed -> retry cleanup later.

## 9. Acceptance Criteria

- Invalid FCM token is deactivated.
- Future push queries ignore inactive token.
- Cleanup is safe to rerun.

