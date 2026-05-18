# Functional Requirement (FR) - Comment Post

## 1. Feature Overview
Cho phep user tao comment tren post dang hoat dong.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Tao comment moi trong `COMMENTS`.
  - Tang `POSTS.reply_count`.
  - Publish `COMMENT_CREATED`.
- **Out of Scope:**
  - Moderation keyword nang cao theo AI.

## 4. API Contract
**Endpoint:** `POST /api/v1/social/posts/{post_id}/comments`  
**Auth:** Required (JWT)

## 5. Business Rules
- Post phai `ACTIVE`.
- `allow_comments = true`.
- Comment top-level co `parent_comment_id = null`.

## 6. Database Impact
- `COMMENTS`: insert comment moi.
- `POSTS.reply_count`: increment.
- `OUTBOX_EVENTS`: insert `COMMENT_CREATED`.

## 7. Transaction
- Tao comment + update counter + outbox can nhat quan.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- Comment hop le -> 201.
- Post tat comment -> 403.
- Post khong ton tai -> 404.
