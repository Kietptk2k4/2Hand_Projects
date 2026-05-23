# Restore Product – API & Behavior

## 1. Business Goal

Cho phép admin **restore** sản phẩm đã bị moderation remove/hide: ghi moderation log, audit và publish `PRODUCT_RESTORED`. Commerce Service (consumer) validate readiness và quyết định trạng thái cuối (`ACTIVE`, `OUT_OF_STOCK`, `PAUSED`, …).

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/products/{productId}/restore` | Bearer + `PRODUCT_RESTORE` **hoặc** `PRODUCT_REMOVE` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do restore (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Khang cao duoc chap nhan",
  "note": "Ticket #456"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Product restored successfully",
  "data": {
    "product_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Khang cao duoc chap nhan",
    "note": "Ticket #456",
    "restored_by": "uuid",
    "restored_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `PRODUCT_RESTORE` và `PRODUCT_REMOVE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 409 | ADMIN-409-MODERATION | Commerce từ chối restore (khi có sync validation với Commerce) |

## 4. Business Rules

- Admin Service **không** ép product `ACTIVE` trực tiếp trên Commerce DB.
- Ghi `content_moderation_logs` (`target_type=PRODUCT`, `action=RESTORE`).
- Critical audit `PRODUCT_RESTORE`.
- Outbox `PRODUCT_RESTORED` → topic `admin.product.restored`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Không gọi public product detail để validate (sản phẩm `REMOVED` thường không hiển thị trên catalog API).

## 5. Outbox payload

`product_id`, `moderation_log_id`, `action`, `reason`, `restored_by`, `restored_at`.

## 6. FE Integration

1. Màn moderation history / product detail → nhập `reason` → `POST .../restore`.
2. Commerce cập nhật trạng thái qua consumer event (response admin không chứa `status` cuối).
3. Hiển thị message success; refresh khi có API moderation history.

## 7. Related

| API | Mục đích |
|-----|----------|
| [RemoveProduct](./RemoveProduct-api-and-behavior.md) | Remove sản phẩm |
| [ViewProductModerationHistory](./ViewProductModerationHistory-api-and-behavior.md) | Xem lịch sử moderation |

## 8. Permission

`PRODUCT_RESTORE` hoặc `PRODUCT_REMOVE`
