# Functional Requirement (FR) - Reply Comment

## 1. Feature Overview
Cho phep user tra loi mot comment da ton tai de tao thread hoi thoai.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Tao comment moi voi `parent_comment_id`.
  - Dinh kem media (toi da 5, `IMAGE`/`VIDEO`) trong payload.
  - Cap nhat bo dem lien quan.
  - Publish `COMMENT_CREATED`.
- **Out of Scope:**
  - Thread depth vo han.

## 4. API Contract
**Endpoint:** `POST /api/v1/social/comments/{comment_id}/replies`  
**Auth:** Required (JWT)

## 5. Business Rules
- Parent comment phai ton tai va `ACTIVE`.
- Gioi han do sau thread theo policy MVP.

## 6. Database Impact
- `COMMENTS`: insert reply.
- `POSTS.reply_count`/counter lien quan: update.
- `OUTBOX_EVENTS`: insert `COMMENT_CREATED`.

## 7. Transaction
- Tao reply + cap nhat counter + outbox can nhat quan.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- Reply hop le -> 201.
- Parent comment khong ton tai/da xoa -> 404.
- FE: reply inline dung `CommentComposer` voi cung luong upload media nhu top-level comment.

## 10. Related
- API doc: `docs/api_fe_behavior/social_api_fe_behavior/ReplyComment-api-and-behavior.md`
- FE: `CommentItem.jsx`, `CommentComposer.jsx`, `usePostComments.js`
