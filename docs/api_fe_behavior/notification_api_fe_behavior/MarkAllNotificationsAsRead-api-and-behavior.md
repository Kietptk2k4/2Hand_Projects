# Mark All Notifications As Read – API & Behavior

## 1. Business Goal

Cho phép user đánh dấu **tất cả** in-app notifications chưa đọc của mình là đã đọc trong một thao tác.

## 2. API Contract

- **Method:** `PATCH`
- **URL:** `/api/v1/notification/notifications/read-all`
- **Auth:** Bearer JWT (user access token)
- **Body:** none

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "All notifications marked as read",
  "data": {
    "updatedCount": 3
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

`updatedCount` = số row vừa được cập nhật từ unread → read. Gọi lại khi không còn unread → `updatedCount: 0`.

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống / DB timeout |

## 5. Business Rules

- Chỉ cập nhật notification có:
  - `user_id = current_user_id` (JWT)
  - `is_read = false`
  - `is_deleted = false`
- Set `is_read = true`, `read_at = now()` cho các row được cập nhật.
- Idempotent: gọi lại trả success với `updatedCount = 0` nếu không còn unread.
- Không ảnh hưởng notification của user khác.

## 6. Edge Cases

- User không có unread → `updatedCount: 0`.
- Notification đã read / deleted → bỏ qua.
- Sau mark-all, `GET /notifications/unread-count` trả `count: 0`.

## 7. Data Dependencies

- Table: `user_notifications`
- Index hỗ trợ: `idx_user_notifications_user_unread`

## 8. FE Integration Notes

- Dùng cho nút "Mark all as read".
- Sau success, refresh unread badge và unread list.
- `updatedCount` có thể hiển thị toast ("3 notifications marked as read") nếu cần.
