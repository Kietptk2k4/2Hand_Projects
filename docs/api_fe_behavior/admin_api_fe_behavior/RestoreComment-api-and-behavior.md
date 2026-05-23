# Restore Comment – API & Behavior

## 1. Business Goal

Cho phép admin **restore** Social comment đã bị moderation hide/remove: ghi moderation log, audit và publish `COMMENT_RESTORED`. Social Service (consumer) validate policy và quyết định trạng thái/counter cuối; Admin **không** sửa Social DB trực tiếp.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/social/comments/{commentId}/restore` | Bearer + `COMMENT_RESTORE` **hoặc** `COMMENT_MODERATE` |

`commentId` là MongoDB ObjectId (string 24 hex).

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do restore (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Khang cao duoc chap nhan",
  "note": "Ticket #902"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Comment restored successfully",
  "data": {
    "comment_id": "507f1f77bcf86cd799439012",
    "moderation_log_id": "uuid",
    "reason": "Khang cao duoc chap nhan",
    "note": "Ticket #902",
    "restored_by": "uuid",
    "restored_at": "2026-05-23T11:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `COMMENT_RESTORE` và `COMMENT_MODERATE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Comment không tồn tại (khi `admin.integrations.social.enabled=true`) |
| 409 | ADMIN-409 | Social từ chối restore (consumer) |
| 503 | ADMIN-503 | Social không phản hồi (khi integration bật) |

## 4. Business Rules

- Admin Service **không** mutate Social DB.
- **Không** auto restore parent post.
- Ghi `content_moderation_logs` (`target_type=COMMENT`, `action=RESTORE`).
- Critical audit `COMMENT_RESTORE`.
- Outbox `COMMENT_RESTORED` → topic `admin.comment.restored`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Restore không bypass Social policy.

## 5. Outbox payload

`comment_id`, `moderation_log_id`, `action`, `reason`, `restored_by`, `restored_at`.

## 6. FE Integration

1. Màn comment moderation history → Restore → nhập `reason` → `POST .../restore`.
2. Social consumer cập nhật visibility/counter; response admin không chứa trạng thái cuối.
3. Toast success; refresh history khi có API.

## 7. Related

| API | Mục đích |
|-----|----------|
| [ModerateComment](./ModerateComment-api-and-behavior.md) | Hide/remove comment |

## 8. Permission

`COMMENT_RESTORE` hoặc `COMMENT_MODERATE`
