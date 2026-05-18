# Functional Requirement (FR) - Create Post

## 1. Feature Overview
Cho phep user tao bai viet moi trong Social Service voi caption, media, hashtags, visibility, allow_comments, va productTags.

## 2. Actors
- **User:** Nguoi dung da dang nhap va co quyen tao bai viet.

## 3. Scope
- **In Scope:**
  - Tao document moi trong `POSTS`.
  - Ho tro trang thai khoi tao `DRAFT` hoac `ACTIVE`.
  - Validate payload caption/media/hashtags/productTags.
- **Out of Scope:**
  - Feed ranking nang cao.
  - Moderation workflow chi tiet.

## 4. API Contract
**Endpoint:** `POST /api/v1/social/posts`  
**Auth:** Required (JWT)

## 5. Business Rules
- User `SUSPENDED`/`DELETED` khong duoc tao post.
- `visibility` chi nhan `PUBLIC` hoac `FOLLOWERS`.
- `productTags[].product_id` duoc validate dinh dang; khong truy cap truc tiep DB Commerce.

## 6. Database Impact
- `POSTS`: insert moi (`author_id`, `caption`, `media`, `productTags`, `status`, `visibility`, `allow_comments`, `hashtags`, timestamps).

## 7. Transaction
- Create post duoc commit atomically tren MongoDB write boundary.

## 8. Security
- JWT bat buoc.
- Ownership gan voi `author_id` duoc lay tu token, khong tin payload user id.

## 9. Acceptance Criteria
- Tao post hop le -> 201, du lieu luu trong `POSTS`.
- Payload sai -> 400.
- User khong hop le -> 403.
