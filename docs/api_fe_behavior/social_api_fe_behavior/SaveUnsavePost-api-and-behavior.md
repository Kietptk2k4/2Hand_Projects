# Save Unsave Post – API & Behavior

## 1. Business Goal
Cho phép người dùng lưu hoặc bỏ lưu bài viết (toggle) để xem lại sau, đồng bộ mapping `post_saves` trên PostgreSQL.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/posts/{postId}/save`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field   | Type   | Required | Mô tả                        |
|---------|--------|----------|------------------------------|
| `postId`| String | yes      | ID bài viết (MongoDB ObjectId). |

## 3. Response – Success

**HTTP 200 OK** (lưu mới)

```json
{
  "code": 200,
  "success": true,
  "message": "Luu bai viet thanh cong.",
  "data": {
    "postId": "507f1f77bcf86cd799439011",
    "saved": true
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

**HTTP 200 OK** (bỏ lưu — gọi lại khi đã lưu)

```json
{
  "code": 200,
  "success": true,
  "message": "Bo luu bai viet thanh cong.",
  "data": {
    "postId": "507f1f77bcf86cd799439011",
    "saved": false
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string              | Mô tả                                      |
|------|--------------------------|--------------------------------------------|
| 401  | `SOCIAL-401`             | Không có hoặc JWT không hợp lệ.            |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản bị SUSPENDED/DELETED.            |
| 404  | `SOCIAL-404`             | Post không tồn tại hoặc đã `DELETED`.      |
| 500  | `SOCIAL-500`             | Lỗi server.                                |

## 5. Business Rules

- **Toggle:** Chưa lưu → insert `post_saves`, `saved: true`.
- **Toggle:** Đã lưu → delete relation (idempotent nếu row đã mất), `saved: false`.
- Mỗi user tối đa **1** save/post (PK `(post_id, user_id)`).
- Post `DELETED` → HTTP 404; `ACTIVE`/`DRAFT` được phép lưu nếu post còn tồn tại.
- Không ghi outbox / không đổi counter trên MongoDB post.
- `user_id` lấy từ JWT.

## 6. Edge Cases

- **Gọi POST 2 lần:** Lần 1 lưu, lần 2 bỏ lưu.
- **Lưu post đã xóa:** HTTP 404.
- **Bỏ lưu khi chưa lưu (toggle):** Lần gọi sẽ **lưu** (khác explicit unsave idempotent — API dùng toggle theo FR).

## 7. Data Dependencies

| Storage    | Table            | Action                                      |
|------------|------------------|---------------------------------------------|
| PostgreSQL | `post_saves`     | Insert/delete `(post_id, user_id)`.         |
| MongoDB    | `posts`          | Read-only: kiểm tra post tồn tại / status.  |
| MongoDB    | `user_projections` | Read-only: kiểm tra status user.          |

## 8. FE Integration Notes

- **Toggle UI:** Dùng `data.saved` cho trạng thái bookmark/lưu.
- **Danh sách đã lưu:** Dùng API View Saved Posts (feature riêng), không embed trong response này.
- **Token refresh:** 401 → refresh token rồi retry.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
