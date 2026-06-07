# View Comment Likers – API & Behavior

## 1. Business Goal
Cho phép người dùng xem danh sách người đã thích một bình luận (phân trang), dùng cho modal danh sách like trong thread comment.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/comments/{commentId}/likes`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field       | Type   | Required | Mô tả                          |
|-------------|--------|----------|--------------------------------|
| `commentId` | String | yes      | ID comment (MongoDB ObjectId). |

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
  "message": "Lay danh sach nguoi thich binh luan thanh cong.",
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
      "totalElements": 5,
      "totalPages": 1,
      "hasNext": false
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
| 404  | `SOCIAL-404`            | Comment không tồn tại hoặc đã bị xóa.     |
| 400  | `SOCIAL-400-PAGINATION` | `page`/`size` không hợp lệ.              |
| 500  | `SOCIAL-500`            | Lỗi server.                                |

## 5. Business Rules

- Comment phải `status = ACTIVE`.
- Danh sách lấy từ `comment_reaction` (PostgreSQL), sắp xếp theo `liked_at` mới nhất.
- Enrich `displayName`, `avatarUrl` từ `user_projections`.
- Read-only.

## 6. Edge Cases

- Comment chưa có like → `items: []`.
- Comment đã xóa → HTTP 404.

## 7. Data Dependencies

| Storage    | Table/Collection   | Action                    |
|------------|--------------------|---------------------------|
| MongoDB    | `comments`         | Verify comment ACTIVE.    |
| PostgreSQL | `comment_reaction` | List likers + pagination. |
| MongoDB    | `user_projections` | Display name/avatar.      |

## 8. FE Integration Notes

- **API client:** `likesApi.js` → `fetchCommentLikers(commentId, { page, size })`.
- **Hook:** `useLikeUsersList.js` với `targetType: "comment"`.
- **Modal:** `LikesListModal.jsx`; mở khi bấm **số like** trên `CommentItem` (`LikeCountButton`).
- **Pages:** `SocialFeedPage`, `SocialProfilePage`, `PostDetailModal` (truyền `onOpenLikesList` từ `useLikesListModal`).
- Tham chiếu: `docs/feature_requirements/social/FR_ViewCommentLikers.md`, `LikeComment-api-and-behavior.md`.