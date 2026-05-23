# Restrict User – API & Behavior

## 1. Business Goal

Cho phép admin **restrict** user: user vẫn **login** và mua hàng (theo policy), nhưng bị chặn write actions (post, comment, review, create product) qua event `USER_RESTRICTED` cho Social/Commerce.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/users/{userId}/restrict` | Bearer + `USER_RESTRICT` |

**Request body:**

```json
{
  "reason_code": "SPAM",
  "description": "Repeated spam comments",
  "expires_at": "2026-06-01T00:00:00Z"
}
```

- `reason_code`, `description`: bắt buộc.
- `expires_at`: optional; nếu có phải trong tương lai.

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "User restricted successfully",
  "data": {
    "enforcement_id": "uuid",
    "user_id": "uuid",
    "reason_code": "SPAM",
    "status": "ACTIVE",
    "expires_at": null,
    "enforced_by": "uuid",
    "created_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Auth Service (delegation)

Khi `admin.integrations.auth.enabled=true`:

`POST /api/v1/admin/users/{userId}/restrict` (forward Bearer)

Auth **chỉ xác minh** user tồn tại và actor có `USER_RESTRICT`. **Không** đổi `users.status`, **không** revoke sessions (`revoked_session_count = 0`).

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `USER_RESTRICT` |
| 404 | ADMIN-404 | User không tồn tại |
| 409 | ADMIN-409-ENFORCEMENT | Đã có ACTIVE `RESTRICT` |
| 400 | ADMIN-400-VALIDATION | Payload không hợp lệ |
| 503 | ADMIN-503 | Auth integration bật nhưng Auth down |

## 5. Business Rules

- Enforcement `action_type = RESTRICT`, status `ACTIVE`.
- Outbox: `USER_RESTRICTED` → topic `admin.user.restricted`.
- Audit critical: `USER_RESTRICT`.
- Login không bị chặn bởi restrict (khác suspend/ban).
- Social/Commerce consumer áp dụng write-block từ event/cache.

## 6. FE Integration

1. Form: reason, description, optional expiry.
2. `POST .../users/{userId}/restrict` với Bearer.
3. Giải thích UX: user vẫn đăng nhập nhưng không đăng bài/bình luận/đánh giá/tạo sản phẩm.

## 7. Data Dependencies

| Service | Table / action |
|---------|----------------|
| Admin Service | `user_enforcements` (`RESTRICT`), logs, audit, outbox |
| Auth Service | Read-only user lookup (MVP) |
| Social / Commerce | Consume `USER_RESTRICTED` |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | Auth base URL |
