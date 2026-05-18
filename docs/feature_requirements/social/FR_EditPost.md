# Functional Requirement (FR) - Edit Post

## 1. Feature Overview
Cho phep tac gia cap nhat noi dung bai viet da tao nhu caption, media, hashtags, visibility, allow_comments va productTags.

## 2. Actors
- **User:** Tac gia bai viet.

## 3. Scope
- **In Scope:**
  - Cap nhat post trong `POSTS`.
  - Cap nhat `updated_at`.
- **Out of Scope:**
  - Chuyen doi quyen so huu bai viet.

## 4. API Contract
**Endpoint:** `PUT /api/v1/social/posts/{post_id}`  
**Auth:** Required (JWT)

## 5. Business Rules
- Chi `author_id` moi duoc sua post.
- Post `DELETED` khong duoc sua.
- Field khong gui len se giu nguyen theo patch policy.

## 6. Database Impact
- `POSTS`: update noi dung va `updated_at`.

## 7. Transaction
- Update post trong mot lan ghi du lieu nhat quan.

## 8. Security
- Ownership check bat buoc.
- Validate input de tranh script injection trong caption.

## 9. Acceptance Criteria
- Tac gia sua post thanh cong -> 200.
- Khong phai tac gia -> 403.
- Post khong ton tai -> 404.
