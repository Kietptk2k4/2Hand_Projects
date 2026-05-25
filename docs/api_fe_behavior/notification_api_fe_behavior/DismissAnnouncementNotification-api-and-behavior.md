# Dismiss Announcement Notification – API & Behavior

## 1. Business Goal

Cho phép user ẩn (soft delete) notification announcement **chỉ khi** announcement được Admin đánh dấu `dismissible`. Không thay đổi trạng thái announcement trên Admin Service.

## 2. API

| Method | Path | Auth |
|--------|------|------|
| `POST` | `/api/v1/notification/notifications/{notificationId}/dismiss` | Bearer JWT |

### Success (200)

```json
{
  "code": 200,
  "success": true,
  "message": "Announcement notification dismissed successfully",
  "data": {
    "notificationId": "<uuid>",
    "dismissed": true,
    "alreadyDismissed": false
  }
}
```

### Errors

| HTTP | Ý nghĩa |
|------|---------|
| 401 | Thiếu / JWT không hợp lệ |
| 404 | Notification không tồn tại hoặc không thuộc user |
| 400 | `reference_type` ≠ `SYSTEM_ANNOUNCEMENT` |
| 409 | `metadata.dismissible` ≠ `true` |

## 3. Business Rules

- Chỉ user sở hữu notification mới dismiss được.
- `reference_type` phải là `SYSTEM_ANNOUNCEMENT`.
- Đọc `metadata.dismissible` (boolean); thiếu hoặc `false` → 409.
- Dismiss = `user_notifications.is_deleted = true` (cùng cơ chế soft delete với `DELETE /notifications/{id}`).
- Announcement không dismissible vẫn hiển thị trong list.
- Dismiss lần hai → `alreadyDismissed: true` (idempotent).
- User khác không bị ảnh hưởng.

## 4. FE / Client

- Chỉ hiện nút dismiss khi `reference_type === "SYSTEM_ANNOUNCEMENT"` và `metadata.dismissible === true`.
- Sau dismiss, notification biến mất khỏi `GET /notifications` và unread count.
- Không gọi dismiss cho notification social/commerce thường — dùng `DELETE` nếu cần xóa chung.

## 5. Related

- `FR_FanOutSystemAnnouncement` — ghi `metadata.dismissible` lúc fan-out.
- `FR_PinSystemAnnouncementNotification` — `is_pinned` độc lập với dismiss.
- Admin `FR_DismissSystemAnnouncement` — cấu hình dismissible trên announcement (khác API user dismiss).
