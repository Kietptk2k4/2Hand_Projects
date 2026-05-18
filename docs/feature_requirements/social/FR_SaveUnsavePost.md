# Functional Requirement (FR) - Save Unsave Post

## 1. Feature Overview
Cho phep user luu hoac bo luu bai viet de xem lai sau.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Insert/Delete `POST_SAVES`.
  - Xu ly idempotent.
- **Out of Scope:**
  - Chia thu muc save theo collection.

## 4. API Contract
**Endpoint:** `POST /api/v1/social/posts/{post_id}/save` (toggle hoac explicit)  
**Auth:** Required (JWT)

## 5. Business Rules
- Unique `(post_id, user_id)` dam bao khong luu trung.
- Bo luu khong ton tai van tra ket qua thanh cong theo idempotency policy.

## 6. Database Impact
- `POST_SAVES`: insert/delete mapping user-post.

## 7. Transaction
- Luu/bo luu trong transaction relation write.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- Save thanh cong -> tao mapping.
- Unsave thanh cong -> xoa mapping.
