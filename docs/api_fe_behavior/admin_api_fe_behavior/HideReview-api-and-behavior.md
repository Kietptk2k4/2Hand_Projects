# Hide Review – API & Behavior

## 1. Business Goal

Cho phép admin **hide** review vi phạm: ghi moderation log, audit và publish `REVIEW_HIDDEN`. Commerce Service (consumer) đặt `reviews.status = HIDDEN` và tính lại rating summary nếu cần.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/reviews/{reviewId}/hide` | Bearer + `REVIEW_HIDE` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do hide (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Noi dung spam",
  "note": "Report #789"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Review hidden successfully",
  "data": {
    "review_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Noi dung spam",
    "note": "Report #789",
    "hidden_by": "uuid",
    "hidden_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `REVIEW_HIDE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Review không tồn tại (khi có sync validation với Commerce) |

## 4. Business Rules

- Admin Service **không** gọi trực tiếp Commerce moderate API; **không** sửa rating/comment.
- Review vẫn lưu trong DB (soft hide).
- Ghi `content_moderation_logs` (`target_type=REVIEW`, `action=HIDE`).
- Critical audit `REVIEW_HIDE`.
- Outbox `REVIEW_HIDDEN` → topic `admin.review.hidden`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.

## 5. Outbox payload

`review_id`, `moderation_log_id`, `action`, `reason`, `hidden_by`, `hidden_at`.

## 6. FE Integration

1. Màn review moderation → nhập `reason` → `POST .../hide`.
2. Ẩn review khỏi public list sau khi Commerce consumer xử lý event.
3. Toast success; refresh moderation history khi có API.

## 7. Related

| API | Mục đích |
|-----|----------|
| Commerce `POST /commerce/api/v1/admin/reviews/{reviewId}/moderate` | Commerce-side moderate (`action=HIDE`) |

## 8. Permission

`REVIEW_HIDE`
