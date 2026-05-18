# Functional Requirement (FR) - Search Hashtag

## 1. Feature Overview
Cho phep user tim kiem post theo hashtag.

## 2. Actors
- **User:** Nguoi dung da dang nhap.

## 3. Scope
- **In Scope:**
  - Query `POSTS.hashtags`.
  - Tra ket qua phan trang theo visibility rule.
- **Out of Scope:**
  - Trend ranking hashtag.

## 4. API Contract
**Endpoint:** `GET /api/v1/social/search/hashtags/{hashtag}`  
**Auth:** Required (JWT)

## 5. Business Rules
- Hashtag can duoc normalize (bo dau # neu can) theo convention.
- Khong tra post `DRAFT`/`DELETED`.

## 6. Database Impact
- Read `POSTS` theo index hashtags.

## 7. Transaction
- Read-only flow.

## 8. Security
- JWT bat buoc.
- Filter visibility bat buoc.

## 9. Acceptance Criteria
- Hashtag hop le -> tra danh sach post dung.
- Hashtag sai dinh dang -> 400.
