# Pin System Announcement Notification – Internal & Behavior

## 1. Business Goal

Lưu trạng thái **pinned** từ Admin announcement vào `user_notifications.metadata` để client hiển thị announcement nổi bật. Notification Service **không** quyết định pin policy và **không** xử lý Admin pin/unpin API.

## 2. Trigger

- Fan-out `SYSTEM_ANNOUNCEMENT_SENT` khi payload có `is_pinned` (hoặc alias `pinned` được normalize lúc ingest).

## 3. Flow

1. **Ingest:** `AdminSystemAnnouncementPayloadNormalizer` map `pinned` → `is_pinned`, loại field nội bộ Admin.
2. **Parse:** `SystemAnnouncementPinnedMetadataPolicy.resolveIsPinned` — thiếu flag → `false`; shape không hợp lệ → fail fan-out (permanent).
3. **Metadata:** `SystemAnnouncementNotificationMetadataPolicy.build` ghi `metadata.is_pinned`, `severity` (chuẩn hóa `INFO|WARNING|CRITICAL`), `dismissible`.
4. **Read API:** `UserNotificationMetadataPresenter` đảm bảo response có `is_pinned` boolean cho `reference_type=SYSTEM_ANNOUNCEMENT`.

## 4. Payload (producer)

```json
{
  "announcement_id": "<uuid>",
  "title": "...",
  "content": "...",
  "severity": "WARNING",
  "is_pinned": true,
  "dismissible": true,
  "recipient_user_ids": ["<user-uuid>"]
}
```

Alias ingest: `"pinned": true` → `is_pinned`.

## 5. Business Rules

| Rule | Behavior |
|------|----------|
| Pin từ Admin | Chỉ đọc từ payload, không suy luận runtime |
| Default | `is_pinned = false` khi thiếu |
| Dismissible độc lập | Pinned vẫn dismiss được nếu `dismissible=true` |
| Sort UI | Thuộc FE (out of scope server sort) |

## 6. Metadata shape

```json
{
  "announcement_id": "<uuid>",
  "severity": "CRITICAL",
  "is_pinned": true,
  "dismissible": false
}
```

## 7. Failure Cases

| Case | Outcome |
|------|---------|
| `is_pinned` / `dismissible` không phải boolean | Event `FAILED` permanent |
| `severity` không hợp lệ | Event `FAILED` permanent |
| Metadata corrupt khi đọc | Sanitize → `is_pinned: false` |

## 8. Related FR

- `FR_FanOutSystemAnnouncement` — tạo notification records.
- `FR_DismissAnnouncementNotification` — dismiss khi `dismissible=true`.
- Admin `FR_PinSystemAnnouncement` — thay đổi pin trên Admin DB (không sync ngược notification đã fan-out).

## 9. FE / Client

- Đọc `metadata.is_pinned === true` để section pinned.
- Sort/order pinned items theo UX (spec FE behavior sau).
- Deep link: `reference_type=SYSTEM_ANNOUNCEMENT`, `reference_id={announcement_id}`.
