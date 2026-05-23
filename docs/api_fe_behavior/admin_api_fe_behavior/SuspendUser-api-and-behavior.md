# Suspend User – API & Behavior

## 1. Business Goal

Cho phép admin/mod có quyền **suspend** user: lưu enforcement trên Admin Service, audit, publish `USER_SUSPENDED`; Auth Service (khi bật integration) set `SUSPENDED` và revoke refresh sessions.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/users/{userId}/suspend` | Bearer + `USER_SUSPEND` |

**Request body:**

```json
{
  "reason_code": "POLICY_VIOLATION",
  "description": "Repeated abusive content",
  "expires_at": "2026-06-01T00:00:00Z"
}
```

- `reason_code`, `description`: bắt buộc.
- `expires_at`: optional; có giá trị phải **trong tương lai** (temporary suspend). Bỏ hoặc `null` = permanent.

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "User suspended successfully",
  "data": {
    "enforcement_id": "uuid",
    "user_id": "uuid",
    "reason_code": "POLICY_VIOLATION",
    "status": "ACTIVE",
    "expires_at": null,
    "enforced_by": "uuid",
    "created_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Auth Service (delegation)

Khi `admin.integrations.auth.enabled=true`, Admin Service gọi trước khi commit local DB:

`POST /api/v1/admin/users/{userId}/suspend` (forward Bearer)

Body: `enforcement_id`, `reason_code`, `description`, `expires_at`.

Auth kiểm tra actor có `USER_SUSPEND` (hoặc role `ADMIN`/`SUPER_ADMIN`), set user `SUSPENDED`, `revokeAllByUserId`.

Khi integration **tắt**, chỉ ghi Admin DB + outbox; consumer/event áp dụng Auth sau.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `USER_SUSPEND` |
| 404 | ADMIN-404 | User không tồn tại (Auth integration) |
| 409 | ADMIN-409-ENFORCEMENT | Đã có ACTIVE `SUSPEND` enforcement |
| 400 | ADMIN-400-VALIDATION | `reason_code`/`description` thiếu hoặc `expires_at` không hợp lệ |
| 503 | ADMIN-503 | Auth integration bật nhưng Auth down |

## 5. Business Rules

- Permission: `USER_SUSPEND` (JWT claims).
- `admin_id` / `enforced_by` lấy từ JWT, không từ body.
- Không duplicate ACTIVE `SUSPEND` trên cùng `user_id`.
- Ghi `user_enforcements`, `user_enforcement_logs`, `admin_action_logs` (critical), `outbox_events` (`USER_SUSPENDED` → topic `admin.user.suspended`).
- Transaction: tất cả Admin writes trong một transaction; outbox cùng transaction.

## 6. FE Integration

1. Form: reason code (select), description, optional expiry datetime.
2. `POST .../users/{userId}/suspend` với Bearer.
3. Hiển thị `enforcement_id`, `outbox_event_id` nếu cần debug; confirm permanent vs temporary.

## 7. Data Dependencies

| Service | Table / action |
|---------|----------------|
| Admin Service | `user_enforcements`, `user_enforcement_logs`, `admin_action_logs`, `outbox_events` |
| Auth Service | `users.status` → `SUSPENDED`, `refresh_token_sessions` → `REVOKED` |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | Auth base URL (vd `http://localhost:3001`) |
