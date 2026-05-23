# Functional Requirement (FR) - View User Posts

## 1. Feature Overview

Cho phep user xem danh sach bai viet cua mot tac gia (profile grid / timeline ca nhan), co phan trang va ap dung privacy + visibility giong feed.

## 2. Actors

- **User:** Nguoi dang xem (viewer).
- **Profile owner:** User duoc xem (`userId` trong path).

## 3. Scope

- **In Scope:**
  - Query `POSTS` theo `author_id`.
  - Phan trang `page`, `size`.
  - Rule visibility + private profile (`user_projections.is_private`).
  - Owner xem them post `DRAFT` cua chinh minh.
- **Out of Scope:**
  - Sua/xoa post.
  - Follow action (`FR_FollowUser`).
  - Commerce product detail.

## 4. Preconditions

- Viewer da dang nhap.
- Target user co ban ghi trong `user_projections` (eventual consistency neu moi tao).

## 5. API Contract

**Endpoint:** `GET /api/v1/social/users/{userId}/posts`

**Auth:** Required (JWT)

**Path params:**

| Field | Type | Required |
|-------|------|----------|
| `userId` | UUID | yes |

**Query params:**

| Field | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | `0` | >= 0 |
| `size` | int | `20` | 1–50 |
| `status_filter` | string | `published` | `published` = chi `ACTIVE`; `all` = owner only: `ACTIVE` + `DRAFT` |

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach bai viet thanh cong.",
  "data": {
    "items": [
      {
        "post_id": "507f1f77bcf86cd799439011",
        "caption": "Preview caption",
        "media": [{ "url": "...", "type": "IMAGE" }],
        "visibility": "PUBLIC",
        "like_count": 5,
        "reply_count": 1,
        "hashtags": [],
        "created_at": "2026-05-21T09:00:00Z"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "total_elements": 15,
      "total_pages": 1,
      "has_next": false
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 6. Business Rules

### 6.1 Private profile

- Neu `is_private = true` va viewer **khong phai** owner:
  - Chi xem duoc neu co quan he follow `ACCEPTED` (giong `FR_ViewSocialProfile`).
  - Khong follow → `403` hoac tra list rong tuy product policy — **MVP khuyen nghi:** `403` voi message ro rang.

### 6.2 Visibility tren tung post

- `PUBLIC`: hien voi viewer duoc phep xem profile.
- `FOLLOWERS`: chi hien khi viewer follow `ACCEPTED` hoac la owner.
- Post `DELETED` hoac bi moderation hide/remove: khong hien.

### 6.3 Owner vs viewer

| Viewer | status_filter | Post tra ve |
|--------|---------------|-------------|
| Owner | `published` | `ACTIVE` only |
| Owner | `all` | `ACTIVE` + `DRAFT` |
| Khac | `published` | `ACTIVE` thoa visibility |
| Khac | `all` | `400` — chi owner dung filter `all` |

### 6.4 Target user status

- `user_projections.status = DELETED` → `404`.
- `SUSPENDED`: van cho phep **doc** post cu (read); write bi chan boi `FR_EnforceUserStatusOnWrite`.

## 7. Database Impact

- **Read `posts`:** index `author_id + status + created_at` (`idx_posts_author_status_created_desc`).
- **Read `user_projections`**, **`FOLLOWS`** cho private/followers rules.

## 8. Transaction

- Read-only.

## 9. Security

- JWT bat buoc.
- Khong lo post `DRAFT` cua nguoi khac.
- `userId` path la UUID, khong phai ObjectId.

## 10. Failure Cases

| HTTP | code | Tinh huong |
|------|------|------------|
| 400 | SOCIAL-400-PAGINATION | Pagination / status_filter sai |
| 401 | SOCIAL-401 | Thieu JWT |
| 403 | SOCIAL-403 | Private profile, chua follow |
| 404 | SOCIAL-404 | User khong ton tai trong projection |

## 11. Acceptance Criteria

- **AC1:** Viewer hop le thay grid post `ACTIVE` cua user public profile.
- **AC2:** Private profile chi tra post khi da follow accepted.
- **AC3:** Owner dung `status_filter=all` thay ca draft.
- **AC4:** Post `FOLLOWERS` chi hien cho follower/owner.
- **AC5:** User khong ton tai → 404.

## 12. Related

| FR / Tai lieu | Muc dich |
|---------------|----------|
| `FR_ViewSocialProfile` | Thong tin profile + counts |
| `FR_ViewPostDetail` | Drill-down 1 post |
| `FR_ConsumeAuthUserEvents` | Dong bo `is_private`, status |
| `docs/business-spec/social-service-spec.md` | Social Graph - profile |

## 13. Implementation Notes (hien trang)

- Endpoint **chua co**; `PostRepository` da co query theo author cho feed/search — can use case rieng cho profile timeline.
