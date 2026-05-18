# UC - Post Management

## 1. Overview
Use Case nay mo ta vong doi bai viet va binh luan trong Social Service, bao gom tao/sua/xoa post, gan product tag, va quan ly thread comment theo mo hinh soft delete. Du lieu noi dung duoc luu o MongoDB, dong bo su kien qua Outbox de dam bao eventual consistency giua cac service.

## 2. Actors
* **User:** Tao, sua, xoa post cua minh; comment/reply tren post hop le.
* **Admin/Moderator:** Xu ly xoa/hide noi dung vi pham theo luong moderation.
* **System:** Worker cap nhat counters va publish outbox events.

## 3. Sub-Use Cases

### 3.1. Tao post (Create Post)
* **Pre-conditions:** User da dang nhap, khong o trang thai `SUSPENDED`/`DELETED` theo du lieu dong bo tu Auth.
* **Main Flow:**
  1. User gui payload post: `caption`, `media`, `productTags`, `visibility`, `allow_comments`, `hashtags`.
  2. He thong validate do dai caption, media format, va cau truc `productTags`.
  3. He thong tao document trong `POSTS` voi `status = DRAFT` hoac `ACTIVE` (tuy action publish).
  4. He thong set cac truong metadata: `author_id`, `created_at`, `updated_at`, `like_count`, `reply_count`.
* **Exception Flow:**
  * Payload khong hop le -> Bao loi 400.
  * User bi khoa/bi xoa -> Bao loi 403.
  * Loi DB -> Bao loi 500.
* **Post-conditions:** Post moi duoc luu trong `POSTS`; co the tao outbox event `POST_CREATED` khi can tich hop.

### 3.2. Sua post (Edit Post)
* **Pre-conditions:** Post ton tai; user la `author_id`; post chua o `DELETED`.
* **Main Flow:**
  1. User gui thong tin can cap nhat.
  2. He thong kiem tra quyen so huu post.
  3. He thong cap nhat noi dung va `updated_at`.
* **Exception Flow:**
  * Khong phai tac gia -> Bao loi 403.
  * Post khong ton tai -> Bao loi 404.
* **Post-conditions:** Du lieu post duoc cap nhat; co the ghi `POST_UPDATED` vao `OUTBOX_EVENTS`.

### 3.3. Xoa post (Soft Delete Post)
* **Pre-conditions:** Post ton tai; actor co quyen xoa (author hoac moderator).
* **Main Flow:**
  1. He thong cap nhat `POSTS.status = DELETED`.
  2. He thong set `deleted_at` va `updated_at`.
  3. He thong loai post khoi ket qua feed/search.
* **Exception Flow:** Khong tim thay post -> Bao loi 404.
* **Post-conditions:** Post duoc xoa mem, khong xoa vat ly trong MVP.

### 3.4. Gan san pham vao post (Tag Product)
* **Pre-conditions:** User tao/sua post; payload `productTags` hop le.
* **Main Flow:**
  1. User chon san pham va gia tri hien thi tren post.
  2. He thong luu `productTags[]` trong document `POSTS`.
  3. He thong khong truy cap truc tiep DB Commerce, chi xu ly theo integration contract/API.
* **Exception Flow:** `product_id` sai dinh dang -> Bao loi 400.
* **Post-conditions:** Post luu danh sach product tags de FE render.

### 3.5. Tao comment tren post (Create Comment)
* **Pre-conditions:** Post `ACTIVE`; `allow_comments = true`; user hop le.
* **Main Flow:**
  1. User gui noi dung comment.
  2. He thong tao document moi trong `COMMENTS` voi `parent_comment_id = null`, `status = ACTIVE`.
  3. He thong cap nhat bo dem `reply_count` cua post.
* **Exception Flow:** Post tat comment/da xoa -> Bao loi 403 hoac 404.
* **Post-conditions:** Comment moi duoc tao; co the publish `COMMENT_CREATED`.

### 3.6. Reply comment
* **Pre-conditions:** Comment cha ton tai va `status = ACTIVE`.
* **Main Flow:**
  1. User gui noi dung reply.
  2. He thong tao `COMMENTS` moi voi `parent_comment_id` tro den comment cha.
  3. He thong cap nhat bo dem lien quan.
* **Exception Flow:** Comment cha khong ton tai/da xoa -> Bao loi 404.
* **Post-conditions:** Thread comment duoc mo rong theo quy dinh do sau MVP.

### 3.7. Xoa comment cua minh (Delete Own Comment)
* **Pre-conditions:** Comment ton tai; user la author hoac moderator co quyen.
* **Main Flow:**
  1. He thong set `COMMENTS.status = DELETED`.
  2. He thong set `deleted_at` (neu co) va `updated_at`.
  3. He thong cap nhat counters lien quan.
* **Exception Flow:** Khong co quyen -> Bao loi 403.
* **Post-conditions:** Comment bi xoa mem; co the ghi `COMMENT_DELETED` vao outbox khi can.
