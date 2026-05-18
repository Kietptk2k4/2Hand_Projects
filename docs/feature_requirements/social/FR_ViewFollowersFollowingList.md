# Functional Requirement (FR) - View Followers Following List

## 1. Feature Overview
Cho phep xem danh sach followers va following cua mot user theo phan trang.

## 2. Actors
- **User:** Nguoi yeu cau xem danh sach.

## 3. Scope
- **In Scope:**
  - Query `FOLLOWS` theo `followee_id` (followers) hoac `follower_id` (following).
  - Tra ve danh sach co pagination.
- **Out of Scope:**
  - Goi y follow de xuat.

## 4. API Contract
**Endpoint:** `GET /api/v1/social/users/{user_id}/relations?type=followers|following`  
**Auth:** Required (JWT)

## 5. Business Rules
- `type` bat buoc va chi chap nhan `followers` hoac `following`.
- Danh sach tra ve co gioi han page size.

## 6. Database Impact
- Read `FOLLOWS` theo index follower/followee.

## 7. Transaction
- Read-only flow.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- Query followers/following hop le -> tra ket qua dung type.
- Type sai -> 400.
