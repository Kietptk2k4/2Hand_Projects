# View User Notifications – API & Behavior

## 1. Business Goal

Cho phép user đã đăng nhập xem danh sách in-app notifications của chính mình, phân trang và sắp xếp mới nhất trước.

## 2. API Contract

- **Method:** `GET`
- **URL:** `/api/v1/notification/notifications`
- **Auth:** Bearer JWT (user access token)
- **Query params:**

| Param | Type | Required | Default | Validation |
|-------|------|----------|---------|------------|
| `page` | int | No | `0` | `>= 0` |
| `size` | int | No | `20` | `1..50` |

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": {
    "items": [
      {
        "id": "11111111-1111-1111-1111-111111111111",
        "actorId": "22222222-2222-2222-2222-222222222222",
        "type": "POST_LIKED",
        "title": "New like",
        "content": "Someone liked your post.",
        "referenceType": "POST",
        "referenceId": "post-1",
        "metadata": "{\"postId\":\"post-1\"}",
        "read": false,
        "readAt": null,
        "createdAt": "2026-05-24T12:00:00Z"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1,
      "hasNext": false
    }
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 400 | 400 | Pagination không hợp lệ (`page < 0`, `size` ngoài `1..50`) |
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống |

## 5. Business Rules

- `user_id` lấy từ JWT subject — không nhận `user_id` từ client.
- Chỉ trả về notification có `user_id = current_user_id` và `is_deleted = false`.
- Sort `created_at DESC`.
- `metadata` được sanitize lại trước khi trả về client (redact token/secret).
- Không expose notification của user khác.

## 6. Edge Cases

- User chưa có notification → `items: []`, `totalElements: 0`.
- `page` vượt quá số trang → `items: []`, meta vẫn phản ánh tổng số bản ghi.
- Notification đã soft-delete → không xuất hiện trong list.
- Metadata chứa field nhạy cảm → redacted trong response.

## 7. Data Dependencies

- Table: `user_notifications`
- Index hỗ trợ: `idx_user_notifications_user_created`, `idx_user_notifications_user_unread`

## 8. FE Integration Notes

- Dùng `page`/`size` cho infinite scroll hoặc phân trang cổ điển.
- Badge unread: gọi riêng `GET /notifications/unread-count` (FR tiếp theo).
- Deep link: dùng `referenceType`, `referenceId`, `metadata`; vẫn phải authorize resource ở service sở hữu.
- 401 → refresh token flow của Auth service.
