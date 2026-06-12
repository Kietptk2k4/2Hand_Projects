# Close Shop – API & Behavior

## 1. Business Goal

Cho phép admin **close** shop seller trên Commerce: đồng bộ HTTP sang Commerce (`action=CLOSE`), ghi moderation log, audit và publish `SHOP_CLOSED` cho Notification. Commerce đặt `seller_shops.status = CLOSED` và chặn hoạt động commerce mới; đơn hiện có vẫn support được.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/shops/{shopId}/close` | Bearer + `SHOP_CLOSE` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do close (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Shop vi pham nghiem trong",
  "note": "Case #602"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Shop closed successfully",
  "data": {
    "shop_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Shop vi pham nghiem trong",
    "note": "Case #602",
    "closed_by": "uuid",
    "closed_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SHOP_CLOSE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Shop không tồn tại (khi có sync validation với Commerce) |

## 4. Business Rules

- Admin Service **không** sửa trực tiếp DB Commerce.
- **Không** auto-cancel đơn; **không** xử lý payout/refund.
- Ghi `content_moderation_logs` (`target_type=SHOP`, `action=CLOSE`).
- Critical audit `SHOP_CLOSE`.
- Outbox `SHOP_CLOSED` → topic `admin.shop.closed`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Khi `admin.integrations.commerce.enabled=true`: Admin gọi `POST /commerce/api/v1/internal/moderation/shops/{shopId}/moderate` với `action=CLOSE` **trước** ghi log/outbox.
- Commerce chặn commerce activity mới khi shop closed.

## 5. Outbox payload

`shop_id`, `moderation_log_id`, `action`, `reason`, `shop_owner_id`, `closed_by`, `closed_at`.

## 6. FE Integration

1. Màn shop moderation → chọn Close → nhập `reason` → `POST .../close`.
2. Commerce consumer cập nhật trạng thái; response admin không chứa status cuối.
3. Toast success; refresh moderation history khi có API.

## 7. Related

| API | Mục đích |
|-----|----------|
| [SuspendShop](./SuspendShop-api-and-behavior.md) | Suspend shop (tạm thời) |
| [ReopenShop](./ReopenShop-api-and-behavior.md) | Reopen shop sau suspend/close |
| Commerce `POST /commerce/api/v1/admin/shops/{shopId}/moderate` | Commerce-side moderate (`action=CLOSE`) |

## 8. Permission

`SHOP_CLOSE`
