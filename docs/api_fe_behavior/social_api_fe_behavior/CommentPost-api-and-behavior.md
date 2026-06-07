# Comment Post – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập tạo bình luận cấp 1 (top-level) trên bài viết đang hoạt động.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/posts/{postId}/comments`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field    | Type   | Required | Mô tả                          |
|----------|--------|----------|--------------------------------|
| `postId` | String | yes      | ID bài viết (MongoDB ObjectId). |

### Request Body (application/json)

| Field         | Type                    | Required | Mô tả                                      |
|---------------|-------------------------|----------|--------------------------------------------|
| `contentText` | String (max 2000)       | yes      | Nội dung bình luận.                        |
| `media`       | Array\<MediaItem\> (max 5) | optional | Media đính kèm.                         |
| `media[].url` | String                  | required (nếu có media) | URL media.                 |
| `media[].type`| `IMAGE` hoặc `VIDEO`    | required (nếu có media) | Loại media.                |

### Ví dụ Request

```json
{
  "contentText": "Bài đăng hay quá!",
  "media": []
}
```

## 3. Response – Success

**HTTP 201 Created**

```json
{
  "code": 201,
  "success": true,
  "message": "Tao binh luan thanh cong.",
  "data": {
    "commentId": "674abc123def456789012345",
    "postId": "507f1f77bcf86cd799439011",
    "authorId": "550e8400-e29b-41d4-a716-446655440001",
    "contentText": "Bài đăng hay quá!",
    "media": [],
    "status": "ACTIVE",
    "createdAt": "2026-05-19T10:30:00Z",
    "updatedAt": "2026-05-19T10:30:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

> `parentCommentId` không có trong response (top-level comment).

## 4. Response – Error

| HTTP | Code string              | Mô tả                                                                 |
|------|--------------------------|-----------------------------------------------------------------------|
| 401  | `SOCIAL-401`             | Không có hoặc JWT không hợp lệ.                                       |
| 400  | `SOCIAL-400-VALIDATION`  | Payload không hợp lệ (rỗng, quá dài, media sai).                      |
| 403  | `SOCIAL-403`             | Post tắt comment hoặc post chưa ACTIVE.                               |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản bị SUSPENDED/DELETED.                                       |
| 404  | `SOCIAL-404`             | Post không tồn tại / đã xóa.                                          |
| 500  | `SOCIAL-500`             | Lỗi server.                                                           |

## 5. Business Rules

- Post phải tồn tại, `status = ACTIVE`, `allow_comments = true`.
- Post `DELETED` → HTTP 404; post tắt comment → HTTP 403.
- Post `DRAFT` hoặc trạng thái khác `ACTIVE` → HTTP 403.
- Tạo comment mới với `parent_comment_id = null` (top-level).
- Tăng `POSTS.reply_count` sau khi tạo comment thành công.
- Ghi `OUTBOX_EVENTS` với `event_type = COMMENT_CREATED`, `status = PENDING` (cùng transaction application).
- `author_id` lấy từ JWT, không nhận từ body.

## 6. Edge Cases

- **`contentText` rỗng:** HTTP 400 (validation layer).
- **Nội dung chứa script/HTML nguy hiểm:** HTTP 400 field `contentText`.
- **Quá 5 media:** HTTP 400 field `media`.
- **Media type không phải IMAGE/VIDEO:** HTTP 400 field `media[].type`.

## 7. Data Dependencies

| Storage    | Collection/Table   | Action                                      |
|------------|--------------------|---------------------------------------------|
| MongoDB    | `comments`         | Insert comment top-level.                   |
| MongoDB    | `posts`            | Increment `reply_count`.                    |
| PostgreSQL | `outbox_events`    | Insert `COMMENT_CREATED` (PENDING).         |
| MongoDB    | `user_projections` | Read-only: kiểm tra status user.            |

## 8. FE Integration Notes

- **Thread UI:** Dùng endpoint này cho comment gốc; reply dùng `POST /api/v1/social/comments/{commentId}/replies`.
- **Comment media (FE):** `CommentComposer.jsx` hỗ trợ đính kèm tối đa 5 ảnh/video (`MAX_COMMENT_MEDIA_ITEMS` trong `commentConstants.js`). Upload qua `useCommentMediaUpload.js` — tái sử dụng `POST /api/v1/social/posts/media/upload-url` (`createPostApi.js`), map payload bằng `mapCommentMediaPayload.js`, gửi `media[]` trong body khi submit (`usePostComments.js` → `submitTopLevel`). `contentText` vẫn bắt buộc kể cả khi có media.
- **Hiển thị media:** `CommentMediaDisplay.jsx` render `media` trên `CommentItem.jsx` sau khi tạo hoặc khi list comments.
- **Optimistic UI:** Sau 201, append comment vào danh sách; có thể tăng `reply_count` trên post card.
- **Token refresh:** 401 → refresh token rồi retry.
- **Field mapping:** `commentId`, `postId`, `authorId`, `contentText`, `media`, `createdAt`, `updatedAt`.
- Tham chiếu: `docs/feature_requirements/social/FR_CommentPost.md`, `docs/engineering_rules/frontend-api-integration.md`.
