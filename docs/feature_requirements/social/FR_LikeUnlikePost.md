# Functional Requirement (FR) - Like Unlike Post

## 1. Feature Overview
Cho phep user like hoac unlike post, dong bo trang thai relation va bo dem like.

## 2. Actors
- **User:** Nguoi thuc hien like/unlike.

## 3. Scope
- **In Scope:**
  - Insert/Delete `POST_LIKES`.
  - Cap nhat `POSTS.like_count`.
  - Publish event `POST_LIKED` khi like thanh cong.
- **Out of Scope:**
  - He thong reaction da dang (haha, wow, ...).

## 4. API Contract
**Endpoint:** `POST /api/v1/social/posts/{post_id}/like` (toggle hoac explicit like/unlike theo API policy)  
**Auth:** Required (JWT)

## 5. Business Rules
- Unique `(post_id, user_id)` dam bao 1 user like toi da 1 lan.
- Unlike tren relation khong ton tai xu ly idempotent.

## 6. Database Impact
- `POST_LIKES`: insert/delete.
- `POSTS.like_count`: increment/decrement.
- `OUTBOX_EVENTS`: insert `POST_LIKED` cho hanh dong like.

## 7. Transaction
- Update relation + counter + outbox (neu like) can thuc hien nhat quan.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- Like thanh cong -> relation duoc tao, counter tang.
- Unlike thanh cong -> relation bi xoa, counter giam.
- User bam so like -> xem danh sach likers (`FR_ViewPostLikers`).

## 10. Related
- `FR_ViewPostLikers` — `GET /api/v1/social/posts/{postId}/likes`
- API doc: `docs/api_fe_behavior/social_api_fe_behavior/LikeUnlikePost-api-and-behavior.md`
