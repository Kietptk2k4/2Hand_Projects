# Search Post – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập tìm post theo từ khóa (caption/hashtags), có phân trang, lọc visibility/follow, và lưu search history best-effort.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/search/posts`
- **Auth:** Bearer JWT (required)

### Query Parameters

| Field  | Type   | Required | Mô tả                                      |
|--------|--------|----------|--------------------------------------------|
| `q`    | String | yes      | Từ khóa tìm kiếm (không rỗng sau trim).    |
| `page` | int    | no       | Trang (mặc định `0`, ≥ 0).                 |
| `size` | int    | no       | Kích thước trang (mặc định `20`, 1–50).    |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tim kiem bai viet thanh cong.",
  "data": {
    "keyword": "travel",
    "items": [
      {
        "postId": "507f1f77bcf86cd799439011",
        "authorId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "caption": "My travel post",
        "media": [],
        "visibility": "PUBLIC",
        "likeCount": 5,
        "replyCount": 1,
        "hashtags": ["travel"],
        "allowComments": true,
        "createdAt": "2026-05-19T10:00:00Z",
        "updatedAt": "2026-05-19T10:00:00Z"
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
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string                | Mô tả                          |
|------|----------------------------|--------------------------------|
| 400  | `SOCIAL-400`               | `q` rỗng hoặc quá dài (255).   |
| 400  | `SOCIAL-400-PAGINATION`    | `page` / `size` không hợp lệ.  |
| 401  | `SOCIAL-401`               | Không có hoặc JWT không hợp lệ.|
| 500  | `SOCIAL-500`               | Lỗi server.                    |

## 5. Business Rules

- Chỉ post `status = ACTIVE`.
- **Visibility:**
  - `PUBLIC` — ai cũng thấy nếu khớp từ khóa.
  - `FOLLOWERS` — chỉ khi viewer follow author với `ACCEPTED`.
- Tìm trong `caption` và `hashtags` (không phân biệt hoa thường, literal match sau escape regex).
- **`SEARCH_HISTORY`:** insert hoặc refresh `updated_at` theo `(user_id, keyword)` — **best-effort**; lỗi ghi history không làm fail response chính.
- Không trả `DRAFT` / `DELETED`.

## 6. Edge Cases

- **Không có kết quả:** 200, `items: []`.
- **Keyword có ký tự đặc biệt:** được escape an toàn (literal search).
- **History DB lỗi:** vẫn 200 với kết quả search.

## 7. Data Dependencies

| Storage    | Table/Collection | Action                                |
|------------|------------------|---------------------------------------|
| MongoDB    | `posts`          | Query ACTIVE + visibility + text.     |
| PostgreSQL | `follows`        | Read ACCEPTED followees của viewer.   |
| PostgreSQL | `search_history` | Best-effort upsert keyword.           |

## 8. FE Integration Notes

- Debounce input search; gửi `q` đã trim.
- Dùng `meta.hasNext` cho load more.
- Hiển thị post item giống feed card.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
