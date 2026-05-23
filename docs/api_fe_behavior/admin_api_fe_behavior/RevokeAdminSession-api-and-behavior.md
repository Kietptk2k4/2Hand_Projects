# Revoke Admin Session – API & Behavior

## 1. Business Goal

Cho phép super admin / admin có quyền **thu hồi refresh session** của admin khác (điều tra compromise, force sign-out). Admin Service authorize + audit; Auth Service sở hữu session state.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/admin-sessions/{sessionId}/revoke` | Bearer + `ADMIN_SESSION_REVOKE` |

**Request body (optional):**

```json
{
  "revoke_all_sessions": false
}
```

- `revoke_all_sessions: true` — revoke mọi session `ACTIVE` của target admin (user sở hữu `sessionId`).
- `false` hoặc bỏ body — chỉ revoke session `{sessionId}` nếu đang `ACTIVE`.

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Admin session revoked successfully",
  "data": {
    "target_admin_user_id": "uuid",
    "session_id": "uuid",
    "revoked_session_count": 1,
    "revoke_all_sessions": false
  }
}
```

## 3. Auth Service (delegation)

Khi `admin.integrations.auth.enabled=true`, Admin Service gọi:

`POST /api/v1/admin/sessions/{sessionId}/revoke` (forward Bearer)

Auth kiểm tra actor có `ADMIN_SESSION_REVOKE` (hoặc role `ADMIN`/`SUPER_ADMIN`) và target user là admin portal user.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | 401 | Thiếu JWT |
| 403 | 403 | Thiếu `ADMIN_SESSION_REVOKE` hoặc target không phải admin user |
| 404 | ADMIN-404 | Session không tồn tại |
| 503 | ADMIN-503 | Auth integration tắt hoặc Auth down |

## 5. Business Rules

- Permission bắt buộc: `ADMIN_SESSION_REVOKE` (JWT claims).
- Không log/expose refresh token hay token hash.
- Ghi `admin_action_logs` với `action_type = ADMIN_SESSION_REVOKE` (critical payload).
- Session đã `REVOKED`/`LOGGED_OUT` → `revoked_session_count = 0`, vẫn 200 (idempotent).
- Sau revoke, refresh token không refresh được.

## 6. FE Integration

1. Lấy `sessionId` từ màn investigation (Auth sessions API / future list).
2. `POST .../admin-sessions/{sessionId}/revoke` với Bearer.
3. Hiển thị `revoked_session_count`; confirm khi `revoke_all_sessions: true`.

## 7. Data Dependencies

| Service | Table / action |
|---------|----------------|
| Admin Service | `admin_action_logs` INSERT |
| Auth Service | `refresh_token_sessions` UPDATE → `REVOKED` |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | `http://localhost:3001` |

## 9. Related

- FR: `docs/feature_requirements/admin/FR_RevokeAdminSession.md`
- Logout (self): `AdminLogout-api-and-behavior.md`
- Flow: `docs/business_flow/admin_business_flow/admin-auth-authorization-flow.md`
