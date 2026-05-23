# Functional Requirement (FR) - Revoke Admin Session

## 1. Feature Overview

Cho phep super admin (hoac admin co permission) thu hoi refresh session cua admin khac — mot session cu the hoac toan bo session — qua Auth Service. Dung cho bao mat khi mat thiet bi, thay doi quyen, hoac can buoc admin dang nhap lai.

Admin Service co the delegate request; **Auth own** `refresh_token_sessions`.

## 2. Actors

- **Super Admin / Authorized Admin:** Thuc hien revoke.
- **Target Admin:** Bi thu hoi session.
- **Auth Service:** Ap dung revoke.

## 3. Scope

- **In Scope:**
  - Revoke mot session theo `session_id`.
  - Optional revoke all sessions cua target admin (`revoke_all_sessions = true`).
  - Kiem tra actor co permission (vi du `ADMIN_SESSION_REVOKE` hoac role `SUPER_ADMIN`).
  - Kiem tra target la admin user.
- **Out of Scope:**
  - Revoke session user app thuong (policy rieng).
  - Password reset.

## 4. Preconditions

- Actor da dang nhap admin JWT.
- Target session ton tai (neu revoke single).

## 5. API Contract

**Service:** Auth Service (source-of-truth)

**Endpoint:** `POST /api/v1/admin/sessions/{sessionId}/revoke`

**Auth:** Required (admin JWT)

**Path params:**

| Param | Type | Mo ta |
|-------|------|-------|
| `sessionId` | UUID | ID refresh session can revoke |

**Request body (optional):**

```json
{
  "revoke_all_sessions": false,
  "target_admin_user_id": "uuid-optional-when-revoke-all"
}
```

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Thu hoi phien admin thanh cong.",
  "data": {
    "target_admin_user_id": "uuid",
    "session_id": "uuid",
    "revoked_session_count": 1,
    "revoke_all_sessions": false
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

**Admin Service gateway (neu co):** `POST /admin/api/v1/admin-sessions/{sessionId}/revoke` — delegate sang Auth.

## 6. Business Rules

- Actor khong duoc revoke session cua chinh minh neu policy cam (tuy cau hinh).
- Target session phai thuoc admin user (co role admin).
- `revoke_all_sessions = true` → revoke tat ca `ACTIVE` sessions cua target admin.
- Session da `REVOKED`/`LOGGED_OUT` → idempotent, `revoked_session_count` co the = 0.
- Khong tra `token_hash`.
- Admin Service (neu proxy) ghi `admin_action_logs` cho critical action.

## 7. Database Impact

- **refresh_token_sessions:** update `status = REVOKED` (hoac `LOGGED_OUT`).

## 8. Transaction

- Batch update khi revoke all.

## 9. Security

- Permission bat buoc; fail closed `403`.
- Audit log phia Admin khi qua gateway.
- Khong log token.

## 10. Failure Cases

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `AUTH-401` | Thieu JWT |
| 403 | `AUTH-403` | Khong du quyen |
| 404 | `AUTH-404` | Session khong ton tai |

## 11. FE Behavior

- Man hinh quan tri admin sessions (neu co).
- Xac nhan truoc khi revoke all.
- Hien so session bi revoke.

## 12. Acceptance Criteria

- **AC1:** Revoke single session thanh cong.
- **AC2:** Revoke all sessions cua target admin.
- **AC3:** Target khong refresh duoc sau revoke.
- **AC4:** User khong phai admin khong bi anh huong boi endpoint nay.

## 13. Related

- `docs/feature_requirements/admin/FR_RevokeAdminSession.md` (delegate)
- `FR_AdminLogout.md`
- Implementation: `AdminSessionController`
