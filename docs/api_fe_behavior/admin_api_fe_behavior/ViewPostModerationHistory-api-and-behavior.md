# View Post Moderation History – API & Behavior

## 1. Business Goal

Cho phép admin xem **lịch sử kiểm duyệt** của một bài viết (ẩn/gỡ/khôi phục): action, reason, note, admin id và timestamp. Chỉ đọc `content_moderation_logs` trong Admin Service; không gọi Social Service.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/social/posts/{postId}/moderation-history` | Bearer + `POST_MODERATION_READ` |

### Query params

| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `page` | int | no | Trang (mặc định `1`, tối thiểu `1`) |
| `size` | int | no | Kích thước trang (mặc định `20`, tối đa `100`) |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Post moderation history retrieved successfully",
  "data": {
    "post_id": "674a10000000000000000001",
    "page": 1,
    "size": 20,
    "total_elements": 2,
    "total_pages": 1,
    "history": [
      {
        "moderation_log_id": "uuid",
        "action": "RESTORE",
        "reason": "Appeal approved",
        "note": "Restore note",
        "admin_id": "uuid",
        "created_at": "2026-05-23T11:00:00Z"
      },
      {
        "moderation_log_id": "uuid",
        "action": "HIDE",
        "reason": "Policy violation",
        "note": null,
        "admin_id": "uuid",
        "created_at": "2026-05-23T10:00:00Z"
      }
    ]
  }
}
```

## 3. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `POST_MODERATION_READ` |
| 400 | ADMIN-400-PAGINATION | `page` / `size` không hợp lệ |

## 4. Business Rules

- Filter `content_moderation_logs` với `target_type = POST` và `target_id = {postId}` (MongoDB ObjectId string).
- Sắp xếp `created_at DESC` (mới nhất trước).
- Post chưa có log → `history: []`, `total_elements: 0` (không 404).
- Read-only; không gọi Social; không ghi audit/outbox.

## 5. FE Integration

1. Drawer chi tiết bài viết → `GET .../moderation-history?page=1&size=20`.
2. Hiển thị timeline theo `history`; badge theo `action` (`HIDE`, `REMOVE`, `RESTORE`).
3. Refresh history sau khi moderate/restore thành công.

## 6. Related

| API | Mục đích |
|-----|----------|
| [ModeratePost](./ModeratePost-api-and-behavior.md) | Kiểm duyệt bài viết |
| [RestorePost](./RestorePost-api-and-behavior.md) | Khôi phục bài viết |
