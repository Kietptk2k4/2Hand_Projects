# Functional Requirement (FR) - Admin Refresh Token

## 1. Feature Overview

Cho phep admin client cap lai access token bang refresh token ma khong can nhap lai password. Chi ap dung cho session admin portal da tao tu `FR_AdminLogin`.

## 2. Actors

- **Admin Frontend / Client:** Goi refresh khi access token het han.
- **Auth Service:** Validate refresh session va cap access token moi.

## 3. Scope

- **In Scope:**
  - Nhan refresh token.
  - Validate session `ACTIVE` trong `refresh_token_sessions`.
  - Kiem tra user van hop le (`ACTIVE`, khong `DELETED`/`SUSPENDED`).
  - Kiem tra van co quyen admin portal.
  - Cap access token moi (+ optional rotate refresh token theo policy).
- **Out of Scope:**
  - Refresh token cua user app thuong (`FR_RefreshAccessToken`).
  - Admin login/logout.

## 4. Preconditions

- Refresh token hop le, chua het han, status `ACTIVE`.
- User van co role/permission admin.

## 5. API Contract

**Endpoint:** `POST /api/v1/auth/admin/token/refresh`

**Auth:** Public (xac thuc bang refresh token trong body)

**Request body:**

```json
{
  "refresh_token": "refresh-jwt-or-raw-token"
}
```

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap lai access token admin thanh cong.",
  "data": {
    "access_token": "new-access-token",
    "expires_in": 3600,
    "user": {
      "user_id": "uuid",
      "email": "admin@2hands.vn",
      "status": "ACTIVE"
    },
    "roles": ["ADMIN"],
    "permissions": ["USER_SUSPEND"]
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 6. Business Rules

- Refresh token khong hop le / het han / `REVOKED` / `LOGGED_OUT` → `401` `AUTH-401-INVALID-REFRESH-SESSION`.
- User `SUSPENDED` → `403` `AUTH-403-ACCOUNT-SUSPENDED`.
- Mat quyen admin (role bi thu hoi) → `403` `AUTH-403-ADMIN-PORTAL`.
- Rate limit refresh (`AUTH-429-REFRESH`).
- Optional: phat hien reuse refresh token bat thuong → revoke all sessions user (policy bao mat).
- Khong tra refresh token plaintext trong log.

## 7. Database Impact

- Read `refresh_token_sessions`, `USERS`, roles/permissions.
- Co the update `updated_at` session; rotate token hash neu policy bat.

## 8. Transaction

- Read-heavy; write neu rotate/revoke.

## 9. Security

- Chi chap nhan refresh token tu admin login flow (cung issuer/audience policy).
- HTTPS bat buoc.

## 10. Failure Cases

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `AUTH-401-INVALID-REFRESH-SESSION` | Token/session khong hop le |
| 403 | `AUTH-403-ACCOUNT-SUSPENDED` | User bi suspend |
| 403 | `AUTH-403-ADMIN-PORTAL` | Khong con quyen admin |
| 429 | `AUTH-429-REFRESH` | Rate limit |

## 11. FE Behavior

- Interceptor HTTP: 401 access token → goi refresh → retry request.
- Neu refresh fail → xoa token local, redirect admin login.
- Khong luu refresh token vao log/console.

## 12. Acceptance Criteria

- **AC1:** Refresh hop le → access token moi + claims admin.
- **AC2:** Session revoked → 401, yeu cau login lai.
- **AC3:** User mat quyen admin → 403 sau refresh.
- **AC4:** User suspend khong refresh duoc.

## 13. Related

- `FR_AdminLogin.md`, `FR_AdminLogout.md`
- `FR_RefreshAccessToken.md` (user app)
- Implementation: `AdminRefreshTokenController`
