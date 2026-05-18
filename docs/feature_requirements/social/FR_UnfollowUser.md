# Functional Requirement (FR) - Unfollow User

## 1. Feature Overview
Cho phep user huy quan he theo doi voi user khac.

## 2. Actors
- **User:** Nguoi dang follow.

## 3. Scope
- **In Scope:**
  - Xoa relation trong `FOLLOWS`.
  - Xu ly idempotent.
- **Out of Scope:**
  - Event notification unfollow bat buoc.

## 4. API Contract
**Endpoint:** `DELETE /api/v1/social/users/{user_id}/follow`  
**Auth:** Required (JWT)

## 5. Business Rules
- Unfollow relation khong ton tai van co the tra ket qua thanh cong theo policy idempotent.

## 6. Database Impact
- `FOLLOWS`: delete relation `(follower_id, followee_id)`.

## 7. Transaction
- Xoa relation trong transaction write.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- Unfollow hop le -> 200.
- Goi lap lai unfollow -> van xu ly an toan.
