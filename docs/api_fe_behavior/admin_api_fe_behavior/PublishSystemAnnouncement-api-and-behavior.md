# Publish System Announcement – API & Behavior

## 1. Business Goal

Cho phép admin **publish** announcement `DRAFT` → `SENT`, ghi `sent_at` và enqueue outbox event để Notification Service fan-out.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/system-announcements/{announcementId}/publish` | Bearer + `SYSTEM_ANNOUNCEMENT_PUBLISH` |

Không có request body.

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "System announcement published successfully",
  "data": {
    "announcement_id": "uuid",
    "title": "Checkout update",
    "severity": "INFO",
    "status": "SENT",
    "is_pinned": false,
    "dismissible": true,
    "sent_at": "2026-05-23T12:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_ANNOUNCEMENT_PUBLISH` |
| 404 | ADMIN-404 | Announcement không tồn tại |
| 409 | ADMIN-409-ANNOUNCEMENT | Không phải `DRAFT` (đã publish / cancelled) |

## 4. Business Rules

- Chỉ announcement **`DRAFT`** mới publish được.
- Publish set `status = SENT`, `sent_at = now()`.
- Ghi `admin_action_logs` critical (`SYSTEM_ANNOUNCEMENT_PUBLISH`).
- Outbox `SYSTEM_ANNOUNCEMENT_PUBLISHED` → topic `admin.announcement.published` (cùng transaction).
- Payload gồm: `announcement_id`, `title`, `content`, `severity`, `is_pinned`, `dismissible`, `status`, `sent_at`, `created_by`.

## 5. FE Integration

1. Nút Publish trên màn draft → confirm → `POST .../system-announcements/{id}/publish`.
2. Sau 200, cập nhật UI status `SENT`, hiển thị `sent_at`.
3. Xử lý 409: thông báo đã publish / không còn draft.

## 6. Related

| API | Mục đích |
|-----|----------|
| `POST .../system-announcements` | Tạo draft |
| PublishAdminEvents (worker) | Đẩy outbox lên broker |

## 7. Permission

`SYSTEM_ANNOUNCEMENT_PUBLISH` — tách khỏi `SYSTEM_ANNOUNCEMENT_CREATE`.
