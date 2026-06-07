# Like Unlike Post – API & Behavior

## 1. Business Goal
Cho phép người dùng like/unlike bài viết (toggle), đồng bộ relation PostgreSQL, `like_count` trên MongoDB, và phát event `POST_LIKED` khi like thành công.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/posts/{postId}/like`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field   | Type   | Required | Mô tả                        |
|---------|--------|----------|------------------------------|
| `postId`| String | yes      | ID bài viết (MongoDB ObjectId). |

## 3. Response – Success

**HTTP 200 OK** (like mới)

```json
{
  "code": 200,
  "success": true,
  "message": "Like bai viet thanh cong.",
  "data": {
    "postId": "507f1f77bcf86cd799439011",
    "liked": true,
    "likeCount": 10
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
  "message": "Unlike bai viet thanh cong.",
  "data": {
    "postId": "507f1f77bcf86cd799439011",
    "liked": false,
    "likeCount": 9
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
| 404  | `SOCIAL-404`             | Post không tồn tại hoặc không ACTIVE.      |
| 500  | `SOCIAL-500`             | Lỗi server.                                |

## 5. Business Rules

- **Toggle:** Chưa like → insert `post_likes`, tăng `like_count`, ghi `OUTBOX_EVENTS` (`POST_LIKED`, PENDING), `liked: true`.
- **Toggle:** Đã like → xóa relation, giảm `like_count` (≥ 0), **không** ghi outbox, `liked: false`.
- Mỗi user tối đa **1** like/post (PK `(post_id, user_id)`).
- Unlike khi chưa có relation: với toggle, lần gọi đầu sẽ like (delete idempotent ở DB).
- Post phải `status = ACTIVE`; `DELETED`/`DRAFT` → HTTP 404.
- `user_id` lấy từ JWT.

## 6. Edge Cases

- **Gọi POST 2 lần:** Lần 1 like, lần 2 unlike.
- **Like post DRAFT/DELETED:** HTTP 404.
- **`like_count` không âm:** Counter không xuống dưới 0.

## 7. Data Dependencies

| Storage    | Collection/Table | Action                                      |
|------------|------------------|---------------------------------------------|
| PostgreSQL | `post_likes`     | Insert/delete `(post_id, user_id)`.         |
| MongoDB    | `posts`          | Increment/decrement `like_count`.           |
| PostgreSQL | `outbox_events`  | Insert `POST_LIKED` khi like (PENDING).     |
| MongoDB    | `user_projections` | Read-only: kiểm tra status user.          |

## 8. FE Integration Notes

- **Toggle UI:** Dùng `data.liked` cho trạng thái nút like; `data.likeCount` cho hiển thị số like.
- **Danh sách likers:** Bấm số like (`LikeCountButton.jsx`) → `LikesListModal.jsx` gọi `GET /api/v1/social/posts/{postId}/likes` qua `likesApi.js` / `useLikeUsersList.js`. Xem `ViewPostLikers-api-and-behavior.md`.
- **Notification:** Downstream consume `POST_LIKED` từ outbox/broker (không gọi trực tiếp từ FE).
- **Token refresh:** 401 → refresh token rồi retry.
- Tham chiếu: `docs/feature_requirements/social/FR_ViewPostLikers.md`, `docs/engineering_rules/frontend-api-integration.md`.
