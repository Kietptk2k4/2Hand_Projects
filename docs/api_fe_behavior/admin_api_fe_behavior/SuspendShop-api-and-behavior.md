# Suspend Shop – API & Behavior

## 1. Business Goal

Cho phép admin **suspend** shop seller trên Commerce: đồng bộ HTTP sang Commerce (`action=SUSPEND`), ghi moderation log, audit và publish `SHOP_SUSPENDED` cho Notification. Commerce đặt `seller_shops.status = SUSPENDED` và chặn publish/checkout mới; đơn hiện có vẫn support được.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/shops/{shopId}/suspend` | Bearer + `SHOP_SUSPEND` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do suspend (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Vi pham chinh sach ban hang",
  "note": "Case #501"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Shop suspended successfully",
  "data": {
    "shop_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Vi pham chinh sach ban hang",
    "note": "Case #501",
    "suspended_by": "uuid",
    "suspended_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SHOP_SUSPEND` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Shop không tồn tại (khi có sync validation với Commerce) |

## 4. Business Rules

- Admin Service **không** sửa trực tiếp DB Commerce.
- **Không** hủy đơn đã tạo; không xử lý payout/refund.
- Ghi `content_moderation_logs` (`target_type=SHOP`, `action=SUSPEND`).
- Critical audit `SHOP_SUSPEND`.
- Outbox `SHOP_SUSPENDED` → topic `admin.shop.suspended`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Khi `admin.integrations.commerce.enabled=true`: Admin gọi `POST /commerce/api/v1/internal/moderation/shops/{shopId}/moderate` với `action=SUSPEND` **trước** ghi log/outbox.
- Commerce chặn publish sản phẩm mới và checkout mới khi shop suspended.

## 5. Outbox payload

`shop_id`, `moderation_log_id`, `action`, `reason`, `shop_owner_id`, `suspension_reason`, `suspended_by`, `suspended_at`.

## 6. FE Integration

1. Màn shop moderation → nhập `reason` → `POST .../suspend`.
2. Commerce consumer cập nhật trạng thái shop; response admin không chứa status cuối.
3. Toast success; refresh moderation history khi có API.

## 7. Related

| API | Mục đích |
|-----|----------|
| [CloseShop](./CloseShop-api-and-behavior.md) | Close shop (vĩnh viễn) |
| [ReopenShop](./ReopenShop-api-and-behavior.md) | Reopen shop sau suspend/close |
| Commerce `POST /commerce/api/v1/admin/shops/{shopId}/moderate` | Commerce-side moderate (`action=SUSPEND`) |

## 8. Permission

`SHOP_SUSPEND`
