# Ban User – API & Behavior

## 1. Business Goal

Cho phép admin **ban** user: lưu enforcement type `BAN` (tách biệt suspend), audit, publish `USER_BANNED`. MVP: Auth áp dụng hiệu ứng giống suspend (`SUSPENDED` + revoke sessions); event `USER_BANNED` để Social/Commerce phân biệt quyết định nghiệp vụ.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/users/{userId}/ban` | Bearer + `USER_BAN` **hoặc** `USER_SUSPEND` |

**Request body:**

```json
{
  "reason_code": "FRAUD",
  "description": "Confirmed payment fraud",
  "expires_at": null
}
```

- `reason_code`, `description`: bắt buộc.
- `expires_at`: optional; thường `null` (permanent ban). Nếu có, phải trong tương lai.

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "User banned successfully",
  "data": {
    "enforcement_id": "uuid",
    "user_id": "uuid",
    "reason_code": "FRAUD",
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

`POST /api/v1/admin/users/{userId}/ban` (forward Bearer)

Actor cần `USER_BAN` hoặc `USER_SUSPEND` (hoặc role `ADMIN`/`SUPER_ADMIN`). Auth set `SUSPENDED`, revoke refresh sessions.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `USER_BAN` và `USER_SUSPEND` |
| 404 | ADMIN-404 | User không tồn tại |
| 409 | ADMIN-409-ENFORCEMENT | Đã có ACTIVE `BAN` enforcement |
| 400 | ADMIN-400-VALIDATION | Payload không hợp lệ |
| 503 | ADMIN-503 | Auth integration bật nhưng Auth down |

## 5. Business Rules

- Ban và suspend là hai enforcement riêng (`action_type` khác nhau).
- Chỉ một ACTIVE `BAN` trên cùng user.
- Outbox: `USER_BANNED` → topic `admin.user.banned`.
- Audit critical: `action_type = USER_BAN`.
- Có thể tồn tại đồng thời ACTIVE `SUSPEND` và `BAN` (MVP Auth chỉ thấy `SUSPENDED`).

## 6. FE Integration

1. Form ban: reason, description; mặc định không expiry.
2. `POST .../users/{userId}/ban` với Bearer.
3. Confirm permanent ban trước khi gửi.

## 7. Data Dependencies

| Service | Table / action |
|---------|----------------|
| Admin Service | `user_enforcements` (`BAN`), logs, audit, outbox |
| Auth Service | `users.status`, `refresh_token_sessions` |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | Auth base URL |
