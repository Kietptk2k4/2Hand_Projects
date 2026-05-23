# Functional Requirement (FR) - Admin Logout

## 1. Feature Overview

Cho phep admin dang xuat khoi admin portal bang cach vo hieu hoa refresh session hien tai. Client tu xoa access/refresh token local.

## 2. Actors

- **Admin User:** Dang nhap admin portal.
- **Auth Service:** Revoke refresh session.

## 3. Scope

- **In Scope:**
  - Nhan refresh token can logout.
  - Danh dau session `LOGGED_OUT` hoac `REVOKED`.
  - Tra `200` idempotent (logout lai lan 2 van thanh cong).
- **Out of Scope:**
  - Logout all admin sessions (dung `FR_RevokeAdminSession` voi revoke all).
  - User app logout (`FR_Logout`).

## 4. Preconditions

- Admin da co JWT access token (optional cho audit actor).
- Refresh token hop le hoac da logout (idempotent).

## 5. API Contract

**Endpoint:** `POST /api/v1/auth/admin/logout`

**Auth:** Required (JWT access token — xac dinh actor)

**Request body:**

```json
{
  "refresh_token": "refresh-token-value"
}
```

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Dang xuat admin thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 6. Business Rules

- `actor_admin_id` lay tu JWT, khong tu body.
- Session khop refresh token + user → `LOGGED_OUT`.
- Token khong ton tai / da logout → van `200` (idempotent).
- Access token con hieu luc den khi het han JWT TTL (stateless); client phai xoa local.
- Khong log refresh token.

## 7. Database Impact

- **refresh_token_sessions:** update status session.

## 8. Transaction

- Single update transaction.

## 9. Security

- JWT required de gan audit actor (neu can ghi log).
- HTTPS.

## 10. Failure Cases

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `AUTH-401` | Thieu JWT |

## 11. FE Behavior

- Nut "Dang xuat" goi API + xoa token storage.
- Redirect ve admin login.
- Clear axios/fetch interceptors.

## 12. Acceptance Criteria

- **AC1:** Logout thanh cong → session khong refresh duoc nua.
- **AC2:** Logout 2 lan van `200`.
- **AC3:** Client xoa token sau logout.

## 13. Related

- `FR_AdminLogin.md`, `FR_AdminRefreshToken.md`, `FR_RevokeAdminSession.md`
- Implementation: `AdminLogoutController`
