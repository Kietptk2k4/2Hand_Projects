# Functional Requirement - Send Push Notification

## 1. Feature Overview

Send push notification to recipient's active device tokens through FCM.

## 2. Actors

- **Notification Worker:** Sends push.
- **FCM Provider:** Delivers push.
- **User Device:** Receives push.

## 3. Scope

**In Scope:**

- Check push policy/settings.
- Load active tokens.
- Send FCM payload.
- Handle provider response.

**Out of Scope:**

- APNS direct integration in MVP.
- Marketing campaign push.

## 4. Business Rules

- Send only if event policy allows push and `allow_push = true`.
- No active token is a skip, not a hard failure.
- Push payload must be short and safe.
- Invalid token must be deactivated.
- Retryable provider errors should be marked for retry.

## 5. Database Impact

- Read `user_device_tokens`.
- Read `user_notification_settings`.
- Update token on invalid response.
- Update notification delivery status where applicable.

## 6. Transaction

- Do not hold long DB transaction while calling FCM.

## 7. Security

- Do not log full device token.
- Do not put secrets/OTP/password reset token in push payload.

## 8. Failure Cases

- FCM timeout -> retryable.
- FCM rate limit -> retry with backoff.
- Invalid token -> deactivate.
- Payload invalid -> failed/permanent.

## 9. Acceptance Criteria

- Push is sent to active tokens only.
- User settings are respected.
- No-token recipients do not fail whole event.
- Provider failures are classified.

