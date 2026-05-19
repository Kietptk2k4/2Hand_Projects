# Delete Own Comment – API & Behavior

## 1. Business Goal
Cho phép người dùng xóa mềm comment của chính mình (hoặc moderator xóa theo quyền moderation), đồng thời cập nhật bộ đếm `reply_count` trên post.

## 2. API Contract

- **Method:** DELETE
- **URL:** `/api/v1/social/comments/{commentId}`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field       | Type   | Required | Mô tả                            |
|-------------|--------|----------|----------------------------------|
| `commentId` | String | yes      | ID comment (MongoDB ObjectId).   |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Xoa comment thanh cong.",
  "data": {
    "commentId": "674abc123def456789012345",
    "postId": "507f1f77bcf86cd799439011",
    "status": "DELETED",
    "deletedAt": "2026-05-19T10:30:00Z",
    "updatedAt": "2026-05-19T10:30:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string              | Mô tả                                                                 |
|------|--------------------------|-----------------------------------------------------------------------|
| 401  | `SOCIAL-401`             | Không có hoặc JWT không hợp lệ.                                       |
| 403  | `SOCIAL-403`             | Không phải tác giả và không có role moderation.                       |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản bị SUSPENDED/DELETED.                                       |
| 404  | `SOCIAL-404`             | Comment không tồn tại.                                                |
| 500  | `SOCIAL-500`             | Lỗi server.                                                           |

## 5. Business Rules

- Chỉ **tác giả** comment hoặc user có role JWT **`MODERATOR`** / **`ADMIN`** mới được xóa.
- Soft delete: `status = DELETED`, set `deleted_at` và `updated_at`.
- Comment đã `DELETED` xử lý **idempotent** → HTTP 200, không giảm `reply_count` lần nữa.
- Khi xóa comment `ACTIVE` lần đầu: giảm `POSTS.reply_count` (tối thiểu 0).
- Không hard delete trong MVP.

## 6. Edge Cases

- **Gọi DELETE lần 2:** HTTP 200, trạng thái `DELETED` giữ nguyên, không trừ counter thêm.
- **User thường xóa comment người khác:** HTTP 403.
- **Moderator xóa comment vi phạm:** HTTP 200.
- **Comment không tồn tại:** HTTP 404.

## 7. Data Dependencies

| Storage | Collection/Table | Action                                      |
|---------|------------------|---------------------------------------------|
| MongoDB | `comments`       | Update `status`, `deleted_at`, `updated_at`. |
| MongoDB | `posts`          | Decrement `reply_count` (khi xóa lần đầu).  |
| MongoDB | `user_projections` | Read-only: kiểm tra status user.          |

## 8. FE Integration Notes

- **Sau khi xóa thành công:** Ẩn comment khỏi thread UI; có thể giảm `reply_count` hiển thị trên post.
- **Idempotent retry:** FE có thể retry DELETE an toàn nếu mạng lỗi sau khi server đã xử lý.
- **Token refresh:** 401 → refresh token rồi retry.
- **Field mapping:** `commentId`, `postId`, `deletedAt`, `updatedAt`.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
