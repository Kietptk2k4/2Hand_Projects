# Functional Requirement (FR) - Tag Product In Post

## 1. Feature Overview
Cho phep user gan san pham vao bai viet thong qua `productTags` de lien ket noi dung social voi commerce context.

## 2. Actors
- **User:** Tac gia post.

## 3. Scope
- **In Scope:**
  - Luu `productTags` khi tao/sua post.
  - Validate dinh dang `product_id` va gia tri display.
- **Out of Scope:**
  - Truy van truc tiep DB cua Commerce Service.

## 4. API Contract
**Endpoint:** `POST /api/v1/social/posts` va `PUT /api/v1/social/posts/{post_id}` (payload co `productTags`)  
**Auth:** Required (JWT)

## 5. Business Rules
- `productTags` la danh sach object `{product_id, price}`.
- `product_id` phai hop le dinh dang UUID.
- Neu product khong con hop le o Commerce, viec kiem tra theo integration contract/API.

## 6. Database Impact
- `POSTS.productTags`: insert/update danh sach tag san pham.

## 7. Transaction
- Product tag duoc luu cung transaction voi thao tac create/edit post.

## 8. Security
- Khong cho user chen du lieu vuot schema cho `productTags`.

## 9. Acceptance Criteria
- Payload tag hop le -> luu thanh cong.
- Payload tag sai format -> 400.
