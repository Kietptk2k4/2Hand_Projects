# View Admin Action Logs – API & Behavior

## 1. Business Goal

Cho auditor/super admin **tra cứu** admin action logs theo actor, action, target, status và thời gian (read-only).

## 2. API Contract

### List logs

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/admin-action-logs` | Bearer + `ADMIN_AUDIT_VIEW` |

**Query params (optional):**

| Param | Mô tả |
|-------|--------|
| `admin_id` | UUID admin thực hiện |
| `action` | `action_type` (vd. `USER_SUSPEND`) |
| `target_type` | Loại target (vd. `USER`) |
| `target_id` | ID target |
| `status` | `SUCCESS` hoặc `FAILURE` |
| `from` | ISO-8601 instant (inclusive) |
| `to` | ISO-8601 instant (inclusive) |
| `page` | Default `1` |
| `size` | Default `20`, max `100` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Admin action logs retrieved successfully",
  "data": {
    "page": 1,
    "size": 20,
    "total_elements": 1,
    "total_pages": 1,
    "logs": [
      {
        "log_id": "uuid",
        "admin_id": "uuid",
        "action_type": "USER_SUSPEND",
        "target_type": "USER",
        "target_id": "uuid",
        "status": "SUCCESS",
        "request_payload": { "reason": "spam" },
        "response_payload": { "status": "SUCCESS", "result": {} },
        "ip_address": "10.0.0.1",
        "user_agent": "Mozilla/5.0",
        "created_at": "2026-05-23T10:00:00Z"
      }
    ]
  }
}
```

Sắp xếp `created_at` **mới nhất trước**.

### Log detail

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/admin-action-logs/{logId}` | Bearer + `ADMIN_AUDIT_VIEW` |

Trả cùng schema một entry như trong list.

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `ADMIN_AUDIT_VIEW` |
| 404 | ADMIN-404 | Log không tồn tại (detail) |
| 400 | ADMIN-400-VALIDATION | Date range / status / action không hợp lệ |
| 400 | ADMIN-400-PAGINATION | `page` / `size` không hợp lệ |

## 4. Business Rules

- Logs **append-only**, không có API sửa/xóa.
- Payload trả về được **sanitize** (redact password, token, secret, …).
- `status` đọc từ `response_payload.status` (không có cột riêng trong MVP schema).

## 5. FE Integration

1. Màn Audit → filter form → `GET .../admin-action-logs?admin_id=&action=&page=1`.
2. Click row → `GET .../admin-action-logs/{logId}`.
3. Hiển thị JSON payload đã redact; không hiển thị raw secrets.

## 6. Permission

`ADMIN_AUDIT_VIEW` (FR). Có thể map song song `ADMIN_AUDIT_READ` trên Auth nếu RBAC hiện tại dùng tên cũ.

## 7. Related

| Doc | Mục đích |
|-----|----------|
| LogAdminAction-internal | Ghi log |
| LogCriticalAdminActionPayload-internal | Payload critical |
