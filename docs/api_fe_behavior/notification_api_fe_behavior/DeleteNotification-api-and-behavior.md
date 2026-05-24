# Delete Notification – API & Behavior

## 1. Business Goal

Cho phép user ẩn (soft delete) một in-app notification của chính mình.

## 2. API Contract

- **Method:** `DELETE`
- **URL:** `/api/v1/notification/notifications/{notificationId}`
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
  "message": "Notification deleted successfully",
  "data": {
    "notificationId": "11111111-1111-1111-1111-111111111111",
    "deleted": true,
    "alreadyDeleted": false
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

`alreadyDeleted: true` khi notification đã bị soft-delete trước đó — idempotent.

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 404 | 404 | Notification không tồn tại hoặc thuộc user khác |
| 500 | 500 | Lỗi hệ thống |

## 5. Business Rules

- Soft delete: set `is_deleted = true` (không hard delete).
- `user_id` lấy từ JWT — chỉ xóa notification của chính user.
- Notification đã deleted → success idempotent (`alreadyDeleted: true`).
- Notification của user khác / không tồn tại → 404.
- Deleted notification không xuất hiện trong list/unread/unread-count.

## 6. Edge Cases

- Xóa unread notification → giảm unread count.
- Gọi DELETE lặp → success, row vẫn tồn tại với `is_deleted = true`.
- Record không bị xóa vật lý khỏi DB.

## 7. Data Dependencies

- Table: `user_notifications`

## 8. FE Integration Notes

- Sau delete, remove item khỏi list local và refresh unread badge.
- Optimistic UI có thể ẩn ngay; rollback nếu 404.
- Không có undo trong MVP — user không thấy lại notification đã xóa qua API list.
