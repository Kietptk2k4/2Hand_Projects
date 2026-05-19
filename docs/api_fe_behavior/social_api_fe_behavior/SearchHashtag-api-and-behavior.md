# Search Hashtag – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập tìm post theo hashtag cụ thể, có phân trang và lọc visibility/follow.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/search/hashtags/{hashtag}`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field     | Type   | Required | Mô tả                                      |
|-----------|--------|----------|--------------------------------------------|
| `hashtag` | String | yes      | Hashtag (có hoặc không có `#` ở đầu).      |

### Query Parameters

| Field  | Type | Required | Mô tả                                   |
|--------|------|----------|-----------------------------------------|
| `page` | int  | no       | Trang (mặc định `0`, ≥ 0).              |
| `size` | int  | no       | Kích thước trang (mặc định `20`, 1–50).  |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tim kiem hashtag thanh cong.",
  "data": {
    "hashtag": "travel",
    "items": [
      {
        "postId": "507f1f77bcf86cd799439011",
        "authorId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "caption": "Trip to Da Nang",
        "media": [],
        "visibility": "PUBLIC",
        "likeCount": 2,
        "replyCount": 0,
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

| HTTP | Code string                | Mô tả                                      |
|------|----------------------------|--------------------------------------------|
| 400  | `SOCIAL-400`               | Hashtag rỗng, sai định dạng, hoặc quá dài. |
| 400  | `SOCIAL-400-PAGINATION`    | `page` / `size` không hợp lệ.              |
| 401  | `SOCIAL-401`               | Không có hoặc JWT không hợp lệ.            |
| 500  | `SOCIAL-500`               | Lỗi server.                                |

## 5. Business Rules

- Normalize hashtag: bỏ `#` đầu chuỗi; chỉ chấp nhận `[a-zA-Z0-9_]`, tối đa 100 ký tự.
- Match exact trong mảng `hashtags` (hỗ trợ cả dạng `travel` và `#travel` trong DB).
- Chỉ post `status = ACTIVE` (không `DRAFT`/`DELETED`).
- **Visibility** (giống Search Post):
  - `PUBLIC` — mọi user đăng nhập.
  - `FOLLOWERS` — chỉ khi viewer follow author với `ACCEPTED`.
- Read-only; **không** ghi `SEARCH_HISTORY` (chỉ Search Post).

## 6. Edge Cases

- **Path `#travel`:** normalize thành `travel`.
- **Không có post:** 200, `items: []`.
- **Hashtag chỉ có `#`:** 400.

## 7. Data Dependencies

| Storage    | Collection/Table | Action                          |
|------------|------------------|---------------------------------|
| MongoDB    | `posts`          | Query `hashtags` + visibility.  |
| PostgreSQL | `follows`        | Read ACCEPTED followees.        |

## 8. FE Integration Notes

- Có thể gửi hashtag có hoặc không có `#`; response `data.hashtag` luôn không có `#`.
- Dùng `meta.hasNext` cho infinite scroll.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
