# Functional Requirement - Admin Logout

## 1. Feature Overview

Cho phep admin dang xuat khoi admin portal bang cach revoke/clear session thong qua Auth Service.

## 2. Actors

- **Admin:** Dang xuat.
- **Auth Service:** Revoke refresh session/token.

## 3. Scope

**In Scope:**

- Logout current admin session.
- Delegate token/session invalidation to Auth Service.

**Out of Scope:**

- Logout all sessions.
- Admin Service session persistence.

## 4. API Contract

**Endpoint:** Auth Service endpoint, e.g. `POST /auth/api/v1/admin/logout`

**Auth:** Required.

## 5. Business Rules

- Admin can logout own current session.
- Auth Service invalidates refresh token/session.
- Access token may remain valid until expiry unless Auth has token blacklist policy.

## 6. Database Impact

- Admin Service: none.
- Auth Service updates session/token state.

## 7. Transaction

- Managed by Auth Service.

## 8. Security

- Do not log tokens.
- Logout request must be authenticated.

## 9. Failure Cases

- Unauthenticated -> 401.
- Session already revoked -> idempotent success.

## 10. Acceptance Criteria

- Admin can logout current session.
- Refresh token cannot be reused after logout.
- Admin Service does not persist session state.

