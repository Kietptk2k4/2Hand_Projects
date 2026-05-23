# Create System Config – API & Behavior

## 1. Business Goal

Cho phép super admin tạo runtime config mới cho platform/services với validation type, audit và event cho consumer.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/system-configs` | Bearer + `SYSTEM_CONFIG_UPDATE` |

**Request body:**

| Field | Required | Mô tả |
|-------|----------|--------|
| `config_key` | Yes | Unique, UPPER_SNAKE_CASE (vd. `MAX_CART_ITEMS`) |
| `config_value` | Yes | Phải khớp `value_type` |
| `value_type` | Yes | `INTEGER`, `DECIMAL`, `STRING`, `BOOLEAN`, `JSON` |
| `description` | No | Mô tả config |
| `is_active` | No | Default `true` |
| `reason` | Yes | Lý do tạo (ghi history + audit) |

**Success (201):**

```json
{
  "code": 201,
  "success": true,
  "message": "System config created successfully",
  "data": {
    "config_id": "uuid",
    "config_key": "MAX_CART_ITEMS",
    "config_value": "100",
    "value_type": "INTEGER",
    "description": "Max items per cart",
    "is_active": true,
    "created_by": "uuid",
    "created_at": "2026-05-23T10:00:00Z",
    "history_id": "uuid",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_CONFIG_UPDATE` |
| 400 | ADMIN-400-VALIDATION | Key/value/type/reason không hợp lệ; key chứa secret |
| 409 | ADMIN-409-CONFIG | `config_key` đã tồn tại |

## 4. Business Rules

- `config_key` unique, immutable sau khi tạo.
- Không cho phép key chứa `PASSWORD`, `SECRET`, `TOKEN`, `API_KEY`, …
- Ghi `system_config_history` (`old_value` = null, `new_value` = giá trị mới).
- Ghi `admin_action_logs` (`SYSTEM_CONFIG_CREATE`, target `CONFIG`).
- Outbox `SYSTEM_CONFIG_UPDATED` với `change_type: CREATED` → topic `admin.config.updated`.

## 5. Value type validation

| Type | Rule |
|------|------|
| INTEGER | Số nguyên hợp lệ |
| DECIMAL | Số thập phân hợp lệ |
| BOOLEAN | `true` / `false` (case-insensitive) |
| JSON | JSON hợp lệ |
| STRING | Bất kỳ chuỗi (kể cả rỗng) |

## 6. FE Integration

1. Form tạo config → `POST /admin/api/v1/system-configs`.
2. Hiển thị `config_id` / lỗi 409 nếu key trùng.
3. Tab history (FR riêng) query theo `config_key`.

## 7. Related

| API | Mục đích |
|-----|----------|
| `PUT/PATCH` update config (FR_UpdateSystemConfig) | Đổi value |
| Toggle / View history | FR riêng |
