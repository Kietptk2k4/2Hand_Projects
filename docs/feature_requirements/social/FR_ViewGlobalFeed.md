# Functional Requirement (FR) - View Global Feed

## 1. Feature Overview
Cho phep user xem feed cong khai toan he thong, sap xep theo bai viet moi nhat.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Query post `ACTIVE`, `visibility = PUBLIC`.
  - Ho tro pagination.
- **Out of Scope:**
  - Ranking machine-learning.

## 4. API Contract
**Endpoint:** `GET /api/v1/social/feed/global`  
**Auth:** Required (JWT)

## 5. Business Rules
- Khong hien thi post `DRAFT`/`DELETED`.
- Co the ap dung pagination cursor/offset theo implementation.

## 6. Database Impact
- Read `POSTS` theo index `status, visibility, created_at`.

## 7. Transaction
- Read-only flow, khong transaction ghi.

## 8. Security
- JWT bat buoc.
- Filter du lieu theo policy visibility.

## 9. Acceptance Criteria
- Request hop le -> tra danh sach post cong khai phan trang.
- Tham so pagination sai -> 400.
