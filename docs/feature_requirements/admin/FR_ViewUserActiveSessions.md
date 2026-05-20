# Functional Requirement - View User Active Sessions

## 1. Feature Overview

Cho phep admin/support xem active sessions cua user de dieu tra account compromise hoac suspicious device activity. Session source-of-truth thuoc Auth Service.

## 2. Actors

- **Support/Admin:** Xem active sessions.
- **Auth Service:** Own session data.
- **Admin Service:** Authorize request.

## 3. Scope

**In Scope:**

- List active sessions/devices.
- Include session metadata safe for support.

**Out of Scope:**

- Revoke session; xem `FR_RevokeAdminSession.md` cho admin session pattern.
- Token display.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/users/{userId}/sessions`

**Auth:** Required, permission `USER_INVESTIGATION_READ`.

## 5. Business Rules

- Auth owns session state.
- Admin Service must not expose refresh/access token values.
- Support read can be audit logged.

## 6. Database Impact

- Admin Service: optional audit log.
- Auth Service: read sessions.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Token values never returned/logged.

## 9. Failure Cases

- User not found -> 404.
- Auth unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized admin can view active session metadata.
- Token secrets are not exposed.
- Unauthorized admin is denied.

