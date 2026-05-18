# UC - Engagement

## 1. Overview
Use Case nay quan ly cac tuong tac chinh cua user tren Social Service gom like/unlike post, save/unsave post, xem danh sach bai da luu, comment/reply, like comment va xoa comment cua chinh minh. Cac quan he tuong tac duoc luu o PostgreSQL, trong khi noi dung post/comment duoc luu o MongoDB.

## 2. Actors
* **User:** Thuc hien cac hanh dong tuong tac.
* **System:** Dam bao idempotency, cap nhat counters, va publish event khi can.

## 3. Sub-Use Cases

### 3.1. Like/Unlike post
* **Pre-conditions:** Post ton tai, `status = ACTIVE`.
* **Main Flow:**
  1. User gui hanh dong like.
  2. He thong insert `POST_LIKES(post_id, user_id)` neu chua ton tai.
  3. He thong cap nhat `POSTS.like_count` theo co che eventual consistency.
  4. Khi unlike, he thong xoa record trong `POST_LIKES` va cap nhat counter.
* **Exception Flow:**
  * Post khong ton tai -> Bao loi 404.
  * Vi pham unique do request lap -> Xu ly idempotent.
* **Post-conditions:** Trang thai like cua user duoc cap nhat dung; co the phat event `POST_LIKED` khi like thanh cong.

### 3.2. Save/Unsave post
* **Pre-conditions:** User da dang nhap; post ton tai.
* **Main Flow:**
  1. Save: He thong insert vao `POST_SAVES(post_id, user_id)`.
  2. Unsave: He thong xoa ban ghi trong `POST_SAVES`.
* **Exception Flow:** Vi pham unique hoac thao tac lap -> xu ly idempotent.
* **Post-conditions:** Danh sach saved posts cua user duoc cap nhat.

### 3.3. Xem danh sach bai da luu (View Saved Posts)
* **Pre-conditions:** User da dang nhap.
* **Main Flow:**
  1. He thong query danh sach `post_id` tu `POST_SAVES` theo `user_id`.
  2. He thong map sang `POSTS` de lay thong tin post.
  3. He thong bo qua post khong con hien thi (`DELETED`).
* **Exception Flow:** Loi truy van/phan trang -> Bao loi 500.
* **Post-conditions:** User xem duoc danh sach post da luu hop le.

### 3.4. Comment post
* **Pre-conditions:** Post `ACTIVE`; `allow_comments = true`.
* **Main Flow:**
  1. User gui noi dung comment.
  2. He thong tao record trong `COMMENTS` voi `parent_comment_id = null`.
  3. He thong cap nhat counter lien quan.
* **Exception Flow:** Post da xoa/khong cho comment -> Bao loi 403 hoac 404.
* **Post-conditions:** Comment moi duoc tao; co the phat `COMMENT_CREATED` qua outbox.

### 3.5. Reply comment
* **Pre-conditions:** Comment cha ton tai va `status = ACTIVE`.
* **Main Flow:**
  1. User gui noi dung reply.
  2. He thong tao comment moi voi `parent_comment_id` tro den comment cha.
  3. He thong cap nhat bo dem.
* **Exception Flow:** Parent comment khong ton tai -> Bao loi 404.
* **Post-conditions:** Thread comment duoc cap nhat.

### 3.6. Like comment
* **Pre-conditions:** Comment ton tai va `status = ACTIVE`.
* **Main Flow:**
  1. User gui hanh dong like comment.
  2. He thong tao/xoa record trong `COMMENT_REACTION` theo `(comment_id, user_id)`.
  3. He thong cap nhat `COMMENTS.like_count`.
* **Exception Flow:** Comment khong ton tai -> Bao loi 404.
* **Post-conditions:** Trang thai like comment duoc cap nhat dung.

### 3.7. Xoa comment cua minh
* **Pre-conditions:** Comment ton tai; user la tac gia comment.
* **Main Flow:**
  1. User gui yeu cau xoa.
  2. He thong cap nhat `COMMENTS.status = DELETED`.
  3. He thong cap nhat bo dem lien quan.
* **Exception Flow:** Khong phai tac gia -> Bao loi 403.
* **Post-conditions:** Comment bi xoa mem va khong hien thi binh thuong tren UI.
