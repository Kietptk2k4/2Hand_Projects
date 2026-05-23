# Restore Review – API & Behavior

## 1. Business Goal

Cho phép admin **restore** review đã bị hide/remove: ghi moderation log, audit và publish `REVIEW_RESTORED`. Commerce Service (consumer) validate eligibility, đặt trạng thái cuối và tính lại rating summary nếu cần.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/reviews/{reviewId}/restore` | Bearer + `REVIEW_RESTORE` **hoặc** `REVIEW_HIDE` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do restore (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Bao cao sai",
  "note": "Ticket #202"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Review restored successfully",
  "data": {
    "review_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Bao cao sai",
    "note": "Ticket #202",
    "restored_by": "uuid",
    "restored_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `REVIEW_RESTORE` và `REVIEW_HIDE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Review không tồn tại (khi có sync validation với Commerce) |
| 409 | ADMIN-409-MODERATION | Commerce từ chối restore (khi có sync validation) |

## 4. Business Rules

- Admin Service **không** sửa nội dung review và **không** tính rating trong Admin DB.
- Ghi `content_moderation_logs` (`target_type=REVIEW`, `action=RESTORE`).
- Critical audit `REVIEW_RESTORE`.
- Outbox `REVIEW_RESTORED` → topic `admin.review.restored`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Commerce owns trạng thái cuối (`VISIBLE`, …) và rating effects.

## 5. Outbox payload

`review_id`, `moderation_log_id`, `action`, `reason`, `restored_by`, `restored_at`.

## 6. FE Integration

1. Màn review moderation history → Restore → nhập `reason` → `POST .../restore`.
2. Commerce consumer xử lý event; không hiển thị status cuối trong response admin.
3. Toast success; refresh list/history.

## 7. Related

| API | Mục đích |
|-----|----------|
| [HideReview](./HideReview-api-and-behavior.md) | Hide review |
| [RemoveReview](./RemoveReview-api-and-behavior.md) | Soft remove review |
| Commerce `POST /commerce/api/v1/admin/reviews/{reviewId}/moderate` | Commerce-side moderate (`action=RESTORE`) |

## 8. Permission

`REVIEW_RESTORE` hoặc `REVIEW_HIDE`
