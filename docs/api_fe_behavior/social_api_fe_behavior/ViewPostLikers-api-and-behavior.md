# View Post Likers – API & Behavior

## 1. Business Goal
Cho phép người dùng xem danh sách người đã thích một bài viết (phân trang), dùng cho modal danh sách like trên feed và post detail.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/posts/{postId}/likes`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field    | Type   | Required | Mô tả                          |
|----------|--------|----------|--------------------------------|
| `postId` | String | yes      | ID bài viết (MongoDB ObjectId). |

### Query Parameters

| Field  | Type | Required | Default | Mô tả                    |
|--------|------|----------|---------|--------------------------|
| `page` | int  | no       | `0`     | Trang (≥ 0).             |
| `size` | int  | no       | `20`    | Kích thước trang (1–50). |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach nguoi thich bai viet thanh cong.",
  "data": {
    "items": [
      {
        "userId": "550e8400-e29b-41d4-a716-446655440001",
        "displayName": "User A",
        "avatarUrl": "https://cdn.2hands.vn/avatars/...",
        "likedAt": "2026-05-19T10:30:00Z"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "totalElements": 42,
      "totalPages": 3,
      "hasNext": true
    }
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string             | Mô tả                                      |
|------|-------------------------|--------------------------------------------|
| 401  | `SOCIAL-401`            | Không có hoặc JWT không hợp lệ.            |
| 403  | `SOCIAL-403`            | Viewer không có quyền xem post.            |
| 404  | `SOCIAL-404`            | Post không tồn tại / đã xóa.               |
| 400  | `SOCIAL-400-PAGINATION` | `page`/`size` không hợp lệ.              |
| 500  | `SOCIAL-500`            | Lỗi server.                                |

## 5. Business Rules

- Viewer phải được phép xem post (cùng rule `PostViewAccessPolicy` như `GET /posts/{postId}`).
- Danh sách lấy từ `post_likes` (PostgreSQL), sắp xếp theo `liked_at` mới nhất.
- Enrich `displayName`, `avatarUrl` từ `user_projections`.
- Read-only.

## 6. Edge Cases

- Post chưa có like → `items: []`, `totalElements: 0`.
- User đã xóa projection → `displayName` fallback, `avatarUrl` có thể null.

## 7. Data Dependencies

| Storage    | Table/Collection   | Action                    |
|------------|--------------------|---------------------------|
| MongoDB    | `posts`            | Verify tồn tại + visibility. |
| PostgreSQL | `post_likes`       | List likers + pagination. |
| MongoDB    | `user_projections` | Display name/avatar.      |

## 8. FE Integration Notes

- **API client:** `likesApi.js` → `fetchPostLikers(postId, { page, size })`.
- **Hook:** `useLikeUsersList.js` với `targetType: "post"`.
- **Modal:** `LikesListModal.jsx` + `LikesListRow.jsx`; mở khi bấm **số like** (`LikeCountButton`) trên `PostCard`, `PostDetailModal`, v.v.
- **Search:** FE filter client-side theo `displayName` trong modal (không gọi lại API).
- **Pagination:** `meta.hasNext` → nút "Tải thêm".
- Tham chiếu: `docs/feature_requirements/social/FR_ViewPostLikers.md`, `LikeUnlikePost-api-and-behavior.md`.