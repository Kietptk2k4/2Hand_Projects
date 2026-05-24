# Count Unread Notifications – API & Behavior

## 1. Business Goal

Trả về số lượng in-app notifications chưa đọc của user hiện tại, dùng cho badge trên app/web.

## 2. API Contract

- **Method:** `GET`
- **URL:** `/api/v1/notification/notifications/unread-count`
- **Auth:** Bearer JWT (user access token)
- **Query params:** none

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Unread notification count retrieved successfully",
  "data": {
    "count": 3
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống / DB unavailable |

## 5. Business Rules

- `user_id` lấy từ JWT subject.
- Count chỉ gồm notification có:
  - `user_id = current_user_id`
  - `is_read = false`
  - `is_deleted = false`
- Count phải khớp `meta.totalElements` của `GET /notifications/unread` (cùng filter).

## 6. Edge Cases

- Không có unread → `count: 0`.
- Notification đã read hoặc deleted → không được tính.
- Notification của user khác → không được tính.

## 7. Data Dependencies

- Table: `user_notifications`
- Index hỗ trợ: `idx_user_notifications_user_unread`

## 8. FE Integration Notes

- Poll định kỳ hoặc refresh sau mark-read/delete.
- Hiển thị badge khi `count > 0`; cap display (vd. `99+`) ở FE nếu cần.
- Không dùng endpoint này để render danh sách — dùng `GET /notifications/unread`.
