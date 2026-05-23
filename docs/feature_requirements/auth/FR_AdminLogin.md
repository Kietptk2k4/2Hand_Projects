# Functional Requirement (FR) - Admin Login

## 1. Feature Overview

Cho phep admin/moderator/support/super admin dang nhap admin portal qua Auth Service. Auth own credential validation, session refresh token va JWT claims (roles/permissions). Admin Service **khong** luu password hay refresh token.

Muc tieu:

- Cap access token + refresh token cho admin hop le.
- Tu choi user khong co role/permission admin.
- Ghi login log va tao refresh session.

## 2. Actors

- **Admin User:** Nguoi co quyen truy cap admin portal.
- **Admin Frontend:** Goi Auth API, luu token theo policy bao mat.
- **Auth Service:** Xac thuc va cap token.

## 3. Scope

- **In Scope:**
  - Nhan email + password.
  - Validate credential.
  - Kiem tra user co role admin (`ADMIN`, `MODERATOR`, `SUPER_ADMIN`, `SUPPORT`, ...) hoac permission admin portal.
  - Cap JWT access token + refresh token.
  - Ghi `LOGIN_LOGS` (success/failure).
  - Tao `refresh_token_sessions` (`ACTIVE`).
- **Out of Scope:**
  - OAuth admin login (MVP).
  - MFA (tru khi Auth bo sung sau).
  - Luu session trong Admin Service.

## 4. Preconditions

- User ton tai trong `USERS`.
- User khong `DELETED`.
- User co it nhat mot role/permission cho phep admin portal.

## 5. API Contract

**Service:** Auth Service

**Endpoint:** `POST /api/v1/auth/admin/login`

**Auth:** Public

**Headers (khuyen nghi):**

- `X-Device-Id`: dinh danh thiet bi (optional)
- `User-Agent`, IP: lay tu request

**Request body:**

```json
{
  "email": "admin@2hands.vn",
  "password": "Password123!"
}
```

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Dang nhap admin thanh cong.",
  "data": {
    "access_token": "jwt-access-token",
    "refresh_token": "jwt-refresh-token",
    "expires_in": 3600,
    "user": {
      "user_id": "uuid",
      "email": "admin@2hands.vn",
      "status": "ACTIVE"
    },
    "roles": ["ADMIN"],
    "permissions": ["USER_SUSPEND", "ADMIN_AUDIT_READ"]
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 6. Business Rules

- So sanh `password_hash`; sai → `401` `AUTH-401-INVALID-CREDENTIALS`, ghi login fail.
- `status = SUSPENDED` → `403` `AUTH-403-ACCOUNT-SUSPENDED`.
- `status = DELETED` → `401` hoac `404` theo policy (khong lo ton tai).
- Khong co role/permission admin → `403` `AUTH-403-ADMIN-PORTAL`.
- Access token chua `user_id`, `roles`, `permissions` (hoac reference de check permission).
- Refresh token luu hash trong `refresh_token_sessions`.
- Khong log password/token.

## 7. Database Impact

- **USERS:** read; co the update `last_login_at` khi thanh cong.
- **LOGIN_LOGS:** insert attempt.
- **refresh_token_sessions:** insert session `ACTIVE`.
- **USER_ROLES / PERMISSIONS:** read de build claims.

## 8. Transaction

- Ghi session + login log trong transaction hop le.
- Khong ghi outbox bat buoc cho login thanh cong (internal event optional).

## 9. Security

- Rate limit login (`AUTH-429-LOGIN`).
- Brute-force protection.
- HTTPS bat buoc.
- Admin FE: secure storage cho refresh token.

## 10. Failure Cases

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `AUTH-401-INVALID-CREDENTIALS` | Sai email/password |
| 403 | `AUTH-403-ACCOUNT-SUSPENDED` | User bi suspend |
| 403 | `AUTH-403-ADMIN-PORTAL` | Khong phai admin |
| 429 | `AUTH-429-LOGIN` | Rate limit |

## 11. FE Behavior

- Form login admin portal (email, password).
- Luu `access_token`, `refresh_token`, `expires_in`.
- Redirect dashboard sau login thanh cong.
- Hien thong bao ro khi bi tu choi admin portal.

## 12. Acceptance Criteria

- **AC1:** Admin hop le nhan duoc token + roles/permissions.
- **AC2:** User thuong khong login duoc admin portal.
- **AC3:** Suspend admin khong login duoc.
- **AC4:** Login log duoc ghi.
- **AC5:** Auth Service khong persist password plaintext.

## 13. Related

- `FR_AdminRefreshToken.md`, `FR_AdminLogout.md`, `FR_RevokeAdminSession.md`
- Admin delegate: `docs/feature_requirements/admin/FR_AdminLogin.md`
- `docs/business_flow/admin_business_flow/admin-auth-authorization-flow.md`
- Implementation: `AdminLoginController` (`POST /api/v1/auth/admin/login`)
