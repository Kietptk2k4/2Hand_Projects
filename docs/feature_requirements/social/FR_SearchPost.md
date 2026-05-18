# Functional Requirement (FR) - Search Post

## 1. Feature Overview
Cho phep user tim kiem post don gian theo tu khoa.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Search theo caption/metadata/hashtags.
  - Filter theo `status = ACTIVE` va visibility.
  - Co the luu search history.
- **Out of Scope:**
  - Search semantic nang cao.

## 4. API Contract
**Endpoint:** `GET /api/v1/social/search/posts?q={keyword}`  
**Auth:** Required (JWT)

## 5. Business Rules
- Keyword rong khong hop le.
- Ket qua phai ton trong visibility va follow relation.

## 6. Database Impact
- Read `POSTS`.
- Best-effort write `SEARCH_HISTORY` (neu bat).

## 7. Transaction
- Search la read-only; ghi search history la best-effort, khong fail request chinh.

## 8. Security
- JWT bat buoc.
- Filter ket qua theo quyen truy cap.

## 9. Acceptance Criteria
- Keyword hop le -> tra ket qua phan trang.
- Keyword khong hop le -> 400.
