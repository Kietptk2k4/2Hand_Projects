# View User Posts – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập xem lưới bài viết trên profile của một user (timeline cá nhân), có phân trang và áp dụng privacy + visibility.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/users/{userId}/posts`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field    | Type | Required | Mô tả                    |
|----------|------|----------|--------------------------|
| `userId` | UUID | yes      | ID chủ profile (tác giả). |

### Query Parameters

| Field           | Type   | Required | Default       | Mô tả                                                                 |
|-----------------|--------|----------|---------------|-----------------------------------------------------------------------|
| `page`          | int    | no       | `0`           | Trang (>= 0).                                                         |
| `size`          | int    | no       | `20`          | Kích thước trang (1–50).                                              |
| `status_filter` | string | no       | `published`   | `published` = chỉ `ACTIVE`; `all` = owner only: `ACTIVE` + `DRAFT`.   |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach bai viet thanh cong.",
  "data": {
    "items": [
      {
        "postId": "507f1f77bcf86cd799439011",
        "caption": "Preview caption",
        "media": [{ "url": "https://cdn.2hands.vn/...", "type": "IMAGE" }],
        "visibility": "PUBLIC",
        "likeCount": 5,
        "replyCount": 1,
        "hashtags": [],
        "createdAt": "2026-05-21T09:00:00Z"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "totalElements": 15,
      "totalPages": 1,
      "hasNext": false
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
| 400  | `SOCIAL-400-PAGINATION` | `page`, `size` hoặc `status_filter` không hợp lệ.                     |
| 403  | `SOCIAL-403`            | Profile riêng tư, viewer chưa được follow chấp nhận.                 |
| 404  | `SOCIAL-404`            | User không có trong projection hoặc đã `DELETED`.                     |
| 500  | `SOCIAL-500`            | Lỗi server.                                                           |

> **Read-only:** User `SUSPENDED` vẫn được **đọc** bài cũ; write bị chặn bởi `FR_EnforceUserStatusOnWrite`.

## 5. Business Rules

### Private profile
- `is_private = true` và viewer không phải owner → cần follow `ACCEPTED`, nếu không → **403**.

### Visibility từng post
- `PUBLIC`: hiển thị khi viewer được phép xem profile.
- `FOLLOWERS`: chỉ khi viewer follow `ACCEPTED` hoặc là owner.
- `DELETED`: không hiển thị (query chỉ lấy `ACTIVE` / `DRAFT` của owner).

### Owner vs viewer

| Viewer | status_filter | Post trả về                          |
|--------|---------------|--------------------------------------|
| Owner  | `published`   | `ACTIVE` only                        |
| Owner  | `all`         | `ACTIVE` + `DRAFT`                   |
| Khác   | `published`   | `ACTIVE` theo visibility             |
| Khác   | `all`         | **400** — chỉ owner dùng `all`       |

### Target user
- Không có trong `user_projections` hoặc `status = DELETED` → **404**.

## 6. Edge Cases

- Profile public, viewer chưa follow → chỉ thấy post `PUBLIC`.
- Profile public, viewer đã follow → thấy `PUBLIC` + `FOLLOWERS`.
- Owner dùng `status_filter=all` để preview draft trên grid profile.
- Chưa có bài → `items: []`, `meta.totalElements: 0`.

## 7. Data Dependencies

| Storage    | Table/Collection   | Action                                      |
|------------|--------------------|---------------------------------------------|
| MongoDB    | `posts`            | Query theo `author_id`, `status`, visibility |
| PostgreSQL | `user_projections` | `is_private`, `status`                      |
| PostgreSQL | `follows`          | Quan hệ follow `ACCEPTED`                   |

## 8. FE Integration Notes

- Màn profile grid: gọi sau `GET .../profile` khi `canViewFullProfile = true`.
- Private profile chưa follow: nhận **403** — hiển thị CTA follow, không gọi lại posts.
- Owner edit flow: `status_filter=all` để hiện draft trên grid.
- Drill-down một post: `FR_ViewPostDetail`.
- Tham chiếu: `docs/feature_requirements/social/FR_ViewUserPosts.md`, `docs/engineering_rules/frontend-api-integration.md`.
