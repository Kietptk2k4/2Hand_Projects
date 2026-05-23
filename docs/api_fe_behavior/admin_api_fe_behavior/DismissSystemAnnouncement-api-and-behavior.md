# Dismiss System Announcement – API & Behavior

## 1. Business Goal

Cho phép client **dismiss** announcement khi `dismissible = true`. MVP Admin **không** lưu per-user dismissal — API xác thực policy; client (hoặc Notification Service) lưu trạng thái ẩn cục bộ.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/system-announcements/{announcementId}/dismiss` | Bearer (authenticated) |

Không có request body.

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "System announcement can be dismissed",
  "data": {
    "announcement_id": "uuid",
    "title": "Promo",
    "status": "SENT",
    "dismissible": true,
    "client_side_persistence": true
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 404 | ADMIN-404 | Announcement không tồn tại |
| 409 | ADMIN-409-ANNOUNCEMENT | `dismissible = false`, status không phải `SENT`, hoặc đã `CANCELLED` |

## 4. Business Rules

- Chỉ announcement **`dismissible = true`** mới dismiss được.
- Chỉ announcement **`SENT`** (đang active) mới dismiss được.
- **`DRAFT` / `CANCELLED`** → 409.
- **Không** ghi DB Admin (MVP); `client_side_persistence: true` báo FE lưu local / gọi Notification dismiss.
- Cấu hình `dismissible` lúc tạo hoặc qua `SYSTEM_ANNOUNCEMENT_UPDATE` (PATCH pin flow tương tự — field trong create body).

## 5. FE Integration

1. User bấm dismiss trên banner → `POST .../dismiss` (hoặc dismiss notification qua Notification API).
2. Nếu 200 + `client_side_persistence`, lưu `announcement_id` vào local storage / gọi `POST /notification/.../dismiss`.
3. Nếu 409 + not dismissible, ẩn nút dismiss trên UI.

## 6. Related

| API / FR | Mục đích |
|----------|----------|
| [CreateSystemAnnouncement](./CreateSystemAnnouncement-api-and-behavior.md) | Set `dismissible` lúc tạo |
| FR_DismissAnnouncementNotification (Notification) | Persist per-user dismiss |

## 7. Permission

Chỉ cần **authenticated** JWT trên Admin API (không permission riêng). End-user dismiss production nên qua Notification Service.
