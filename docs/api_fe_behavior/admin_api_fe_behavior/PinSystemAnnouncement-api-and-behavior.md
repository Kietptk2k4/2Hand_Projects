# Pin System Announcement – API & Behavior

## 1. Business Goal

Cho phép admin **pin / unpin** announcement để hiển thị nổi bật trên client hoặc admin portal.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| PATCH | `/admin/api/v1/system-announcements/{announcementId}/pin` | Bearer + `SYSTEM_ANNOUNCEMENT_UPDATE` |

**Request body:**

| Field | Required | Mô tả |
|-------|----------|--------|
| `is_pinned` | Yes | `true` = pin, `false` = unpin |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "System announcement pin updated successfully",
  "data": {
    "announcement_id": "uuid",
    "title": "Flash sale",
    "status": "DRAFT",
    "is_pinned": true,
    "state_changed": true
  }
}
```

- `state_changed: false` + message idempotent khi `is_pinned` đã đúng giá trị yêu cầu.

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SYSTEM_ANNOUNCEMENT_UPDATE` |
| 404 | ADMIN-404 | Announcement không tồn tại |
| 409 | ADMIN-409-ANNOUNCEMENT | Status `CANCELLED` — không cho pin |
| 400 | ADMIN-400-VALIDATION | Thiếu `is_pinned` |

## 4. Business Rules

- Announcement phải tồn tại.
- `DRAFT` và `SENT` có thể pin/unpin.
- `CANCELLED` → **409**.
- Không publish outbox ở bước pin (chỉ cập nhật DB).
- Ghi `admin_action_logs` (`SYSTEM_ANNOUNCEMENT_PIN`) khi có thay đổi.

## 5. FE Integration

1. Toggle pin trên list/detail → `PATCH .../pin` body `{ "is_pinned": true }`.
2. Cập nhật icon/badge theo `is_pinned` trong response.
3. Không gọi lại nếu user bật pin khi đã pinned (optional — API idempotent).

## 6. Related

| API | Mục đích |
|-----|----------|
| `POST .../system-announcements` | Tạo (set pin lúc create) |
| `POST .../system-announcements/{id}/publish` | Publish |

## 7. Permission

`SYSTEM_ANNOUNCEMENT_UPDATE` — dùng chung cho các thao tác cập nhật metadata announcement (pin, dismissible, …).
