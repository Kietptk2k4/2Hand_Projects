# Reopen Shop – API & Behavior

## 1. Business Goal

Cho phép admin **reopen** shop đã bị suspend/close: đồng bộ HTTP sang Commerce (`action=RESTORE`), ghi moderation log, audit và publish `SHOP_RESTORED` cho Notification. Commerce áp dụng trạng thái `ACTIVE`; admin **không** auto republish sản phẩm đã remove/archive.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/shops/{shopId}/reopen` | Bearer + `SHOP_RESTORE` **hoặc** `SHOP_SUSPEND` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do reopen (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Appeal approved after review",
  "note": "Case #701"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Shop reopened successfully",
  "data": {
    "shop_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Appeal approved after review",
    "note": "Case #701",
    "reopened_by": "uuid",
    "reopened_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SHOP_RESTORE` và `SHOP_SUSPEND` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Shop không tồn tại (khi có sync validation với Commerce) |
| 409 | ADMIN-409 | Commerce từ chối transition (consumer) |

## 4. Business Rules

- Admin Service **không** sửa trực tiếp DB Commerce.
- **Không** auto republish products; **không** undo moderation logs cũ.
- Ghi `content_moderation_logs` (`target_type=SHOP`, `action=RESTORE`).
- Critical audit `SHOP_RESTORE`.
- Outbox `SHOP_RESTORED` → topic `admin.shop.restored`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Khi `admin.integrations.commerce.enabled=true`: Admin gọi `POST /commerce/api/v1/internal/moderation/shops/{shopId}/moderate` với `action=RESTORE` **trước** ghi log/outbox.
- Commerce validate transition shop (vd. suspend/close → active).

## 5. Outbox payload

`shop_id`, `moderation_log_id`, `action`, `reason`, `shop_owner_id`, `restored_by`, `restored_at`.

## 6. FE Integration

1. Màn shop moderation → chọn Reopen → nhập `reason` → `POST .../reopen`.
2. Commerce consumer cập nhật trạng thái; response admin không chứa status cuối.
3. Toast success; refresh moderation history khi có API.

## 7. Related

| API | Mục đích |
|-----|----------|
| [SuspendShop](./SuspendShop-api-and-behavior.md) | Suspend shop |
| [CloseShop](./CloseShop-api-and-behavior.md) | Close shop |
| Commerce `POST /commerce/api/v1/admin/shops/{shopId}/moderate` | Commerce-side moderate (`action=RESTORE`) |

## 8. Permission

`SHOP_RESTORE` hoặc `SHOP_SUSPEND`
