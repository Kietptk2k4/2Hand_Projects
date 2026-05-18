# Functional Requirement (FR) - View Saved Posts

## 1. Feature Overview
Cho phep user xem danh sach bai viet da luu cua chinh minh.

## 2. Actors
- **User:** Chu tai khoan.

## 3. Scope
- **In Scope:**
  - Lay danh sach post da save theo `user_id`.
  - Ho tro pagination.
- **Out of Scope:**
  - Chia folder/label cho bai da save.

## 4. API Contract
**Endpoint:** `GET /api/v1/social/posts/saved`  
**Auth:** Required (JWT)

## 5. Business Rules
- Chi tra bai da save cua current user.
- Bo qua post da `DELETED` hoac khong con quyen xem.

## 6. Database Impact
- Read `POST_SAVES` theo `user_id`.
- Read `POSTS` theo danh sach `post_id`.

## 7. Transaction
- Read-only flow.

## 8. Security
- Ownership theo `user_id` trong token.

## 9. Acceptance Criteria
- User da save bai -> tra dung danh sach.
- User chua save bai nao -> tra mang rong.
