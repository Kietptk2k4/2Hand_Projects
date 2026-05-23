# Restore Post – API & Behavior

## 1. Business Goal

Cho phép admin **restore** Social post đã bị moderation hide/remove: ghi moderation log, audit và publish `POST_RESTORED`. Social Service (consumer) validate policy và quyết định trạng thái/visibility cuối; Admin **không** sửa Social DB trực tiếp.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/social/posts/{postId}/restore` | Bearer + `POST_RESTORE` **hoặc** `POST_MODERATE` |

`postId` là MongoDB ObjectId (string 24 hex).

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do restore (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Khang cao duoc chap nhan",
  "note": "Ticket #801"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Post restored successfully",
  "data": {
    "post_id": "507f1f77bcf86cd799439011",
    "moderation_log_id": "uuid",
    "reason": "Khang cao duoc chap nhan",
    "note": "Ticket #801",
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
| 403 | ADMIN-403 | Thiếu `POST_RESTORE` và `POST_MODERATE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Post không tồn tại (khi `admin.integrations.social.enabled=true`) |
| 409 | ADMIN-409 | Social từ chối restore (consumer) |
| 503 | ADMIN-503 | Social không phản hồi (khi integration bật) |

## 4. Business Rules

- Admin Service **không** mutate Social DB.
- Ghi `content_moderation_logs` (`target_type=POST`, `action=RESTORE`).
- Critical audit `POST_RESTORE`.
- Outbox `POST_RESTORED` → topic `admin.post.restored`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Restore không bypass Social content/user policy.

## 5. Outbox payload

`post_id`, `moderation_log_id`, `action`, `reason`, `restored_by`, `restored_at`.

## 6. FE Integration

1. Màn post moderation history → Restore → nhập `reason` → `POST .../restore`.
2. Social consumer cập nhật visibility; response admin không chứa trạng thái cuối.
3. Toast success; refresh history khi có API.

## 7. Related

| API | Mục đích |
|-----|----------|
| [ModeratePost](./ModeratePost-api-and-behavior.md) | Hide/remove post |

## 8. Permission

`POST_RESTORE` hoặc `POST_MODERATE`
