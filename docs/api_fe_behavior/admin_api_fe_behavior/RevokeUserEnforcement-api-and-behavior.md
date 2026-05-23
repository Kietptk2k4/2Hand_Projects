# Revoke User Enforcement – API & Behavior

## 1. Business Goal

Cho phép admin **revoke** một enforcement đang `ACTIVE`, ghi log, publish `USER_ENFORCEMENT_REVOKED` để Auth/Social/Commerce gỡ hiệu ứng.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/user-enforcements/{enforcementId}/revoke` | Bearer + `USER_ENFORCEMENT_REVOKE` |

**Request body (optional):**

```json
{
  "note": "False positive after review",
  "reason": "APPEAL_ACCEPTED"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "User enforcement revoked successfully",
  "data": {
    "enforcement_id": "uuid",
    "user_id": "uuid",
    "action_type": "RESTRICT",
    "status": "REVOKED",
    "revoked_by": "uuid",
    "updated_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Auth Service (delegation)

Khi `admin.integrations.auth.enabled=true`:

`POST /api/v1/admin/user-enforcements/{enforcementId}/revoke`

Body gồm `user_id`, `action_type`, `reactivate_user` (admin tính: revoke `SUSPEND`/`BAN` và không còn ACTIVE suspend/ban khác).

- `reactivate_user=true` → Auth set `users.status = ACTIVE` nếu đang `SUSPENDED`.
- `RESTRICT` revoke → Auth không đổi status, không revoke session.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `USER_ENFORCEMENT_REVOKE` |
| 404 | ADMIN-404 | Enforcement không tồn tại |
| 409 | ADMIN-409-ENFORCEMENT | Enforcement không ở trạng thái `ACTIVE` |
| 503 | ADMIN-503 | Auth integration bật nhưng Auth down |

## 5. Business Rules

- Chỉ `ACTIVE` → `REVOKED`; `REVOKED`/`EXPIRED` là terminal (409).
- Ghi `user_enforcement_logs` (ACTIVE → REVOKED).
- Outbox: `USER_ENFORCEMENT_REVOKED` → `admin.user.enforcement_revoked`.
- Audit critical: `USER_ENFORCEMENT_REVOKE`.
- Không xóa bản ghi enforcement.

## 6. FE Integration

1. Lấy `enforcementId` từ màn current enforcement / history.
2. `POST .../user-enforcements/{enforcementId}/revoke` với optional note/reason.
3. Refresh trạng thái enforcement của user.

## 7. Data Dependencies

| Service | Action |
|---------|--------|
| Admin Service | UPDATE `user_enforcements`, INSERT logs/audit/outbox |
| Auth Service | Optional reactivate user |
| Social / Commerce | Consume `USER_ENFORCEMENT_REVOKED` |

## 8. Config

| Key | Default |
|-----|---------|
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | Auth base URL |
