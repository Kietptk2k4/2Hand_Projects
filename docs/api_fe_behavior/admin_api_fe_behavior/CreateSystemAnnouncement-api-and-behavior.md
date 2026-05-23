# Create System Announcement – API & Behavior

## 1. Business Goal

Cho phép admin tạo **announcement toàn hệ thống** ở trạng thái `DRAFT` (chưa gửi tới user) để chỉnh sửa / publish sau.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/system-announcements` | Bearer + `SYSTEM_ANNOUNCEMENT_CREATE` |

**Request body:**

| Field | Required | Mô tả |
|-------|----------|--------|
| `title` | Yes | Tiêu đề (max 500 ký tự) |
| `content` | Yes | Nội dung announcement |
| `severity` | Yes | `INFO`, `WARNING`, `CRITICAL` |
| `is_pinned` | No | Default `false` |
| `dismissible` | No | Default `true` |

**Success (201):**

```json
{
  "code": 201,
  "success": true,
  "message": "System announcement created successfully",
  "data": {
    "announcement_id": "uuid",
    "title": "Platform maintenance",
    "content": "Scheduled downtime tonight.",
    "severity": "WARNING",
    "is_pinned": false,
    "dismissible": true,
    "status": "DRAFT",
    "created_by": "uuid",
    "created_at": "2026-05-23T10:00:00Z"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_ANNOUNCEMENT_CREATE` |
| 400 | ADMIN-400-VALIDATION | Title/content/severity không hợp lệ |

## 4. Business Rules

- `title` và `content` bắt buộc (sau trim).
- Trạng thái ban đầu luôn là **`DRAFT`**.
- `created_by` = admin id từ JWT.
- `sent_at` = `null` cho đến khi publish.
- **Không** publish event / fan-out ở bước tạo.
- Ghi `admin_action_logs` (`SYSTEM_ANNOUNCEMENT_CREATE`, target `ANNOUNCEMENT`).

## 5. FE Integration

1. Form tạo announcement → `POST .../system-announcements`.
2. Sau 201, điều hướng tới màn detail draft hoặc list drafts.
3. Publish riêng qua `POST .../system-announcements/{id}/publish` (FR_PublishSystemAnnouncement).

## 6. Related

| API | Mục đích |
|-----|----------|
| `POST .../system-announcements/{id}/publish` | Gửi announcement (`SENT`, outbox) |

## 7. Permission

`SYSTEM_ANNOUNCEMENT_CREATE` — tách khỏi `SYSTEM_ANNOUNCEMENT_PUBLISH`.
