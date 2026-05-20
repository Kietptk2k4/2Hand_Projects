# Functional Requirement - Retry Failed Push Notification

## 1. Feature Overview

Retry push notifications that failed due to transient provider/network errors.

## 2. Actors

- **Delivery Retry Worker:** Retries push.
- **FCM Provider:** Receives retry.

## 3. Scope

**In Scope:**

- Select retryable push failures.
- Respect max retry/backoff.
- Reload latest token/settings.
- Retry FCM send.

**Out of Scope:**

- Retrying invalid tokens.
- Per-device delivery history table in MVP.

## 4. Business Rules

- Retry only transient failures: timeout, rate limit, provider 5xx.
- Do not retry invalid/unregistered tokens.
- Latest `allow_push` should be respected unless event is critical override.
- Retry must not create duplicate `user_notifications`.
- Max retry policy must be enforced.

## 5. Database Impact

- Read `user_notifications`.
- Read/update `user_device_tokens`.
- Update delivery status/retry metadata according to MVP implementation.

## 6. Transaction

- Select retry candidates in small batches.
- Avoid long DB transaction around FCM call.

## 7. Failure Cases

- Provider still unavailable -> keep failed and increment retry metadata.
- Token becomes inactive -> skip.
- Max retries exceeded -> permanent failed by policy.

## 8. Security

- Full device token is never logged.
- Push payload must remain sanitized on retry.

## 9. Acceptance Criteria

- Retryable push failures are retried.
- Invalid tokens are not retried.
- Latest settings are respected.
- Retry does not duplicate in-app notification.

