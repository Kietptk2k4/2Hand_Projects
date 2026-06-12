# Remove Review – API & Behavior

## 1. Business Goal

Cho phép admin **soft remove** review vi phạm nặng: đồng bộ HTTP sang Commerce (`action=HIDE` — map REMOVE → HIDDEN), ghi moderation log, audit và publish `REVIEW_REMOVED` cho Notification. Review vẫn auditable, không xóa vật lý từ Admin Service.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| POST | `/admin/api/v1/reviews/{reviewId}/remove` | Bearer + `REVIEW_REMOVE` **hoặc** `REVIEW_HIDE` |

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do remove (tối đa 4000 ký tự) |
| `note` | string | no | Ghi chú nội bộ admin (tối đa 4000 ký tự) |

```json
{
  "reason": "Vi pham nghiem trong",
  "note": "Escalation #101"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Review removed successfully",
  "data": {
    "review_id": "uuid",
    "moderation_log_id": "uuid",
    "reason": "Vi pham nghiem trong",
    "note": "Escalation #101",
    "removed_by": "uuid",
    "removed_at": "2026-05-23T10:00:00Z",
    "outbox_event_id": "uuid"
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `REVIEW_REMOVE` và `REVIEW_HIDE` |
| 400 | ADMIN-400-VALIDATION | `reason` trống hoặc quá dài |
| 404 | ADMIN-404 | Review không tồn tại (khi có sync validation với Commerce) |

## 4. Business Rules

- Soft remove: Admin Service **không** hard delete review.
- **Không** sửa rating/comment buyer.
- Ghi `content_moderation_logs` (`target_type=REVIEW`, `action=REMOVE`).
- Critical audit `REVIEW_REMOVE`.
- Outbox `REVIEW_REMOVED` → topic `admin.review.removed`.
- `note` chỉ lưu moderation/audit; **không** đưa vào outbox payload.
- Khi `admin.integrations.commerce.enabled=true`: Admin gọi `POST /commerce/api/v1/internal/moderation/reviews/{reviewId}/moderate` với `action=HIDE` **trước** ghi log/outbox.
- Commerce owns trạng thái cuối (`HIDDEN`); Admin REMOVE map sang Commerce HIDE.

## 5. Outbox payload

`review_id`, `moderation_log_id`, `action`, `reason`, `review_author_id`, `seller_user_id`, `removed_by`, `removed_at`.

## 6. FE Integration

1. Màn review moderation → chọn Remove (nặng hơn Hide) → nhập `reason` → `POST .../remove`.
2. Commerce được sync qua HTTP internal moderate; Notification nhận `admin.review.removed` (in-app + push cho author và seller).
3. Toast success; refresh history khi có API.

## 7. Related

| API | Mục đích |
|-----|----------|
| [HideReview](./HideReview-api-and-behavior.md) | Hide review (mức độ nhẹ hơn) |
| [RestoreReview](./RestoreReview-api-and-behavior.md) | Restore review |
| Commerce `POST /commerce/api/v1/admin/reviews/{reviewId}/moderate` | Commerce-side moderate |

## 8. Permission

`REVIEW_REMOVE` hoặc `REVIEW_HIDE`
