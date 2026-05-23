# Cancel System Announcement – API & Behavior

## 1. Business Goal

Cho phép admin **hủy** announcement `DRAFT` hoặc `SENT` để không còn hiển thị active; notify consumer khi đã từng publish.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/system-announcements/{announcementId}/cancel` | Bearer + `SYSTEM_ANNOUNCEMENT_CANCEL` |

Không có request body.

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "System announcement cancelled successfully",
  "data": {
    "announcement_id": "uuid",
    "title": "Shipping delay",
    "status": "CANCELLED",
    "state_changed": true,
    "outbox_event_id": "uuid"
  }
}
```

- `outbox_event_id` = `null` khi cancel **DRAFT** (chưa publish).
- `state_changed: false` + message idempotent nếu đã `CANCELLED`.

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_ANNOUNCEMENT_CANCEL` |
| 404 | ADMIN-404 | Announcement không tồn tại |

## 4. Business Rules

- `DRAFT` hoặc `SENT` → `CANCELLED`.
- Đã `CANCELLED` → idempotent **200** (không ghi lại).
- Cancel **SENT** → outbox `SYSTEM_ANNOUNCEMENT_CANCELLED` → `admin.announcement.cancelled`.
- Cancel **DRAFT** → chỉ DB + audit (không outbox).
- Critical audit `SYSTEM_ANNOUNCEMENT_CANCEL`.

## 5. Outbox payload (SENT cancel)

`announcement_id`, `title`, `content`, `severity`, `is_pinned`, `dismissible`, `status`, `sent_at`, `previous_status`, `cancelled_at`, `created_by`.

## 6. FE Integration

1. Nút Cancel trên draft/sent detail → confirm → `POST .../cancel`.
2. Ẩn announcement khỏi list active sau 200.
3. Idempotent: gọi lại khi đã cancelled vẫn 200.

## 7. Related

| API | Mục đích |
|-----|----------|
| [CreateSystemAnnouncement](./CreateSystemAnnouncement-api-and-behavior.md) | Tạo draft |
| [PublishSystemAnnouncement](./PublishSystemAnnouncement-api-and-behavior.md) | Publish |

## 8. Permission

`SYSTEM_ANNOUNCEMENT_CANCEL`
