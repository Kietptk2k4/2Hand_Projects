# Fan Out System Announcement – Internal & Behavior

## 1. Business Goal

Fan-out thông báo in-app (và push khi `severity=CRITICAL`) cho danh sách user khi Admin publish announcement. Notification Service không sở hữu trạng thái announcement trên Admin.

## 2. Trigger

- Kafka: `admin.announcement.published`
- Internal ingest: `eventType` = `SYSTEM_ANNOUNCEMENT_SENT` (alias từ `SYSTEM_ANNOUNCEMENT_PUBLISHED`)

## 3. Flow

1. **Ingest:** `AdminSystemAnnouncementPayloadNormalizer` map `id` → `announcement_id`, loại `created_by`, `status`.
2. **Worker:** `SystemAnnouncementFanOutNotificationEventHandler` (`@Order(30)`).
3. **Recipients:** `recipient_user_ids` trong payload, hoặc `target_audience` qua `SystemAnnouncementAudienceUserProvider` (MVP: provider chưa nối Auth → retryable khi `ALL_USERS` không resolve được user).
4. **Fan-out:** `FanOutSystemAnnouncementUseCase` tạo `user_notifications` theo batch (`notification.system-announcement.fan-out-batch-size`).
5. **Reference:** `SYSTEM_ANNOUNCEMENT/{announcement_id}`.
6. **Metadata:** `announcement_id`, `severity`, `is_pinned`, `dismissible` (chi tiết pin: `PinSystemAnnouncementNotification-internal-and-behavior.md`).

## 4. Admin Payload (producer)

```json
{
  "announcement_id": "<uuid>",
  "title": "Platform update",
  "content": "New features are live.",
  "severity": "INFO",
  "is_pinned": false,
  "dismissible": true,
  "recipient_user_ids": ["<user-uuid>", "<user-uuid>"]
}
```

Hoặc:

```json
{
  "target_audience": "ALL_USERS"
}
```

- MVP broadcast: Admin (hoặc bước publish mở rộng) nên gửi `recipient_user_ids` cho đến khi Auth paging được implement.
- Push chỉ gửi khi `severity` = `CRITICAL` và user bật push + có device token.

## 5. Outcomes

| Outcome | Ý nghĩa |
|---------|---------|
| `COMPLETED` | Ít nhất một recipient nhận in-app hoặc push |
| `FAILED` + `PERMANENT` | Thiếu `announcement_id`, title/content/severity, không có recipients/audience |
| `FAILED` + `RETRYABLE` | Lỗi DB; audience provider chưa cấu hình cho `ALL_USERS` |
| `NO_OP` | Không recipient nào deliver được channel |

`NotificationCriticalOverridePolicy` bắt buộc in-app (`allow_in_app=false` vẫn tạo notification).

## 6. Idempotency

Unique theo `(notification_event_id, user_id, type=SYSTEM_ANNOUNCEMENT_SENT, reference_type=SYSTEM_ANNOUNCEMENT, reference_id=announcement_id)`.

## 7. Related FR

- `FR_DismissAnnouncementNotification` — dismiss khi `metadata.dismissible=true`.
- Generic `PushNotificationEventHandler` **loại trừ** `SYSTEM_ANNOUNCEMENT_SENT`.

## 8. FE / Client

- Hiển thị `title` / `content` từ notification record (không dùng template tĩnh).
- Deep link: `reference_type=SYSTEM_ANNOUNCEMENT`, `reference_id={announcement_id}`.
- Pin/dismiss: đọc `metadata.is_pinned`, `metadata.dismissible`.
