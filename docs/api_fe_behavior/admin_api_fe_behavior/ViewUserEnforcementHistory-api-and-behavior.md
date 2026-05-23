# View User Enforcement History – API & Behavior

## 1. Business Goal

Cho phép admin/support xem **lịch sử enforcement** của user: mọi trạng thái (`ACTIVE`, `REVOKED`, `EXPIRED`) kèm log chuyển trạng thái để điều tra.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/users/{userId}/enforcements/history` | Bearer + `USER_ENFORCEMENT_READ` |

**Query params (optional):**

| Param | Default | Max |
|-------|---------|-----|
| `page` | `1` | — |
| `size` | `20` | `100` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "User enforcement history retrieved successfully",
  "data": {
    "user_id": "uuid",
    "page": 1,
    "size": 20,
    "total_elements": 2,
    "total_pages": 1,
    "enforcements": [
      {
        "enforcement_id": "uuid",
        "user_id": "uuid",
        "action_type": "RESTRICT",
        "reason_code": "SPAM",
        "description": "Spam comments",
        "expires_at": null,
        "enforced_by": "uuid",
        "status": "REVOKED",
        "created_at": "2026-05-20T10:00:00Z",
        "updated_at": "2026-05-23T10:00:00Z",
        "logs": [
          {
            "log_id": "uuid",
            "old_status": "ACTIVE",
            "new_status": "REVOKED",
            "admin_id": "uuid",
            "actor_type": "ADMIN",
            "note": "False positive",
            "created_at": "2026-05-23T10:00:00Z"
          },
          {
            "log_id": "uuid",
            "old_status": null,
            "new_status": "ACTIVE",
            "admin_id": "uuid",
            "actor_type": "ADMIN",
            "note": "Enforcement created",
            "created_at": "2026-05-20T10:00:00Z"
          }
        ]
      }
    ]
  }
}
```

- Enforcements sắp xếp `created_at` **mới nhất trước**.
- Logs trong mỗi enforcement sắp xếp `created_at` **mới nhất trước**.
- `actor_type`: `ADMIN` khi có `admin_id`, `SYSTEM` khi `admin_id` null (job expiration tương lai).

## 3. User validation

Giống View Current: khi `admin.integrations.auth.enabled=true`, gọi Auth public profile để 404 nếu user không tồn tại.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `USER_ENFORCEMENT_READ` |
| 404 | ADMIN-404 | User không tồn tại (Auth integration) |
| 400 | ADMIN-400-PAGINATION | `page` hoặc `size` không hợp lệ |
| 503 | ADMIN-503 | Auth down khi integration bật |

## 5. Business Rules

- Trả **tất cả** status enforcement (không chỉ ACTIVE).
- Pagination theo enforcement (không paginate logs riêng).
- Read-only, không audit/outbox.
- Không trả password/token/secret.

## 6. FE Integration

1. Tab investigation → `GET .../users/{userId}/enforcements/history?page=1&size=20`.
2. Timeline UI từ `logs` trong từng enforcement.
3. Phân trang theo `total_pages` / `total_elements`.

## 7. Related APIs

| API | Mục đích |
|-----|----------|
| `GET .../enforcements/current` | Chỉ ACTIVE (moderation nhanh) |
| `POST .../user-enforcements/{id}/revoke` | Revoke enforcement |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | Auth base URL |
