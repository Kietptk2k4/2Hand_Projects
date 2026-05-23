# Functional Requirement (FR) - View Post Detail

## 1. Feature Overview

Cho phep user xem chi tiet mot bai viet (caption, media, hashtags, counters, author, trang thai tuong tac cua viewer) de phuc vu man hinh post detail, deep link va share.

## 2. Actors

- **User:** Nguoi dung da dang nhap.
- **System:** Doc `POSTS`, `user_projections`, va trang thai like/save cua viewer.

## 3. Scope

- **In Scope:**
  - Tra ve day du noi dung post hop le voi viewer.
  - Ap dung visibility (`PUBLIC`, `FOLLOWERS`).
  - Flags `liked_by_me`, `saved_by_me` (neu da co bang tuong tac).
  - Author summary tu projection.
- **Out of Scope:**
  - Danh sach comment (`FR_ListPostComments`).
  - Feed list (`FR_ViewGlobalFeed`, `FR_ViewFollowingFeed`).
  - Moderation admin UI.

## 4. Preconditions

- JWT hop le.
- `postId` la ObjectId hop le.
- Post ton tai va khong bi moderation remove khoi view policy.

## 5. API Contract

**Endpoint:** `GET /api/v1/social/posts/{postId}`

**Auth:** Required (JWT)

**Path params:**

| Field | Type | Required |
|-------|------|----------|
| `postId` | string (ObjectId) | yes |

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay chi tiet bai viet thanh cong.",
  "data": {
    "post_id": "507f1f77bcf86cd799439011",
    "author": {
      "user_id": "uuid",
      "display_name": "User A",
      "avatar_url": "https://cdn.2hands.vn/avatars/..."
    },
    "caption": "Bai viet mau",
    "media": [{ "url": "https://cdn.2hands.vn/social/posts/...", "type": "IMAGE" }],
    "product_tags": [{ "product_id": "uuid", "price": 199000 }],
    "visibility": "PUBLIC",
    "status": "ACTIVE",
    "like_count": 10,
    "reply_count": 2,
    "hashtags": ["thoitrang"],
    "allow_comments": true,
    "liked_by_me": false,
    "saved_by_me": true,
    "is_owner": false,
    "created_at": "2026-05-21T09:00:00Z",
    "updated_at": "2026-05-21T09:30:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 6. Business Rules

### 6.1 Visibility & status

| Post status | Viewer | Ket qua |
|-------------|--------|---------|
| `ACTIVE` + `PUBLIC` | Bat ky user dang nhap | 200 |
| `ACTIVE` + `FOLLOWERS` | Follower `ACCEPTED` hoac author | 200 |
| `ACTIVE` + `FOLLOWERS` | Khong follow | 403 |
| `DRAFT` | Author | 200 |
| `DRAFT` | Khac author | 404 (khong lo ton tai) |
| `DELETED` | Khong phai admin | 404 |
| Hidden by moderation (`FR_HandlePostModeratedEvent` HIDE) | User thuong | 404 hoac 403 theo policy |

### 6.2 Engagement flags

- `liked_by_me`: ton tai record `POST_LIKES(post_id, viewer_id)`.
- `saved_by_me`: ton tai record `POST_SAVES(post_id, viewer_id)`.
- `is_owner`: `author_id == viewer_id` tu JWT.

### 6.3 Product tags

- Chi tra ve tags da luu; khong goi Commerce DB truc tiep trong request read.

## 7. Database Impact

- **Read `posts` (MongoDB)** by `_id`.
- **Read `user_projections`** by `author_id`.
- **Read `POST_LIKES`, `POST_SAVES` (PostgreSQL)** cho viewer.
- **Read `FOLLOWS`** khi `visibility = FOLLOWERS`.

## 8. Transaction

- Read-only.

## 9. Security

- JWT bat buoc; `viewer_id` tu token.
- Khong tra post `DELETED`/draft cua nguoi khac (anti-enumeration: uu tien 404).
- Khong log URL media nhay cam day du o DEBUG production.

## 10. Failure Cases

| HTTP | code | Tinh huong |
|------|------|------------|
| 400 | SOCIAL-400 | `postId` khong phai ObjectId |
| 401 | SOCIAL-401 | Thieu JWT |
| 403 | SOCIAL-403 | Post FOLLOWERS, viewer khong follow |
| 404 | SOCIAL-404 | Post khong ton tai / khong duoc xem |

## 11. Acceptance Criteria

- **AC1:** User xem duoc post `PUBLIC` `ACTIVE`.
- **AC2:** User follow hop le xem duoc post `FOLLOWERS`.
- **AC3:** User khong follow khong xem duoc post `FOLLOWERS` → 403.
- **AC4:** Author xem duoc post `DRAFT` cua minh.
- **AC5:** `liked_by_me` / `saved_by_me` phan anh dung trang thai DB.

## 12. Related

| FR / Tai lieu | Muc dich |
|---------------|----------|
| `FR_ListPostComments` | Binh luan tren post |
| `FR_ViewUserPosts` | Grid bai viet tren profile |
| `FR_LikePost`, `FR_SavePost` | Tuong tac |
| `FR_HandlePostModeratedEvent` | An post sau moderation |
| `docs/business-spec/social-service-spec.md` | Post Management |

## 13. Implementation Notes (hien trang)

- Endpoint `GET /api/v1/social/posts/{postId}` **chua co** trong `social-service`.
- Co the reuse mapper field tu `ViewGlobalFeedResponse.PostItemResponse` + bo sung author/flags.
