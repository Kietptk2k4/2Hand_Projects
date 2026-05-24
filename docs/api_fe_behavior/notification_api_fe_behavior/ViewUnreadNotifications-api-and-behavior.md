# View Unread Notifications – API & Behavior

## 1. Business Goal

Cho phép user đã đăng nhập xem danh sách in-app notifications **chưa đọc**, phân trang và sắp xếp mới nhất trước.

## 2. API Contract

- **Method:** `GET`
- **URL:** `/api/v1/notification/notifications/unread`
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
  "message": "Unread notifications retrieved successfully",
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
        "metadata": "{}",
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
| 400 | 400 | Pagination không hợp lệ |
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống |

## 5. Business Rules

- `user_id` lấy từ JWT subject.
- Chỉ trả về notification có:
  - `user_id = current_user_id`
  - `is_read = false`
  - `is_deleted = false`
- Sort `created_at DESC`.
- `metadata` được sanitize trước khi trả về client.

## 6. Edge Cases

- Không có unread → `items: []`, `totalElements: 0`.
- Notification đã read hoặc deleted → không xuất hiện.
- Notification của user khác → không xuất hiện.

## 7. Data Dependencies

- Table: `user_notifications`
- Index hỗ trợ: `idx_user_notifications_user_unread`

## 8. FE Integration Notes

- Dùng cho màn/tab "Unread only".
- Badge count: dùng riêng `GET /notifications/unread-count`.
- Response shape giống `GET /notifications` — có thể reuse UI component.
