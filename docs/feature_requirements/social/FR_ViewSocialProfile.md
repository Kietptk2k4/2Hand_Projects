# Functional Requirement (FR) - View Social Profile

## 1. Feature Overview
Cho phep user xem thong tin social profile cua user khac, gom thong tin cong khai va social counters.

## 2. Actors
- **User:** Nguoi yeu cau xem profile.

## 3. Scope
- **In Scope:**
  - Lay du lieu profile tu projection local cua Social Service.
  - Ap dung privacy rule.
- **Out of Scope:**
  - Cap nhat profile (thuoc Auth Service).

## 4. API Contract
**Endpoint:** `GET /api/v1/social/users/{user_id}/profile`  
**Auth:** Required (JWT)

## 5. Business Rules
- Chi hien thi thong tin theo policy privacy.
- Profile user da xoa/khong ton tai -> 404.

## 6. Database Impact
- Read projection profile local.
- Read counts tu social graph theo policy.

## 7. Transaction
- Read-only flow.

## 8. Security
- JWT bat buoc.

## 9. Acceptance Criteria
- User ton tai -> tra profile hop le.
- User khong ton tai -> 404.
