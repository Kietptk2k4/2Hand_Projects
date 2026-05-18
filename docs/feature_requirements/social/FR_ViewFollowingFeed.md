# Functional Requirement (FR) - View Following Feed

## 1. Feature Overview
Cho phep user xem feed tu nhung nguoi dang theo doi, ket hop visibility rules cua bai viet.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Lay danh sach followee tu `FOLLOWS`.
  - Query `POSTS` theo `author_id` va `status = ACTIVE`.
  - Ap dung visibility `PUBLIC`/`FOLLOWERS`.
- **Out of Scope:**
  - De-xep hang feed nang cao.

## 4. API Contract
**Endpoint:** `GET /api/v1/social/feed/following`  
**Auth:** Required (JWT)

## 5. Business Rules
- Chi lay quan he follow `ACCEPTED`.
- Post `FOLLOWERS` chi hien thi khi co relation hop le.

## 6. Database Impact
- Read `FOLLOWS`.
- Read `POSTS` theo index author/status/created_at.

## 7. Transaction
- Read-only flow.

## 8. Security
- Khong tra du lieu post vuot quyen xem cua user.

## 9. Acceptance Criteria
- User co follow relation hop le -> nhan following feed dung visibility.
- User chua follow ai -> ket qua rong hop le.
