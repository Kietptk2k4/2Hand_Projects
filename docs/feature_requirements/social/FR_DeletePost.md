# Functional Requirement (FR) - Delete Post

## 1. Feature Overview
Cho phep xoa bai viet theo co che soft delete de bao toan audit trail va dam bao nhat quan lien service.

## 2. Actors
- **User:** Tac gia bai viet.
- **Admin/Moderator:** Actor co quyen moderation.

## 3. Scope
- **In Scope:**
  - Dat `status = DELETED`.
  - Set `deleted_at`, `updated_at`.
- **Out of Scope:**
  - Hard delete du lieu vat ly.

## 4. API Contract
**Endpoint:** `DELETE /api/v1/social/posts/{post_id}`  
**Auth:** Required (JWT)

## 5. Business Rules
- Chi tac gia hoac moderator moi duoc xoa.
- Post da `DELETED` xu ly idempotent.

## 6. Database Impact
- `POSTS`: update trang thai xoa mem.

## 7. Transaction
- Soft delete post duoc thuc hien atomically.

## 8. Security
- Ownership/RBAC check bat buoc.

## 9. Acceptance Criteria
- Xoa thanh cong -> 200.
- Khong co quyen -> 403.
- Post khong ton tai -> 404.
