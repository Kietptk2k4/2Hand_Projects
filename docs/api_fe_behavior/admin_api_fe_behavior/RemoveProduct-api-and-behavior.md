# Remove Product – API & Behavior

## 1. Business Goal

Cho phép admin **remove** sản phẩm vi phạm: ghi moderation log, audit và publish `PRODUCT_REMOVED`. Commerce Service (consumer) own cập nhật `products.status = REMOVED`.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/products/{productId}/remove` | Bearer + `PRODUCT_REMOVE` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do remove (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Vi pham chinh sach noi dung",
  "note": "Bao cao tu user #123"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Product removed successfully",
  "data": {
    "product_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Vi pham chinh sach noi dung",
    "note": "Bao cao tu user #123",
    "removed_by": "uuid",
    "removed_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `PRODUCT_REMOVE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Product không tồn tại (khi `admin.integrations.commerce.enabled=true`) |
| 503 | ADMIN-503 | Commerce không phản hồi (khi integration bật) |

## 4. Business Rules

- Admin Service **không** ghi trực tiếp DB Commerce.
- Ghi `content_moderation_logs` (`target_type=PRODUCT`, `action=REMOVE`).
- Critical audit `PRODUCT_REMOVE`.
- Outbox `PRODUCT_REMOVED` → topic `admin.product.removed`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload (tránh lộ ghi chú nội bộ).
- Khi `admin.integrations.commerce.enabled=false` (mặc định test/local): không gọi Commerce để validate tồn tại.

## 5. Outbox payload

`product_id`, `moderation_log_id`, `action`, `reason`, `removed_by`, `removed_at`.

## 6. FE Integration

1. Màn product moderation → nhập `reason` (+ `note` tùy chọn) → `POST .../remove`.
2. Hiển thị toast success; refresh moderation history khi có API history.
3. Commerce cập nhật trạng thái product qua consumer event (không chờ đồng bộ trong response admin).

## 7. Related

| API | Mục đích |
|-----|----------|
| [RestoreProduct](./RestoreProduct-api-and-behavior.md) | Restore sản phẩm |
| [ViewProductModerationHistory](./ViewProductModerationHistory-api-and-behavior.md) | Xem lịch sử moderation |
| Commerce `POST /commerce/api/v1/admin/products/{productId}/remove` | Commerce-side remove (DB + cart invalidation) |

## 8. Permission

`PRODUCT_REMOVE`
