# View Product Moderation History – API & Behavior

## 1. Business Goal

Cho phép admin xem **lịch sử moderation** của một product (remove/restore): action, reason, note, admin id và timestamp. Chỉ đọc `content_moderation_logs` trong Admin Service; không bao gồm lịch sử chỉnh sửa Commerce hay đơn hàng.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/products/{productId}/moderation-history` | Bearer + `PRODUCT_MODERATION_READ` |

### Query params

| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `page` | int | no | Trang (mặc định `1`, tối thiểu `1`) |
| `size` | int | no | Kích thước trang (mặc định `20`, tối đa `100`) |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Product moderation history retrieved successfully",
  "data": {
    "product_id": "uuid",
    "page": 1,
    "size": 20,
    "total_elements": 2,
    "total_pages": 1,
    "history": [
      {
        "moderation_log_id": "uuid",
        "action": "RESTORE",
        "reason": "Appeal approved",
        "note": "Restore note",
        "admin_id": "uuid",
        "created_at": "2026-05-23T11:00:00Z"
      },
      {
        "moderation_log_id": "uuid",
        "action": "REMOVE",
        "reason": "Policy violation",
        "note": "Remove note",
        "admin_id": "uuid",
        "created_at": "2026-05-23T10:00:00Z"
      }
    ]
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `PRODUCT_MODERATION_READ` |
| 400 | ADMIN-400-PAGINATION | `page` / `size` không hợp lệ |

## 4. Business Rules

- Filter `content_moderation_logs` với `target_type = PRODUCT` và `target_id = {productId}` (UUID dạng string).
- Sắp xếp `created_at DESC` (mới nhất trước).
- Product chưa có log → `history: []`, `total_elements: 0` (không 404).
- Read-only; không gọi Commerce; không ghi audit/outbox.

## 5. FE Integration

1. Màn product moderation detail → `GET .../moderation-history?page=1&size=20`.
2. Hiển thị timeline/table theo `history`; badge theo `action` (`REMOVE`, `RESTORE`).
3. Phân trang khi `total_pages > 1`.

## 6. Related

| API | Mục đích |
|-----|----------|
| [RemoveProduct](./RemoveProduct-api-and-behavior.md) | Remove product |
| [RestoreProduct](./RestoreProduct-api-and-behavior.md) | Restore product |

## 7. Permission

`PRODUCT_MODERATION_READ`
