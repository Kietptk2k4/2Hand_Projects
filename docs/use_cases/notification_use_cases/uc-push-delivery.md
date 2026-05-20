# UC - Push Delivery

## 1. Overview

Use case nay mo ta viec gui push notification qua FCM cho user co active device token va cho phep push theo notification settings.

## 2. Actors

- **Notification Worker:** Dieu phoi push delivery.
- **FCM Provider:** Nhan push request.
- **User Device:** Nhan push.
- **User:** Cau hinh push preference va register token.

## 3. Related Data

- `user_device_tokens`
- `user_notification_settings`
- `user_notifications`
- `notification_events`

## 4. Preconditions

- Event/notification da duoc resolve recipient.
- Event policy cho phep push.
- User setting khong tat push.

## 5. Business Rules

- Push chi gui den `is_active = true` tokens.
- Khong co active token khong phai loi system.
- Invalid/unregistered FCM token phai deactivate.
- Timeout/rate limit/provider 5xx la retryable.
- Device token la sensitive, khong log full value.

## 6. Sub-Use Cases

### UC-PUSH-01: Send Push Notification

Main flow:

1. Worker xac dinh event policy co push.
2. Worker load setting cua recipient.
3. Worker load active device tokens.
4. Worker build push payload tu title/content/reference safe.
5. Worker call FCM cho tung token.
6. Worker update delivery status/metrics.

Postconditions:

- Push duoc gui den active devices.

### UC-PUSH-02: Skip Push

Main flow:

1. Worker kiem tra setting/token.
2. Push disabled hoac khong co active token.
3. Worker skip push va khong mark whole event failed.

Postconditions:

- Event co the completed neu channel required khac da xong.

### UC-PUSH-03: Cleanup Invalid Token From Provider Response

Main flow:

1. FCM tra invalid/unregistered token.
2. Worker set `user_device_tokens.is_active = false`.
3. Worker khong retry token do.

Postconditions:

- Token invalid khong duoc dung cho push sau nay.

## 7. Failure Cases

- FCM unavailable.
- FCM rate limit.
- Invalid token.
- Malformed push payload.
- DB update delivery status failed.

## 8. Security

- Push payload khong chua secret/OTP/password reset token neu khong can.
- Full `device_token` khong duoc ghi log.
- Reference link chi la deep link; owner service van authorize.

## 9. Acceptance Criteria

- Push respects user setting.
- Push sent only to active tokens.
- Invalid tokens are deactivated.
- No active token does not fail event.
- Retryable provider errors can be retried later.

