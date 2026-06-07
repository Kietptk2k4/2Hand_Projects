# Functional Requirement (FR) - Like Comment

## 1. Feature Overview
Cho phep user like comment de the hien tuong tac voi noi dung hoi thoai.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Tao/xoa relation like comment.
  - Cap nhat `COMMENTS.like_count`.
- **Out of Scope:**
  - Da reaction type tren comment.

## 4. API Contract
**Endpoint:** `POST /api/v1/social/comments/{comment_id}/like`  
**Auth:** Required (JWT)

## 5. Business Rules
- 1 user chi like toi da 1 lan tren 1 comment.
- Unlike xu ly idempotent neu relation khong ton tai.

## 6. Database Impact
- `COMMENT_REACTION`: insert/delete `(comment_id, user_id)`.
- `COMMENTS.like_count`: increment/decrement.

## 7. Transaction
- Cap nhat relation + counter can nhat quan.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- Like comment hop le -> 200.
- Comment khong ton tai -> 404.
- User bam so like tren comment -> xem danh sach likers (`FR_ViewCommentLikers`).

## 10. Related
- `FR_ViewCommentLikers` — `GET /api/v1/social/comments/{commentId}/likes`
- API doc: `docs/api_fe_behavior/social_api_fe_behavior/LikeComment-api-and-behavior.md`
