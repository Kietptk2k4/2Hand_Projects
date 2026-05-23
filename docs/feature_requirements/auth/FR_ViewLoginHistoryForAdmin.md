# Functional Requirement (FR) - View Login History For Admin

## 1. Feature Overview

Cho phep admin/support co quyen dieu tra xem **lich su dang nhap** cua mot user muc tieu. Auth Service own `LOGIN_LOGS`. Admin Service co the proxy request nhung **khong** truy cap DB Auth truc tiep.

Khac `FR_TrackLoginHistory` (user xem lich su **cua chinh minh**).

## 2. Actors

- **Support / Admin / Super Admin:** Dieu tra account.
- **Auth Service:** Cung cap du lieu read-only.
- **Admin Service (optional):** Gateway + audit log support read.

## 3. Scope

- **In Scope:**
  - List login attempts theo `user_id`.
  - Filter/pagination: `page`, `limit`, optional `from`, `to`, `success`.
  - Tra: `login_method`, `ip_address`, `user_agent`, `success`, `created_at`.
- **Out of Scope:**
  - Sua/xoa login log.
  - Revoke session (dung `FR_ViewUserSessionsForAdmin` + revoke APIs).
  - Tra password/token.

## 4. Preconditions

- Actor co permission dieu tra (vi du `USER_INVESTIGATION_READ` hoac role `SUPPORT`/`ADMIN`).
- Target user ton tai (hoac policy tra 404 chung).

## 5. API Contract

**Service:** Auth Service (source-of-truth)

**Endpoint:** `GET /api/v1/admin/users/{userId}/login-history`

**Auth:** Required (admin JWT)

**Query params:**

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–100 |
| `success` | boolean | — | Loc thanh cong / that bai |
| `from` | instant | — | Tu thoi diem (optional) |
| `to` | instant | — | Den thoi diem (optional) |

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay lich su dang nhap thanh cong.",
  "data": {
    "user_id": "uuid",
    "items": [
      {
        "login_method": "EMAIL",
        "ip_address": "203.0.113.1",
        "user_agent": "Mozilla/5.0 ...",
        "success": true,
        "created_at": "2026-05-20T08:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 42,
      "total_pages": 3,
      "has_next": true
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

**Admin Service gateway (neu co):** `GET /admin/api/v1/users/{userId}/login-history` — delegate Auth, optional ghi `admin_action_logs`.

## 6. Business Rules

- Chi user co permission moi goi duoc.
- Sap xep `created_at DESC`.
- Khong tra token, password, refresh token.
- User khong ton tai → `404` `AUTH-404`.
- Support read co the audit-log phia Admin (policy).

## 7. Database Impact

- Read-only `LOGIN_LOGS` filtered by `user_id`.

## 8. Transaction

- Read-only.

## 9. Security

- Permission bat buoc; fail closed.
- Mask/truncate `user_agent` neu qua dai (optional).
- Khong expose PII ngoai muc dieu tra can thiet.

## 10. Failure Cases

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `AUTH-401` | Thieu JWT |
| 403 | `AUTH-403` | Khong du quyen |
| 404 | `AUTH-404` | User khong ton tai |
| 400 | `AUTH-400-VALIDATION` | Pagination/filter sai |

## 11. FE Behavior

- Man hinh investigation user: tab "Login history".
- Badge success/fail.
- Pagination infinite scroll hoac page.

## 12. Acceptance Criteria

- **AC1:** Admin co quyen xem duoc lich su login user ton tai.
- **AC2:** Khong co secret trong response.
- **AC3:** User khong ton tai → 404.
- **AC4:** Phan trang dung total.

## 13. Related

- `FR_TrackLoginHistory.md` (self-service)
- `docs/feature_requirements/admin/FR_ViewUserLoginHistory.md` (delegate)
- `docs/use_cases/admin_use_cases/uc-user-investigation.md`
