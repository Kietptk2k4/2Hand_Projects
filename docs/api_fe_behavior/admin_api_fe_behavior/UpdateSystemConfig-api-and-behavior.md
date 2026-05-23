# Update System Config – API & Behavior

## 1. Business Goal

Cho phép super admin cập nhật **giá trị** (và tùy chọn **mô tả**) của runtime config, ghi history before/after, audit và publish event cho consumer.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| PATCH | `/admin/api/v1/system-configs/{configId}` | Bearer + `SYSTEM_CONFIG_UPDATE` |

**Path param:** `configId` (UUID)

**Request body:**

| Field | Required | Mô tả |
|-------|----------|--------|
| `config_value` | Yes | Giá trị mới; phải khớp `value_type` hiện có |
| `description` | No | Bỏ qua field → giữ description cũ |
| `reason` | Yes | Lý do thay đổi (history + audit) |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "System config updated successfully",
  "data": {
    "config_id": "uuid",
    "config_key": "MAX_CART_ITEMS",
    "config_value": "120",
    "value_type": "INTEGER",
    "description": "Updated cart limit",
    "is_active": true,
    "updated_by": "uuid",
    "updated_at": "2026-05-23T11:00:00Z",
    "history_id": "uuid",
    "outbox_event_id": "uuid"
  }
}
```

- `config_key` và `value_type` **không đổi** qua PATCH này.

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_CONFIG_UPDATE` |
| 404 | ADMIN-404 | Config không tồn tại |
| 400 | ADMIN-400-VALIDATION | `config_value` không hợp lệ theo type; thiếu `reason` |

## 4. Business Rules

- `config_key` immutable.
- History: `old_value` = giá trị trước, `new_value` = giá trị sau (chỉ track value trong history row).
- Audit `SYSTEM_CONFIG_UPDATE` với before/after summary.
- Outbox `SYSTEM_CONFIG_UPDATED`, `change_type: UPDATED` → `admin.config.updated`.
- Toggle `is_active` dùng FR_ToggleSystemConfig (không qua PATCH này).

## 5. FE Integration

1. Form edit → `PATCH .../system-configs/{configId}` với `reason` bắt buộc.
2. Sau success, refresh giá trị hiện tại; tab history (FR_ViewSystemConfigHistory) hiển thị dòng mới.

## 6. Related

| API | Mục đích |
|-----|----------|
| `POST .../system-configs` | Tạo config |
| Toggle / View history | FR riêng |
