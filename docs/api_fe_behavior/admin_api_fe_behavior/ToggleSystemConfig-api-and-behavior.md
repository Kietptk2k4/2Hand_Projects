# Toggle System Config – API & Behavior

## 1. Business Goal

Cho phép super admin bật/tắt config qua `is_active` (tạm dừng runtime config / feature flag đơn giản), có history, audit và event cho consumer.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| PATCH | `/admin/api/v1/system-configs/{configId}/toggle` | Bearer + `SYSTEM_CONFIG_UPDATE` |

**Request body:**

| Field | Required | Mô tả |
|-------|----------|--------|
| `is_active` | Yes | Trạng thái mong muốn (`true` / `false`) |
| `reason` | Yes | Lý do toggle |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "System config toggled successfully",
  "data": {
    "config_id": "uuid",
    "config_key": "ALLOW_NEW_SELLER",
    "config_value": "true",
    "value_type": "BOOLEAN",
    "description": "Allow new seller registration",
    "is_active": false,
    "updated_by": "uuid",
    "updated_at": "2026-05-23T12:00:00Z",
    "history_id": "uuid",
    "outbox_event_id": "uuid",
    "state_changed": true
  }
}
```

Khi `is_active` đã đúng trạng thái yêu cầu (idempotent):

- `state_changed: false`
- `history_id` / `outbox_event_id`: `null`
- Message: `System config is already in the requested active state`

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_CONFIG_UPDATE` |
| 404 | ADMIN-404 | Config không tồn tại |
| 400 | ADMIN-400-VALIDATION | Thiếu `reason` |

## 4. Business Rules

- `config_value` không đổi khi toggle; chỉ `is_active`.
- History: `old_value` / `new_value` = `"true"` / `"false"` (trạng thái active).
- Audit `SYSTEM_CONFIG_TOGGLE` với before/after `is_active`.
- Outbox `SYSTEM_CONFIG_UPDATED`, `change_type: TOGGLED`.
- Config `is_active=false` không được consumer resolver dùng (trừ khi resolver cho phép explicit).

## 5. FE Integration

1. Switch bật/tắt → `PATCH .../system-configs/{configId}/toggle`.
2. Kiểm `state_changed` để hiển thị toast phù hợp.
3. Đổi value dùng `PATCH .../system-configs/{configId}` (FR_UpdateSystemConfig).

## 6. Related

| API | Mục đích |
|-----|----------|
| `POST .../system-configs` | Tạo config |
| `PATCH .../system-configs/{id}` | Đổi value |
| View history | FR_ViewSystemConfigHistory |
