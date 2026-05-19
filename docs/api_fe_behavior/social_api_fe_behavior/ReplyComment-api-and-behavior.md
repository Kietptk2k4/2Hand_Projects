# Reply Comment – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập trả lời một comment đang hoạt động để mở rộng thread hội thoại trên bài viết.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/comments/{commentId}/replies`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field       | Type   | Required | Mô tả                                |
|-------------|--------|----------|--------------------------------------|
| `commentId` | String | yes      | ID comment cha (MongoDB ObjectId).   |

### Request Body (application/json)

| Field         | Type                    | Required | Mô tả                                      |
|---------------|-------------------------|----------|--------------------------------------------|
| `contentText` | String (max 2000)       | yes      | Nội dung trả lời.                          |
| `media`       | Array\<MediaItem\> (max 5) | optional | Media đính kèm.                         |
| `media[].url` | String                  | required (nếu có media) | URL media.                 |
| `media[].type`| `IMAGE` hoặc `VIDEO`    | required (nếu có media) | Loại media.                |

### Ví dụ Request

```json
{
  "contentText": "Đồ này còn không bạn?",
  "media": []
}
```

## 3. Response – Success

**HTTP 201 Created**

```json
{
  "code": 201,
  "success": true,
  "message": "Tra loi comment thanh cong.",
  "data": {
    "commentId": "674abc123def456789012345",
    "postId": "507f1f77bcf86cd799439011",
    "parentCommentId": "674aaa111bbb222333444555",
    "authorId": "550e8400-e29b-41d4-a716-446655440001",
    "contentText": "Đồ này còn không bạn?",
    "media": [],
    "status": "ACTIVE",
    "createdAt": "2026-05-19T10:30:00Z",
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
| 400  | `SOCIAL-400-VALIDATION`  | Payload không hợp lệ hoặc vượt độ sâu thread MVP.                     |
| 403  | `SOCIAL-403`             | Post tắt comment hoặc post chưa ACTIVE.                               |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản bị SUSPENDED/DELETED.                                       |
| 404  | `SOCIAL-404`             | Comment cha hoặc post không tồn tại / đã xóa.                         |
| 500  | `SOCIAL-500`             | Lỗi server.                                                           |

## 5. Business Rules

- Comment cha phải tồn tại và `status = ACTIVE`.
- Comment cha đã `DELETED` → HTTP 404.
- **Độ sâu thread MVP:** chỉ được reply comment **cấp 1** (top-level). Không reply vào reply (nested sâu hơn) → HTTP 400.
- Post liên quan phải `ACTIVE` và `allow_comments = true`.
- Post `DELETED` → HTTP 404; post tắt comment → HTTP 403.
- Tạo comment mới với `parent_comment_id` = ID comment cha.
- Tăng `POSTS.reply_count` sau khi tạo reply thành công.
- Ghi `OUTBOX_EVENTS` với `event_type = COMMENT_CREATED`, `status = PENDING` (cùng luồng transaction application).
- `author_id` lấy từ JWT, không nhận từ body.

## 6. Edge Cases

- **Reply vào comment đã là reply (cấp 2):** HTTP 400 field `parentCommentId`.
- **Post DRAFT:** HTTP 403.
- **`contentText` rỗng:** HTTP 400 (validation layer).
- **Nội dung chứa script/HTML nguy hiểm:** HTTP 400 field `contentText`.
- **Comment cha thuộc post đã xóa:** HTTP 404 (post không tồn tại / đã xóa).

## 7. Data Dependencies

| Storage    | Collection/Table   | Action                                      |
|------------|--------------------|---------------------------------------------|
| MongoDB    | `comments`         | Insert reply với `parent_comment_id`.       |
| MongoDB    | `posts`            | Increment `reply_count`.                    |
| PostgreSQL | `outbox_events`    | Insert `COMMENT_CREATED` (PENDING).         |
| MongoDB    | `user_projections` | Read-only: kiểm tra status user.            |

## 8. FE Integration Notes

- **Thread UI:** MVP chỉ hiển thị 2 tầng (comment gốc + reply); không gọi API reply cho reply.
- **Optimistic UI:** Sau 201, append reply vào thread của `parentCommentId`; có thể tăng `reply_count` trên post card.
- **Token refresh:** 401 → refresh token rồi retry.
- **Field mapping:** `commentId`, `postId`, `parentCommentId`, `authorId`, `contentText`, `createdAt`, `updatedAt`.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
