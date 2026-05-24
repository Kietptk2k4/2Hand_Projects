# Mark Notification As Read – API & Behavior

## 1. Business Goal

Cho phép user đánh dấu một in-app notification là đã đọc, cập nhật `is_read` và `read_at`.

## 2. API Contract

- **Method:** `PATCH`
- **URL:** `/api/v1/notification/notifications/{notificationId}/read`
- **Auth:** Bearer JWT (user access token)
- **Path params:**

| Param | Type | Required |
|-------|------|----------|
| `notificationId` | UUID | Yes |

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "notificationId": "11111111-1111-1111-1111-111111111111",
    "read": true,
    "readAt": "2026-05-24T12:00:00Z",
    "alreadyRead": false
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

`alreadyRead: true` khi notification đã read trước đó — idempotent, `readAt` không đổi.

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 404 | 404 | Notification không tồn tại, đã deleted, hoặc thuộc user khác |
| 500 | 500 | Lỗi hệ thống |

## 5. Business Rules

- `user_id` lấy từ JWT — chỉ mark notification của chính user.
- Notification `is_deleted = true` → 404 (hidden).
- Notification của user khác → 404 (tránh leak existence).
- Unread → set `is_read = true`, `read_at = now()`.
- Already read → success, giữ nguyên `read_at`.

## 6. Edge Cases

- Gọi lặp PATCH → idempotent success.
- Notification id không tồn tại → 404.
- Concurrent mark-read → idempotent.

## 7. Data Dependencies

- Table: `user_notifications`
- Constraint: `read_at` null khi unread; non-null khi read

## 8. FE Integration Notes

- Sau mark-read thành công, refresh unread badge (`GET /notifications/unread-count`).
- Có thể optimistic update UI trước khi API trả về.
- 404 → remove item khỏi list local nếu đã deleted/không còn quyền.
