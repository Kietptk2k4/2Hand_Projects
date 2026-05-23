# View Saved Posts – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập xem danh sách bài viết đã lưu của chính mình, có phân trang.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/posts/saved`
- **Auth:** Bearer JWT (required)

### Query Parameters

| Field  | Type | Required | Default | Mô tả                          |
|--------|------|----------|---------|--------------------------------|
| `page` | int  | no       | `0`     | Trang (bắt đầu từ 0).          |
| `size` | int  | no       | `20`    | Số bản ghi mỗi trang (1–50).   |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach bai da luu thanh cong.",
  "data": {
    "items": [
      {
        "postId": "507f1f77bcf86cd799439011",
        "authorId": "550e8400-e29b-41d4-a716-446655440001",
        "caption": "Bài viết đã lưu",
        "media": [{ "url": "https://cdn.2hands.vn/social/posts/...", "type": "IMAGE" }],
        "visibility": "PUBLIC",
        "likeCount": 10,
        "replyCount": 2,
        "hashtags": ["thoitrang"],
        "allowComments": true,
        "savedAt": "2026-05-20T08:00:00Z",
        "createdAt": "2026-05-18T10:15:30Z",
        "updatedAt": "2026-05-18T10:20:30Z"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1,
      "hasNext": false
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 4. Response – Error

| HTTP | Code string             | Mô tả                                      |
|------|-------------------------|--------------------------------------------|
| 401  | `SOCIAL-401`            | Không có hoặc JWT không hợp lệ.            |
| 400  | `SOCIAL-400-PAGINATION` | `page` hoặc `size` không hợp lệ.           |
| 500  | `SOCIAL-500`            | Lỗi server.                                |

> **Read-only:** Không áp dụng `FR_EnforceUserStatusOnWrite` — user `SUSPENDED` vẫn được xem danh sách đã lưu (chỉ bị chặn thao tác ghi như save/unsave).

## 5. Business Rules

- Chỉ trả bài đã save của **current user** (theo `user_id` trong JWT).
- Sắp xếp theo thời điểm lưu (`post_saves.created_at` giảm dần).
- **Bỏ qua** (không đưa vào `items`) post:
  - `status = DELETED`
  - Không còn quyền xem (ví dụ `FOLLOWERS` khi viewer không follow author)
  - `DRAFT` của người khác
- Chưa save bài nào → `items: []`, `meta.totalElements: 0`.
- Read-only: không ghi DB.

## 6. Edge Cases

- Post đã save nhưng bị xóa khỏi Mongo → bỏ qua trong danh sách.
- Một trang có nhiều save nhưng hầu hết không xem được → `items` có thể ít hơn `size` (pagination theo bản ghi save trong DB).

## 7. Data Dependencies

| Storage    | Table/Collection | Action                                |
|------------|------------------|---------------------------------------|
| PostgreSQL | `post_saves`     | Read paginated theo `user_id`.        |
| MongoDB    | `posts`          | Read batch theo `post_id`.            |
| MongoDB    | `follows`        | Read accepted followees (visibility). |

## 8. FE Integration Notes

- Màn **Đã lưu**: gọi endpoint này thay vì feed global.
- Hiển thị `savedAt` để sort UI nhất quán với server.
- `meta.hasNext` dùng cho infinite scroll.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
