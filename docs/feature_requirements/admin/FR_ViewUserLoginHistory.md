# Functional Requirement - View User Login History

## 1. Feature Overview

Cho phep admin/support xem login history cua user phuc vu dieu tra suspicious login, spam account va abuse report. Du lieu login history duoc own boi Auth Service.

## 2. Actors

- **Support/Admin:** Xem login history.
- **Auth Service:** Cung cap login history.
- **Admin Service:** Authorize and proxy/read support view.

## 3. Scope

**In Scope:**

- View login attempts by user.
- Include timestamp, IP, device/user agent, success/failure.
- Pagination/filter by time.

**Out of Scope:**

- Mutate login history.
- Revoke session.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/users/{userId}/login-history`

**Auth:** Required, permission `USER_INVESTIGATION_READ`.

## 5. Business Rules

- Auth Service owns login history.
- Admin Service must not access Auth DB directly.
- No password/token/OTP returned.
- Support read can be audit logged.

## 6. Database Impact

- Admin Service: optional `admin_action_logs` for support read.
- Auth Service: read login history.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Sensitive fields redacted.

## 9. Failure Cases

- User not found -> 404.
- Auth Service unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized admin sees login history.
- No secrets are exposed.
- Admin Service respects Auth ownership.

