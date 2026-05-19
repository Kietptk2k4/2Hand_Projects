# Delete Post – API & Behavior

## 1. Business Goal
Cho phép xóa bài viết theo cơ chế soft delete (`status = DELETED`, `deleted_at`, `updated_at`) để bảo toàn audit trail và nhất quán liên service.

## 2. API Contract

- **Method:** DELETE
- **URL:** `/api/v1/social/posts/{postId}`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field    | Type   | Required | Mô tả                          |
|----------|--------|----------|--------------------------------|
| `postId` | String | yes      | ID bài viết (MongoDB ObjectId). |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Xoa bai viet thanh cong.",
  "data": {
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
| 404  | `SOCIAL-404`             | Post không tồn tại.                                                   |
| 500  | `SOCIAL-500`             | Lỗi server.                                                           |

### Ví dụ Error 403

```json
{
  "code": 403,
  "success": false,
  "message": "Ban khong co quyen xoa bai viet nay.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 5. Business Rules

- Chỉ **tác giả** (`author_id`) hoặc user có role JWT **`MODERATOR`** / **`ADMIN`** mới được xóa.
- Soft delete: `status = DELETED`, set `deleted_at` và `updated_at` = thời điểm xóa.
- Post đã `DELETED` xử lý **idempotent** → HTTP 200, không ghi DB lại.
- Không hard delete dữ liệu trong MVP.
- Post sau khi xóa không hiển thị trên feed/search.
- User `SUSPENDED`/`DELETED` không được thực hiện hành động → HTTP 403.

## 6. Edge Cases

- **Gọi DELETE lần 2 trên post đã xóa:** HTTP 200, trả về trạng thái `DELETED` hiện tại (idempotent).
- **User thường xóa post người khác:** HTTP 403.
- **Moderator xóa post vi phạm:** HTTP 200 (role `MODERATOR` hoặc `ADMIN` trong JWT claim `roles`).
- **Post không tồn tại:** HTTP 404.
- **JWT không có claim `roles`:** Chỉ tác giả mới xóa được post của mình.

## 7. Data Dependencies

| Storage | Collection/Table | Action                                                |
|---------|------------------|-------------------------------------------------------|
| MongoDB | `posts`          | Update `status`, `deleted_at`, `updated_at`.         |
| MongoDB | `user_projections` | Read-only: kiểm tra status user.                    |

## 8. FE Integration Notes

- **Không cần request body** — chỉ cần `postId` trên path và Bearer token.
- **Sau khi xóa thành công:** Gỡ post khỏi feed/local cache; không gọi Edit trên post đã xóa (sẽ 404).
- **Idempotent retry:** FE có thể retry DELETE an toàn nếu mạng lỗi sau khi server đã xử lý.
- **Token refresh:** 401 → refresh token rồi retry.
- **Field mapping:** `postId`, `deletedAt`, `updatedAt` (camelCase trong response).
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
