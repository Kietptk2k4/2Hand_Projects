# Like Comment – API & Behavior

## 1. Business Goal
Cho phép người dùng like/unlike comment (toggle) để thể hiện tương tác với nội dung hội thoại, đồng bộ relation PostgreSQL và `like_count` trên MongoDB.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/comments/{commentId}/like`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field       | Type   | Required | Mô tả                          |
|-------------|--------|----------|--------------------------------|
| `commentId` | String | yes      | ID comment (MongoDB ObjectId). |

## 3. Response – Success

**HTTP 200 OK** (like mới)

```json
{
  "code": 200,
  "success": true,
  "message": "Like comment thanh cong.",
  "data": {
    "commentId": "674abc123def456789012345",
    "liked": true,
    "likeCount": 5
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

**HTTP 200 OK** (unlike — gọi lại khi đã like)

```json
{
  "code": 200,
  "success": true,
  "message": "Unlike comment thanh cong.",
  "data": {
    "commentId": "674abc123def456789012345",
    "liked": false,
    "likeCount": 4
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
| 404  | `SOCIAL-404`             | Comment không tồn tại hoặc đã bị xóa.     |
| 500  | `SOCIAL-500`             | Lỗi server.                                |

## 5. Business Rules

- **Toggle:** Chưa like → tạo record `COMMENT_REACTION`, tăng `like_count`, `liked: true`.
- **Toggle:** Đã like → xóa record, giảm `like_count` (tối thiểu 0), `liked: false`.
- Mỗi user chỉ có tối đa **1** reaction trên 1 comment (PK `(comment_id, user_id)`).
- Unlike khi chưa có relation: không xảy ra với toggle (lần gọi đầu sẽ like); delete idempotent ở tầng DB.
- Comment phải `status = ACTIVE`; comment `DELETED` → HTTP 404.
- `user_id` lấy từ JWT.

## 6. Edge Cases

- **Gọi POST 2 lần liên tiếp:** Lần 1 like, lần 2 unlike (toggle).
- **Like comment đã xóa:** HTTP 404.
- **`like_count` không âm:** Sau unlike nhiều lần bất thường, counter không xuống dưới 0.

## 7. Data Dependencies

| Storage    | Collection/Table   | Action                                      |
|------------|--------------------|---------------------------------------------|
| PostgreSQL | `comment_reaction` | Insert/delete `(comment_id, user_id)`.      |
| MongoDB    | `comments`         | Increment/decrement `like_count`.           |
| MongoDB    | `user_projections` | Read-only: kiểm tra status user.            |

## 8. FE Integration Notes

- **Toggle UI:** Dùng `data.liked` để cập nhật icon/trạng thái; dùng `data.likeCount` để hiển thị số like.
- **Danh sách likers:** Bấm số like trên `CommentItem.jsx` → `LikesListModal.jsx` gọi `GET /api/v1/social/comments/{commentId}/likes` qua `likesApi.js` / `useLikeUsersList.js`. Xem `ViewCommentLikers-api-and-behavior.md`.
- **Optimistic UI:** Có thể toggle ngay, rollback nếu API lỗi.
- **Token refresh:** 401 → refresh token rồi retry.
- Tham chiếu: `docs/feature_requirements/social/FR_ViewCommentLikers.md`, `docs/engineering_rules/frontend-api-integration.md`.
