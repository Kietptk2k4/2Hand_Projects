# Moderate Post – API & Behavior

## 1. Business Goal

Cho phép admin **hide** hoặc **remove** Social post vi phạm: ghi moderation log, audit và publish `POST_MODERATED`. Social Service (consumer) own trạng thái post và feed visibility; Admin **không** sửa Social DB trực tiếp.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/social/posts/{postId}/moderate` | Bearer + `POST_MODERATE` |

`postId` là MongoDB ObjectId (string 24 hex), không phải UUID.

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `action` | string | yes | `HIDE` hoặc `REMOVE` |
| `reason` | string | yes | Lý do moderation (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "action": "HIDE",
  "reason": "Noi dung vi pham chinh sach",
  "note": "Report #1001"
}
```

**Success (200) – action HIDE:**

```json
{
  "code": 200,
  "success": true,
  "message": "Post hidden successfully",
  "data": {
    "post_id": "507f1f77bcf86cd799439011",
    "action": "HIDE",
    "moderation_log_id": "uuid",
    "reason": "Noi dung vi pham chinh sach",
    "note": "Report #1001",
    "moderated_by": "uuid",
    "moderated_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

Message khi `action=REMOVE`: `"Post removed successfully"`.

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `POST_MODERATE` |
| 400 | ADMIN-400-VALIDATION | `action` không hợp lệ, `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Post không tồn tại (khi `admin.integrations.social.enabled=true`) |
| 503 | ADMIN-503 | Social không phản hồi (khi integration bật) |

## 4. Business Rules

- Admin Service **không** mutate Social DB.
- Ghi `content_moderation_logs` (`target_type=POST`, `action=HIDE|REMOVE`).
- Critical audit `POST_MODERATE`.
- Outbox `POST_MODERATED` → topic `admin.post.moderated`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Social consumer áp dụng hide/remove theo policy feed/search/profile.

## 5. Outbox payload

`post_id`, `moderation_log_id`, `action`, `reason`, `moderated_by`, `moderated_at`.

Khi `admin.integrations.social.enabled=true`, payload them `author_user_id` (UUID tac gia post) de Notification Service deliver in-app/push.

## 6. FE Integration

1. Màn post moderation → chọn Hide/Remove → nhập `reason` → `POST .../moderate`.
2. Toast theo `message`; refresh moderation history khi có API history.
3. Không chờ Social đồng bộ trong response admin.

## 7. Related

| API | Mục đích |
|-----|----------|
| [ModerateComment](./ModerateComment-api-and-behavior.md) | Moderate comment |
| [RestorePost](./RestorePost-api-and-behavior.md) | Restore post sau moderation |

## 8. Permission

`POST_MODERATE`
