# Functional Requirement - Refresh Admin Token

## 1. Feature Overview

Cho phep admin refresh access token thong qua Auth Service. Admin Service khong own refresh token lifecycle.

## 2. Actors

- **Admin:** Can access token moi.
- **Auth Service:** Validate refresh token and issue new access token.

## 3. Scope

**In Scope:**

- Refresh admin access token.
- Preserve admin role/permission claims according Auth policy.

**Out of Scope:**

- Admin Service session storage.
- Manual session revoke.

## 4. API Contract

**Endpoint:** Auth Service endpoint, e.g. `POST /auth/api/v1/admin/token/refresh`

**Request body:**

- `refresh_token` or cookie-based refresh token.

## 5. Business Rules

- Refresh token must be valid and not revoked.
- Admin account must still be active and authorized.
- New access token must reflect current roles/permissions.

## 6. Database Impact

- Admin Service: none.
- Auth Service owns refresh token/session data.

## 7. Transaction

- Managed by Auth Service.

## 8. Security

- Do not log refresh token.
- Revoked/suspended admin cannot refresh token.

## 9. Failure Cases

- Invalid/expired refresh token -> 401.
- Revoked session -> 401.
- Admin no longer authorized -> 403.

## 10. Acceptance Criteria

- Valid admin refresh token returns new access token.
- Revoked or unauthorized admin cannot refresh.
- Admin Service does not store refresh tokens.

