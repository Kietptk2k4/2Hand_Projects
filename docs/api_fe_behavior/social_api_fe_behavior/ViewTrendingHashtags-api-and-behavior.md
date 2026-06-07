# View Trending Hashtags – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập xem danh sách hashtag đang thịnh hành trên feed sidebar, dựa trên engagement trong cửa sổ thời gian gần đây.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/search/trending-hashtags`
- **Auth:** Bearer JWT (required)

### Query Parameters

| Field   | Type | Required | Default | Mô tả                                      |
|---------|------|----------|---------|--------------------------------------------|
| `limit` | int  | no       | `5`     | Số hashtag trả về (1–20).                  |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay hashtag thi hanh thanh cong.",
  "data": {
    "items": [
      {
        "tag": "travel",
        "postCount": 12,
        "totalLikes": 340,
        "totalReplies": 56,
        "engagementCount": 396,
        "score": 516
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string             | Mô tả                                      |
|------|-------------------------|--------------------------------------------|
| 401  | `SOCIAL-401`            | Không có hoặc JWT không hợp lệ.            |
| 400  | `SOCIAL-400-PAGINATION` | `limit` ngoài khoảng 1–20.                 |
| 500  | `SOCIAL-500`            | Lỗi server.                                |

## 5. Business Rules

- Chỉ tính post `ACTIVE` trong cửa sổ **7 ngày** gần nhất.
- `score` = `engagementCount` + (`postCount` × trọng số); dùng để xếp hạng hashtag.
- `engagementCount` = tổng `like_count` + `reply_count` của các post chứa hashtag.
- Read-only: không ghi DB.

## 6. Edge Cases

- Không có hashtag phù hợp → `items: []`.
- `limit` ≤ 0 → dùng default `5`.

## 7. Data Dependencies

| Storage | Collection/Table | Action                          |
|---------|------------------|---------------------------------|
| MongoDB | `posts`          | Aggregate hashtag + engagement. |

## 8. FE Integration Notes

- **API client:** `discoveryApi.js` → `fetchTrendingHashtags({ limit })`.
- **Hook:** `useTrendingHashtags.js` (cache, loading/error state).
- **UI:** `FeedRightSidebar.jsx` — block "Hashtag thịnh hành"; click tag → trang hashtag posts.
- **Field mapping:** `tag`, `postCount`, `totalLikes`, `totalReplies`, `engagementCount`, `score` (camelCase từ BE).
- **Token refresh:** 401 → refresh token rồi retry.
- Tham chiếu: `docs/feature_requirements/social/FR_ViewTrendingHashtags.md`.