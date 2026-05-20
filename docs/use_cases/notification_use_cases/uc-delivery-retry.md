# UC - Delivery Retry

## 1. Overview

Use case nay mo ta retry cho cac delivery failures cua push/email trong MVP khi `delivery_status = FAILED` hoac provider response cho thay loi transient.

## 2. Actors

- **Delivery Retry Worker:** Retry failed delivery.
- **FCM Provider:** Push provider.
- **Email Provider:** Email provider.
- **Notification DB:** Stores notification/token state.

## 3. Related Data

- `user_notifications`
- `user_device_tokens`
- `user_notification_settings`
- `notification_events`

## 4. Preconditions

- Delivery failure da duoc ghi nhan.
- Failure con retry budget.
- Event/notification co du metadata de retry.

## 5. Business Rules

- Retryable failures: timeout, rate limit, provider 5xx.
- Permanent failures: invalid token, invalid email, missing template, malformed payload.
- Latest user settings should be respected on retry unless event critical override applies.
- Invalid token must be deactivated.
- Retry must not create duplicate `user_notifications`.

## 6. Sub-Use Cases

### UC-RETRY-01: Retry Failed Push

Main flow:

1. Worker selects failed push delivery candidates.
2. Worker reloads active tokens and settings.
3. Worker sends push again.
4. On success, mark delivery success.
5. On retryable failure, keep failed and increment retry metadata.

### UC-RETRY-02: Retry Failed Email

Main flow:

1. Worker selects failed email delivery candidates.
2. Worker validates template and recipient.
3. Worker sends email again.
4. Worker updates status based on provider response.

### UC-RETRY-03: Stop Retrying Permanent Failure

Main flow:

1. Worker classifies failure as permanent.
2. Worker marks item failed/permanent according to policy.
3. Worker stops retry loop for that target.

## 7. Failure Cases

- Max retry exceeded.
- Token invalid.
- Email invalid.
- Provider still unavailable.
- User deleted notification before retry.

## 8. Security

- Do not log device token, email token, OTP, reset link.
- Provider errors stored in sanitized form.
- Retry worker internal-only.

## 9. Acceptance Criteria

- Retryable failures are retried with backoff.
- Permanent failures do not retry forever.
- Invalid tokens are deactivated.
- Latest settings are respected.
- Retry does not duplicate in-app records.

