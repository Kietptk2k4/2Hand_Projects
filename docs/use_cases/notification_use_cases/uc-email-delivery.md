# UC - Email Delivery

## 1. Overview

Use case nay mo ta viec gui email notification cho cac event critical/system trong MVP nhu verify email, reset password, payment success, order confirmation va account enforcement.

## 2. Actors

- **Notification Worker:** Dieu phoi email delivery.
- **Email Provider:** SMTP/SendGrid/Mailgun hoac tuong duong.
- **Auth/Commerce/Admin Service:** Publish event co du du lieu email.
- **User:** Nhan email.

## 3. Related Data

- `notification_events`
- `user_notification_settings`
- `user_notifications` optional neu event cung tao in-app.

## 4. Preconditions

- Event type co email channel policy.
- Payload co recipient email hoac cach resolve email hop le.
- Email template ton tai.

## 5. Business Rules

- Notification Service chi deliver verification/reset link/token do Auth Service tao.
- Khong tao password reset token trong Notification Service.
- Email chi dung cho critical/system events trong MVP.
- Marketing/campaign email out of scope.
- Token/OTP/provider credential khong duoc log.
- Critical security email co the override user disabled email neu policy quy dinh.

## 6. Sub-Use Cases

### UC-EMAIL-01: Send Critical Email

Main flow:

1. Worker nhan event can email.
2. Worker validate payload va template.
3. Worker apply email settings/default/critical override.
4. Worker render email body tu template trusted.
5. Worker call email provider.
6. Worker update delivery state.

Postconditions:

- Email duoc provider accept hoac delivery failure duoc ghi nhan.

### UC-EMAIL-02: Send Auth Email

Main flow:

1. Auth publishes `EMAIL_VERIFICATION_REQUESTED` hoac `PASSWORD_RESET_REQUESTED`.
2. Notification validates link/token da co trong payload.
3. Notification sends email.

Postconditions:

- User nhan email verify/reset.
- Auth van la service validate token khi user click link.

### UC-EMAIL-03: Handle Email Failure

Main flow:

1. Provider tra loi loi.
2. Worker classify retryable/permanent.
3. Retryable: mark failed for retry.
4. Permanent: mark failed with sanitized reason.

## 7. Failure Cases

- Missing recipient email.
- Missing reset/verify link.
- Template not found.
- Provider timeout/rate limit.
- Invalid email address.

## 8. Security

- Never log token, OTP, provider secret.
- Email content phai sanitized.
- Raw event payload khong duoc expose cho user.

## 9. Acceptance Criteria

- Critical emails are delivered when valid.
- Auth token generation remains in Auth Service.
- Marketing email is not implemented.
- Retryable/permanent email failures are distinguished.
- Sensitive data is not logged.

