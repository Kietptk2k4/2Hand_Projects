# List Post Comments – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập xem danh sách bình luận trên một bài viết (top-level hoặc reply theo comment cha), có phân trang và áp dụng rule visibility của post.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/posts/{postId}/comments`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field    | Type   | Required | Mô tả                          |
|----------|--------|----------|--------------------------------|
| `postId` | String | yes      | ID bài viết (MongoDB ObjectId). |

### Query Parameters

| Field               | Type   | Required | Default           | Mô tả                                                                 |
|---------------------|--------|----------|-------------------|-----------------------------------------------------------------------|
| `page`              | int    | no       | `0`               | Trang (>= 0).                                                         |
| `size`              | int    | no       | `20`              | Kích thước trang (1–50).                                              |
| `parent_comment_id` | String | no       | (bỏ trống)        | Bỏ trống: chỉ top-level. Có giá trị: list reply của comment cha.      |
| `sort`              | String | no       | `created_at_asc`  | `created_at_asc` hoặc `created_at_desc`.                              |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach binh luan thanh cong.",
  "data": {
    "items": [
      {
        "commentId": "507f1f77bcf86cd799439012",
        "postId": "507f1f77bcf86cd799439011",
        "parentCommentId": null,
        "author": {
          "userId": "550e8400-e29b-41d4-a716-446655440001",
          "displayName": "User A",
          "avatarUrl": "https://cdn.2hands.vn/avatars/..."
        },
        "contentText": "Hay qua!",
        "media": [],
        "likeCount": 3,
        "replyCount": 1,
        "createdAt": "2026-05-21T10:00:00Z",
        "updatedAt": "2026-05-21T10:00:00Z"
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
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 4. Response – Error

| HTTP | Code string             | Mô tả                                                                 |
|------|-------------------------|-----------------------------------------------------------------------|
| 401  | `SOCIAL-401`            | Không có hoặc JWT không hợp lệ.                                       |
| 400  | `SOCIAL-400-PAGINATION` | `page` hoặc `size` không hợp lệ.                                      |
| 400  | `SOCIAL-400`            | `postId`, `parent_comment_id` hoặc `sort` không hợp lệ.               |
| 403  | `SOCIAL-403`            | Không đủ quyền xem post (visibility).                                 |
| 404  | `SOCIAL-404`            | Post hoặc comment cha không tồn tại / không ACTIVE.                   |
| 500  | `SOCIAL-500`            | Lỗi server.                                                           |

> **Read-only:** Không áp dụng `FR_EnforceUserStatusOnWrite` — user `SUSPENDED` vẫn được đọc comment (trừ khi product chặn sau này).

## 5. Business Rules

- Chỉ trả comment `status = ACTIVE`; bỏ qua `DELETED`.
- Post phải tồn tại và viewer được xem (cùng logic `FR_ViewPostDetail`: `PUBLIC`, `FOLLOWERS` + follow `ACCEPTED`, author xem `DRAFT` của mình).
- Post `DELETED` hoặc không tồn tại → 404.
- Post không được viewer xem → 403.
- `parent_comment_id` nếu có phải thuộc cùng `post_id` và comment cha `ACTIVE`; sai → 404.
- `replyCount` trên item top-level: số reply trực tiếp (`ACTIVE`) của comment đó; với danh sách reply thì `replyCount = 0`.
- Author `DELETED` trong projection → `displayName: "Tai khoan da xoa"`, `avatarUrl: null`.
- Post `allow_comments = false` vẫn cho phép **đọc** comment cũ (chỉ chặn write).
- Read-only: không ghi DB.

## 6. Edge Cases

- Post không có comment → `items: []`, `meta.totalElements: 0`.
- Expand reply: FE gọi lại endpoint với `parent_comment_id` (MVP không load nested tree một lần).
- `parent_comment_id` không phải ObjectId hợp lệ → 400.

## 7. Data Dependencies

| Storage    | Table/Collection   | Action                                              |
|------------|--------------------|-----------------------------------------------------|
| MongoDB    | `posts`            | Verify tồn tại + visibility.                        |
| MongoDB    | `comments`         | List/filter theo `post_id`, `parent_comment_id`.    |
| PostgreSQL | `user_projections` | Author `display_name`, `avatar_url`.                |
| PostgreSQL | `follows`          | Accepted followees (visibility `FOLLOWERS`).        |

## 8. FE Integration Notes

- **Post Detail Modal** (`FR_ViewPostDetail.md` mục 9.1): gọi top-level comments sau khi `GET /posts/{postId}` thành công; khi user mở reply gọi thêm với `parent_comment_id`.
- **Comment media:** Mỗi item có `media[]` (`url`, `type`); FE render qua `CommentMediaDisplay.jsx` trên `CommentItem.jsx` (ảnh + video inline).
- Dùng `meta.hasNext` cho infinite scroll.
- Response JSON dùng camelCase (`commentId`, `parentCommentId`, `media`, `totalElements`).
- Tham chiếu: `docs/feature_requirements/social/FR_ListPostComments.md`, `docs/engineering_rules/frontend-api-integration.md`.
