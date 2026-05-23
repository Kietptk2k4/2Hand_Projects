# Functional Requirement (FR) - View User Sessions For Admin

## 1. Feature Overview

Cho phep admin/support xem danh sach **refresh token sessions** dang active (va co the ca history ngan) cua user muc tieu de dieu tra truy cap bat thuong hoac ho tro revoke session.

Khac `FR_ViewLoginSesssionList` (user xem session **cua chinh minh**).

## 2. Actors

- **Support / Admin:** Dieu tra session.
- **Auth Service:** Own `refresh_token_sessions`.

## 3. Scope

- **In Scope:**
  - List sessions theo `user_id`.
  - Tra metadata: `session_id`, `device_id`, `ip_address`, `user_agent`, `status`, `created_at`, `updated_at`.
  - Mac dinh uu tien `ACTIVE`; optional query `status`.
- **Out of Scope:**
  - Tra `token_hash` / refresh token plaintext.
  - Revoke session (dung revoke APIs rieng; admin revoke admin session: `FR_RevokeAdminSession`).

## 4. Preconditions

- Actor co permission dieu tra.
- Target user ton tai.

## 5. API Contract

**Endpoint:** `GET /api/v1/admin/users/{userId}/sessions`

**Auth:** Required (admin JWT)

**Query params:**

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `status` | string | `ACTIVE` | `ACTIVE`, `LOGGED_OUT`, `REVOKED`, `EXPIRED` hoac `ALL` |
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach phien dang nhap thanh cong.",
  "data": {
    "user_id": "uuid",
    "sessions": [
      {
        "session_id": "uuid",
        "device_id": "device-abc",
        "ip_address": "203.0.113.1",
        "user_agent": "Chrome/120 ...",
        "status": "ACTIVE",
        "created_at": "2026-05-19T12:00:00Z",
        "updated_at": "2026-05-19T12:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 2,
      "has_next": false
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

**Admin Service gateway:** `GET /admin/api/v1/users/{userId}/active-sessions` — delegate Auth.

## 6. Business Rules

- Khong bao gio tra refresh token hoac hash.
- Sap xep `created_at DESC`.
- User khong ton tai → `404`.
- Permission bat buoc.

## 7. Database Impact

- Read-only `refresh_token_sessions`.

## 8. Transaction

- Read-only.

## 9. Security

- Chi admin co quyen investigation.
- Optional audit log khi support read.

## 10. Failure Cases

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `AUTH-401` | Thieu JWT |
| 403 | `AUTH-403` | Khong du quyen |
| 404 | `AUTH-404` | User khong ton tai |

## 11. FE Behavior

- Tab "Active sessions" trong user investigation.
- Nut "Revoke all sessions" (neu co API rieng cho target user — co the mo rong sau MVP).

## 12. Acceptance Criteria

- **AC1:** Admin xem duoc session ACTIVE cua user.
- **AC2:** Khong leak token.
- **AC3:** User khong ton tai → 404.

## 13. Related

- `FR_ViewLoginSesssionList.md`, `FR_ViewLoginHistoryForAdmin.md`
- `docs/feature_requirements/admin/FR_ViewUserActiveSessions.md`
- `docs/use_cases/admin_use_cases/uc-user-investigation.md`
