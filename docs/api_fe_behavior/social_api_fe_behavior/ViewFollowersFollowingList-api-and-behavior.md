# View Followers Following List – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập xem danh sách **followers** hoặc **following** của một user, có phân trang và tuân thủ privacy.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/users/{userId}/relations`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field   | Type | Required | Mô tả                    |
|---------|------|----------|--------------------------|
| `userId`| UUID | yes      | User cần xem danh sách.  |

### Query Parameters

| Field  | Type   | Required | Mô tả                                      |
|--------|--------|----------|--------------------------------------------|
| `type` | String | yes      | `followers` hoặc `following`.              |
| `page` | int    | no       | Trang (mặc định `0`, ≥ 0).                 |
| `size` | int    | no       | Kích thước trang (mặc định `20`, 1–50).    |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach quan he thanh cong.",
  "data": {
    "targetUserId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "type": "followers",
    "items": [
      {
        "userId": "8b2c4f1e-2a3b-4c5d-9e0f-1a2b3c4d5e6f",
        "displayName": "Follower User",
        "avatarUrl": "https://cdn.example/avatar.png",
        "followedAt": "2026-05-18T08:00:00.000Z"
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
| 400  | `SOCIAL-400`               | `type` thiếu hoặc không hợp lệ.            |
| 400  | `SOCIAL-400-PAGINATION`    | `page` / `size` không hợp lệ.              |
| 401  | `SOCIAL-401`               | Không có hoặc JWT không hợp lệ.            |
| 403  | `SOCIAL-403`               | Tài khoản private, viewer chưa đủ quyền.   |
| 404  | `SOCIAL-404`               | Target user không tồn tại / đã xóa.        |
| 500  | `SOCIAL-500`               | Lỗi server.                                |

## 5. Business Rules

- **`type=followers`:** Query `FOLLOWS` theo `followee_id = userId`, chỉ `status = ACCEPTED`, sort `created_at` desc.
- **`type=following`:** Query theo `follower_id = userId`, chỉ `ACCEPTED`, sort `created_at` desc.
- Mỗi item enrich từ `user_projections` (`displayName`, `avatarUrl`); user đã xóa khỏi projection → `displayName`/`avatarUrl` có thể `null`.
- **Privacy** (giống social profile):
  - Xem chính mình hoặc target **public** → được xem list.
  - Target **private** → chỉ xem được nếu viewer follow target với `ACCEPTED`.
- Read-only; không ghi outbox.

## 6. Edge Cases

- **List rỗng:** 200, `items: []`, `totalElements: 0`.
- **Private + chưa follow:** 403.
- **Gọi lại `type=following`:** Cùng endpoint, đổi query `type`.

## 7. Data Dependencies

| Storage    | Table/Collection   | Action                          |
|------------|--------------------|---------------------------------|
| PostgreSQL | `follows`          | Paginated read ACCEPTED rows.   |
| MongoDB    | `user_projections` | Read display info per user.     |

## 8. FE Integration Notes

- Tab Followers/Following: đổi `type` query, giữ `page`/`size`.
- Dùng `meta.hasNext` cho infinite scroll.
- Kết hợp `GET .../profile` để lấy counts trước khi mở list.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
