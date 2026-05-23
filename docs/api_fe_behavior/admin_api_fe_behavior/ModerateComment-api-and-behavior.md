# Moderate Comment – API & Behavior

## 1. Business Goal

Cho phép admin **hide** hoặc **remove** Social comment vi phạm: ghi moderation log, audit và publish `COMMENT_MODERATED`. Social Service (consumer) own trạng thái comment và side effects (counter/thread); Admin **không** sửa Social DB trực tiếp.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/social/comments/{commentId}/moderate` | Bearer + `COMMENT_MODERATE` |

`commentId` là MongoDB ObjectId (string 24 hex), không phải UUID.

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `action` | string | yes | `HIDE` hoặc `REMOVE` |
| `reason` | string | yes | Lý do moderation (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "action": "REMOVE",
  "reason": "Noi dung vi pham chinh sach",
  "note": "Report #2002"
}
```

**Success (200) – action HIDE:**

```json
{
  "code": 200,
  "success": true,
  "message": "Comment hidden successfully",
  "data": {
    "comment_id": "507f1f77bcf86cd799439012",
    "action": "HIDE",
    "moderation_log_id": "uuid",
    "reason": "Noi dung vi pham chinh sach",
    "note": "Report #2002",
    "moderated_by": "uuid",
    "moderated_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

Message khi `action=REMOVE`: `"Comment removed successfully"`.

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `COMMENT_MODERATE` |
| 400 | ADMIN-400-VALIDATION | `action` không hợp lệ, `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Comment không tồn tại (khi `admin.integrations.social.enabled=true`) |
| 503 | ADMIN-503 | Social không phản hồi (khi integration bật) |

## 4. Business Rules

- Admin Service **không** mutate Social DB.
- Ghi `content_moderation_logs` (`target_type=COMMENT`, `action=HIDE|REMOVE`).
- Critical audit `COMMENT_MODERATE`.
- Outbox `COMMENT_MODERATED` → topic `admin.comment.moderated`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Social consumer áp dụng hide/remove và cập nhật counter/thread theo policy.

## 5. Outbox payload

`comment_id`, `moderation_log_id`, `action`, `reason`, `moderated_by`, `moderated_at`.

## 6. FE Integration

1. Màn comment moderation → chọn Hide/Remove → nhập `reason` → `POST .../moderate`.
2. Toast theo `message`; refresh moderation history khi có API history.
3. Không chờ Social đồng bộ trong response admin.

## 7. Related

| API | Mục đích |
|-----|----------|
| [ModeratePost](./ModeratePost-api-and-behavior.md) | Moderate post |
| FR_RestoreComment (tương lai) | Restore comment sau moderation |

## 8. Permission

`COMMENT_MODERATE`
