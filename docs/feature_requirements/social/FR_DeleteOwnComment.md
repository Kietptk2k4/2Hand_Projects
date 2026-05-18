# Functional Requirement (FR) - Delete Own Comment

## 1. Feature Overview
Cho phep user xoa mem comment cua chinh minh.

## 2. Actors
- **User:** Tac gia comment.

## 3. Scope
- **In Scope:**
  - Soft delete comment (`status = DELETED`).
  - Cap nhat counter lien quan.
- **Out of Scope:**
  - Hard delete comment.

## 4. API Contract
**Endpoint:** `DELETE /api/v1/social/comments/{comment_id}`  
**Auth:** Required (JWT)

## 5. Business Rules
- User chi xoa duoc comment cua minh (tru moderator flow).
- Comment da xoa xu ly idempotent theo policy.

## 6. Database Impact
- `COMMENTS`: update `status`, `updated_at`, `deleted_at` (neu co).
- Counter lien quan: update.

## 7. Transaction
- Soft delete + cap nhat counter can thuc hien nhat quan.

## 8. Security
- Ownership check bat buoc.

## 9. Acceptance Criteria
- Tac gia xoa comment thanh cong -> 200.
- Khong phai tac gia -> 403.
