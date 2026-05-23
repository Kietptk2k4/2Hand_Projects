# Functional Requirement - Admin Login

## 1. Feature Overview

Cho phep admin dang nhap vao admin portal thong qua Auth Service. Admin Service khong own credential/session, chi tieu thu token do Auth Service cap.

## 2. Actors

- **Admin/Moderator/Support/Super Admin:** Dang nhap admin portal.
- **Auth Service:** Xac thuc credential va cap token.
- **Admin Frontend:** Goi Auth API va luu token theo policy.

## 3. Scope

**In Scope:**

- Admin login delegation to Auth Service.
- Ensure user has admin role/permission.
- Return access token/refresh token from Auth Service.

**Out of Scope:**

- Password storage in Admin Service.
- OAuth implementation.
- MFA implementation unless Auth Service supports.

## 4. API Contract

**Source-of-truth spec:** `docs/feature_requirements/auth/FR_AdminLogin.md`

**Endpoint:** Auth Service `POST /api/v1/auth/admin/login` (local: `http://localhost:{auth-port}/api/v1/auth/admin/login`)

**Admin Service Endpoint:** None required unless acting as gateway.

**Request body:**

- `email` or username
- `password`

## 5. Business Rules

- User must be authenticated by Auth Service.
- User must have admin role or admin permission.
- Non-admin user cannot login to admin portal.
- Admin Service must not store password or refresh token.

## 6. Database Impact

- Admin Service: none.
- Auth Service owns login history/session writes.

## 7. Transaction

- Managed by Auth Service.

## 8. Security

- Do not log password/token.
- Rate limit/brute force protection belongs to Auth Service.
- Admin frontend must protect tokens according frontend security policy.

## 9. Failure Cases

- Invalid credentials -> 401.
- Valid user but no admin role -> 403.
- Suspended admin -> 403.

## 10. Acceptance Criteria

- Admin can login using Auth Service.
- Non-admin user is denied.
- Admin Service does not persist credentials.

