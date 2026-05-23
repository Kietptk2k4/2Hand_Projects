# View Current User Enforcement – API & Behavior

## 1. Business Goal

Cho phép admin/support xem danh sách enforcement **đang ACTIVE** của user (suspend, ban, restrict) trước khi moderation hoặc revoke.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/users/{userId}/enforcements/current` | Bearer + `USER_ENFORCEMENT_READ` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Current user enforcements retrieved successfully",
  "data": {
    "user_id": "uuid",
    "enforcements": [
      {
        "enforcement_id": "uuid",
        "user_id": "uuid",
        "action_type": "RESTRICT",
        "reason_code": "SPAM",
        "description": "Spam comments",
        "expires_at": "2026-06-01T00:00:00Z",
        "enforced_by": "uuid",
        "created_at": "2026-05-23T10:00:00Z",
        "possibly_expired": false
      }
    ]
  }
}
```

- `possibly_expired: true` khi `expires_at` đã qua nhưng job expiration chưa cập nhật status (vẫn `ACTIVE` trong DB).

## 3. User validation

Khi `admin.integrations.auth.enabled=true`, Admin gọi Auth `GET /api/v1/users/{userId}/public-profile` để xác minh user tồn tại trước khi query enforcement.

Khi integration tắt, chỉ đọc `user_enforcements` (không 404 nếu user chưa từng có enforcement).

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `USER_ENFORCEMENT_READ` |
| 404 | ADMIN-404 | User không tồn tại (Auth integration) |
| 503 | ADMIN-503 | Auth down khi integration bật |

## 5. Business Rules

- Chỉ trả `status = ACTIVE`.
- `REVOKED` / `EXPIRED` không xuất hiện.
- Nhiều ACTIVE cùng lúc được phép nếu khác `action_type`.
- Sắp xếp theo `created_at` mới nhất trước.
- Read-only transaction.

## 6. FE Integration

1. Màn investigation user → `GET .../users/{userId}/enforcements/current`.
2. Hiển thị badge SUSPEND / BAN / RESTRICT; cảnh báo nếu `possibly_expired`.
3. Link revoke tới `POST .../user-enforcements/{enforcementId}/revoke`.

## 7. Data Dependencies

| Source | Table |
|--------|--------|
| Admin Service | `user_enforcements` (read) |
| Auth Service | Public profile lookup (optional) |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | Auth base URL |
