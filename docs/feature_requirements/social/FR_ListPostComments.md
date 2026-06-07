# Functional Requirement (FR) - List Post Comments

## 1. Feature Overview

Cho phep user xem danh sach comment tren mot post (top-level hoac reply theo `parent_comment_id`), co phan trang va ap dung rule visibility/post status.

## 2. Actors

- **User:** Nguoi dung da dang nhap.
- **System:** Doc `COMMENTS`, join thong tin author tu `user_projections`.

## 3. Scope

- **In Scope:**
  - Liet ke comment `ACTIVE` thuoc `post_id`.
  - Mac dinh chi top-level (`parent_comment_id = null`); ho tro filter reply theo `parent_comment_id`.
  - Phan trang (`page`, `size`).
  - Tra ve author summary (`display_name`, `avatar_url`) tu projection.
  - Tra ve `media[]` dinh kem tren moi comment (neu co).
- **Out of Scope:**
  - Tao/sua/xoa comment (`FR_CommentPost`, `FR_ReplyComment`, `FR_DeleteOwnComment`).
  - Nested tree load tat ca cap trong 1 request (MVP: FE goi them khi expand reply).

## 4. Preconditions

- User da dang nhap (JWT).
- Post ton tai, `status = ACTIVE` (hoac author dang xem post cua minh neu policy mo rong sau nay).
- Post thoa rule visibility voi viewer (`PUBLIC` / `FOLLOWERS` + follow hop le) — cung logic `FR_ViewPostDetail`.
- Neu `allow_comments = false`: van co the xem comment cu (read-only) hoac tra 403 — **MVP khuyen nghi:** van cho phep doc comment da ton tai; chan chi write.

## 5. API Contract

**Endpoint:** `GET /api/v1/social/posts/{postId}/comments`

**Auth:** Required (JWT)

**Path params:**

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `postId` | string | yes | MongoDB ObjectId (24 hex) |

**Query params:**

| Field | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | `0` | Trang (>= 0) |
| `size` | int | `20` | Kich thuoc trang (1–50) |
| `parent_comment_id` | string | null | Neu null/bo trong: chi top-level. Neu co: list reply cua comment cha |
| `sort` | string | `created_at_asc` | `created_at_asc` hoac `created_at_desc` |

**Response - 200 OK (envelope chuan):**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach binh luan thanh cong.",
  "data": {
    "items": [
      {
        "comment_id": "507f1f77bcf86cd799439012",
        "post_id": "507f1f77bcf86cd799439011",
        "parent_comment_id": null,
        "author": {
          "user_id": "uuid",
          "display_name": "User A",
          "avatar_url": "https://cdn.2hands.vn/avatars/..."
        },
        "content_text": "Hay qua!",
        "media": [],
        "like_count": 3,
        "reply_count": 1,
        "created_at": "2026-05-21T10:00:00Z",
        "updated_at": "2026-05-21T10:00:00Z"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "total_elements": 42,
      "total_pages": 3,
      "has_next": true
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 6. Business Rules

- Chi tra comment `status = ACTIVE`; bo qua `DELETED`.
- Post `DELETED` hoac khong ton tai → `404` (`SOCIAL-404`).
- Post khong duoc viewer xem (visibility) → `403` (`SOCIAL-403`).
- `parent_comment_id` neu co phai thuoc cung `post_id` va comment cha `ACTIVE`; sai → `404`.
- `reply_count` tren item top-level: so reply truc tiep (optional MVP) hoac lay tu counter da luu.
- Khong tra noi dung user `DELETED` trong projection (co the an author hoac hien "Tai khoan da xoa").
- Pagination sai (`page < 0`, `size > 50`) → `400` (`SOCIAL-400-PAGINATION`).

## 7. Database Impact

- **Read MongoDB `comments`:** filter `post_id`, `status`, `parent_comment_id`, sort `created_at`.
- **Read MongoDB `posts`:** verify ton tai + visibility.
- **Read MongoDB `user_projections`:** author display.
- **Read PostgreSQL (optional):** `comment_reactions` de flag `liked_by_me` neu MVP bat.

Index khuyen nghi: `idx_comments_post_parent_created` (`post_id`, `parent_comment_id`, `created_at`).

## 8. Transaction

- Read-only; khong `@Transactional` ghi.

## 9. Security

- JWT bat buoc.
- Khong expose comment `DELETED` cho user thuong.
- Moderator/Admin doc comment bi moderation qua Admin Service (ngoai scope FR nay).

## 10. Failure Cases

| HTTP | code | Tinh huong |
|------|------|------------|
| 400 | SOCIAL-400-PAGINATION | Tham so phan trang khong hop le |
| 401 | SOCIAL-401 | Thieu JWT |
| 403 | SOCIAL-403 | Khong du quyen xem post |
| 404 | SOCIAL-404 | Post/comment cha khong ton tai |

## 11. Acceptance Criteria

- **AC1:** User hop le xem duoc danh sach top-level comment cua post `ACTIVE` + visibility hop le.
- **AC2:** Filter `parent_comment_id` tra dung reply cua comment cha.
- **AC3:** Post khong ton tai → 404.
- **AC4:** Post khong duoc xem → 403.
- **AC5:** Comment da xoa khong xuat hien trong list.

## 12. Related

| FR / Tai lieu | Muc dich |
|---------------|----------|
| `FR_CommentPost` | Tao comment top-level |
| `FR_ReplyComment` | Tao reply |
| `FR_ViewPostDetail` | Xem post truoc khi vao man comment |
| `docs/database/social-schema.md` | Schema `COMMENTS` |
| `docs/business-spec/social-service-spec.md` | Phan Comment Management |

## 13. FE Integration Notes

- `CommentItem.jsx` hien thi `media` qua `CommentMediaDisplay.jsx`.
- Top-level comment trong `PostDetailModal` dung `CommentComposer` (co media).
- Tham chieu: `docs/api_fe_behavior/social_api_fe_behavior/ListPostComments-api-and-behavior.md`
