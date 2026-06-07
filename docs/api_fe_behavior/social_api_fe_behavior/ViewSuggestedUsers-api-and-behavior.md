# View Suggested Users – API & Behavior

## 1. Business Goal
Gợi ý người dùng nên follow trên feed sidebar, ưu tiên mutual follow và loại trừ bản thân / người đã follow.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/users/suggestions`
- **Auth:** Bearer JWT (required)

### Query Parameters

| Field  | Type | Required | Default | Mô tả                                           |
|--------|------|----------|---------|-------------------------------------------------|
| `page` | int  | no       | `0`     | Trang (≥ 0).                                    |
| `size` | int  | no       | `20`    | Kích thước trang (1–50).                        |
| `limit`| int  | no       | —       | Alias của `size` (FE sidebar dùng `limit=3`).   |

> Nếu cả `size` và `limit` được gửi, BE ưu tiên `size`.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach goi y nguoi dung thanh cong.",
  "data": {
    "items": [
      {
        "userId": "550e8400-e29b-41d4-a716-446655440001",
        "displayName": "User A",
        "avatarUrl": "https://cdn.2hands.vn/avatars/...",
        "followStatus": "NONE",
        "mutualFollowCount": 3
      }
    ],
    "meta": {
      "page": 0,
      "size": 3,
      "totalElements": 12,
      "totalPages": 4,
      "hasNext": true
    }
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

### `followStatus`

| Giá trị      | Mô tả                                      |
|--------------|--------------------------------------------|
| `NONE`       | Chưa follow.                               |
| `FOLLOWING`  | Viewer đang follow user.                   |
| `PENDING`    | Đã gửi follow request (nếu có).           |

## 4. Response – Error

| HTTP | Code string             | Mô tả                                      |
|------|-------------------------|--------------------------------------------|
| 401  | `SOCIAL-401`            | Không có hoặc JWT không hợp lệ.            |
| 400  | `SOCIAL-400-PAGINATION` | `page`/`size` không hợp lệ.              |
| 500  | `SOCIAL-500`            | Lỗi server.                                |

## 5. Business Rules

- Loại trừ: chính viewer, user đã follow, user không `ACTIVE` trong projection.
- Sắp xếp: `mutualFollowCount` giảm dần, sau đó `displayName` A→Z.
- Enrich `displayName` / `avatarUrl` từ `user_projections`.
- Read-only.

## 6. Edge Cases

- Không còn candidate → `items: []`, `meta.totalElements: 0`.
- User projection thiếu avatar → FE có thể fallback qua Auth public profile.

## 7. Data Dependencies

| Storage    | Table/Collection   | Action                              |
|------------|--------------------|-------------------------------------|
| PostgreSQL | `follows`          | Exclude followees, mutual counts.   |
| MongoDB    | `user_projections` | Candidate users + display info.     |

## 8. FE Integration Notes

- **API client:** `discoveryApi.js` → `fetchSuggestedUsers({ page, limit, size })`.
- **Hook:** `useSuggestedUsers.js` — sidebar limit 3, modal expanded limit 20; follow/unfollow inline.
- **UI:** `FeedRightSidebar.jsx`, `SuggestedUsersModal.jsx`.
- **Field mapping:** `userId`, `displayName`, `avatarUrl`, `followStatus`, `mutualFollowCount`.
- **Token refresh:** 401 → refresh token rồi retry.
- Tham chiếu: `docs/feature_requirements/social/FR_ViewSuggestedUsers.md`.