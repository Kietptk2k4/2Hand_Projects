# View User Profile For Investigation – API & Behavior

## 1. Business Goal

Cho phép admin/support xem **profile user** phục vụ điều tra (abuse/spam/suspicious), kèm tóm tắt enforcement đang ACTIVE từ Admin DB. Dữ liệu profile do Auth Service sở hữu.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/users/{userId}/profile` | Bearer + `USER_INVESTIGATION_READ` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "User investigation profile retrieved successfully",
  "data": {
    "user_id": "uuid",
    "email": "user@example.com",
    "status": "ACTIVE",
    "email_verified": true,
    "phone_verified": false,
    "last_login_at": "2026-05-22T08:00:00Z",
    "created_at": "2026-01-01T00:00:00Z",
    "display_name": "Kiet Tran",
    "avatar_url": "https://cdn.example/avatar.png",
    "bio": "Backend engineer",
    "website": "https://example.com",
    "is_private": false,
    "current_enforcements": [
      {
        "enforcement_id": "uuid",
        "action_type": "RESTRICT",
        "reason_code": "SPAM",
        "status": "ACTIVE",
        "expires_at": null,
        "possibly_expired": false
      }
    ]
  }
}
```

- Không trả password, token, OTP, session secret.
- Profile lấy từ Auth (không mask privacy — support view).
- `current_enforcements`: chỉ `ACTIVE` từ `user_enforcements`.

## 3. Auth integration

Khi `admin.integrations.auth.enabled=true`:

- Admin forward Bearer token tới Auth.
- Auth: `GET /api/v1/admin/users/{userId}/investigation-profile` (actor cần `USER_INVESTIGATION_READ` trong Auth RBAC).

Khi integration **tắt**: `503` — investigation profile bắt buộc Auth.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `USER_INVESTIGATION_READ` (Admin hoặc Auth từ chối) |
| 404 | ADMIN-404 | User không tồn tại / DELETED |
| 503 | ADMIN-503 | Auth integration tắt hoặc Auth không khả dụng |

## 5. Business Rules

- Read-only; không ghi audit bắt buộc (MVP).
- Admin compose: Auth profile + Admin enforcement summary.
- Enforcement chi tiết: dùng `GET .../enforcements/current` hoặc `.../history`.

## 6. FE Integration

1. Màn investigation → `GET .../users/{userId}/profile` (Bearer admin).
2. Hiển thị account status, email, profile + badge enforcement từ `current_enforcements`.
3. Tab khác: login history, sessions, roles (FR riêng).

## 7. Related APIs

| API | Mục đích |
|-----|----------|
| `GET .../enforcements/current` | Chi tiết enforcement ACTIVE |
| `GET .../enforcements/history` | Timeline enforcement |
| Auth `GET /api/v1/admin/users/{userId}/investigation-profile` | Nguồn profile (internal) |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | Auth base URL |
