# View System Config History – API & Behavior

## 1. Business Goal

Cho phép admin/auditor xem **lịch sử thay đổi** config (create, update value, toggle) để audit và hỗ trợ rollback thủ công.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/system-configs/{configId}/history` | Bearer + `SYSTEM_CONFIG_VIEW` |

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
  "message": "System config history retrieved successfully",
  "data": {
    "config_id": "uuid",
    "config_key": "MAX_CART_ITEMS",
    "page": 1,
    "size": 20,
    "total_elements": 2,
    "total_pages": 1,
    "values_masked": false,
    "history": [
      {
        "history_id": "uuid",
        "config_key": "MAX_CART_ITEMS",
        "old_value": "50",
        "new_value": "100",
        "changed_by": "uuid",
        "reason": "Increase cart limit",
        "created_at": "2026-05-23T11:00:00Z",
        "values_masked": false
      },
      {
        "history_id": "uuid",
        "config_key": "MAX_CART_ITEMS",
        "old_value": null,
        "new_value": "50",
        "changed_by": "uuid",
        "reason": "Initial rollout",
        "created_at": "2026-05-23T10:00:00Z",
        "values_masked": false
      }
    ]
  }
}
```

- Sắp xếp `created_at` **mới nhất trước**.
- `old_value` / `new_value` cho **value change**; toggle ghi `"true"` / `"false"`.
- `changed_by`: UUID admin thực hiện.

## 3. Value masking

Nếu `config_key` secret-like (chứa `PASSWORD`, `SECRET`, `TOKEN`, `API_KEY`, …):

- `values_masked: true` ở cấp response và từng entry.
- `old_value` / `new_value` trả `********` (không lộ secret).

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_CONFIG_VIEW` |
| 404 | ADMIN-404 | Config không tồn tại |
| 400 | ADMIN-400-PAGINATION | `page` / `size` không hợp lệ |

## 5. Business Rules

- Read-only; không sửa/xóa history qua API.
- History append-only từ create/update/toggle.
- Không join Auth profile actor (MVP: chỉ `changed_by` UUID).

## 6. FE Integration

1. Màn config detail → tab History → `GET .../system-configs/{configId}/history?page=1&size=20`.
2. Hiển thị cảnh báo khi `values_masked=true`.
3. Phân trang theo `total_pages`.

## 7. Related

| API | Mục đích |
|-----|----------|
| `POST .../system-configs` | Tạo (history dòng đầu) |
| `PATCH .../system-configs/{id}` | Update value |
| `PATCH .../system-configs/{id}/toggle` | Toggle active |

## 8. Permission

`SYSTEM_CONFIG_VIEW` — tách khỏi `SYSTEM_CONFIG_UPDATE` để auditor chỉ đọc history.
