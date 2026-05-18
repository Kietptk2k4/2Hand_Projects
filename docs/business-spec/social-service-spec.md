# Product Specification - Social Service (MVP)

Tai lieu nay dinh nghia chi tiet cac nghiep vu cot loi cua Social Service trong he thong 2Hands (MVP), tuan thu kien truc Microservices + Event-Driven Architecture va Outbox Pattern.

Social Service quan ly du lieu noi dung mang xa hoi theo mo hinh polyglot persistence:
- MongoDB: noi dung post/comment va cac truong doc nhieu.
- PostgreSQL: cac quan he tuong tac va outbox events.

---

## I. Phan he: Feed (Global Feed & Following Feed)

### 1. Xem Global Feed
1. **Business Goal:** Cho phep user kham pha noi dung cong khai moi nhat tren toan he thong.
2. **Actors:** User.
3. **Preconditions:** User da dang nhap; post co `status = ACTIVE`.
4. **Workflow:**
   - Query `POSTS` (MongoDB) voi `status = ACTIVE`.
   - Chi lay post co `visibility = PUBLIC`.
   - Sap xep theo `created_at` giam dan, phan trang cursor/offset.
5. **State Machine:** Khong thay doi trang thai user hay post.
6. **Business Rules:** Khong hien thi post `DRAFT` va `DELETED`.
7. **Failure Cases:** Loi ket noi MongoDB, payload phan trang khong hop le.
8. **Events:** Khong phat event domain bat buoc.
9. **Ownership:** Social Service.

### 2. Xem Following Feed
1. **Business Goal:** Hien thi feed uu tien noi dung tu nguoi dung dang theo doi.
2. **Actors:** User.
3. **Preconditions:** User da dang nhap; quan he follow hop le trong `FOLLOWS`.
4. **Workflow:**
   - Lay danh sach `followee_id` ma user dang theo doi (`ACCEPTED`).
   - Query `POSTS` theo `author_id in followee_ids` va `status = ACTIVE`.
   - Ap dung rule visibility:
     - `PUBLIC`: luon co the xem.
     - `FOLLOWERS`: chi hien thi khi ton tai quan he follow hop le.
5. **State Machine:** Khong thay doi.
6. **Business Rules:** Following feed khong duoc tra ve post da bi xoa mem.
7. **Failure Cases:** Follow graph khong truy van duoc, timeout DB.
8. **Events:** Khong phat event domain bat buoc.
9. **Ownership:** Social Service.

---

## II. Phan he: Post Management (Quan ly bai viet)

### 1. Tao post
1. **Business Goal:** Cho phep user tao noi dung de chia se.
2. **Actors:** User.
3. **Preconditions:** User hop le, khong bi khoa (suspended/deleted theo thong tin dong bo tu Auth).
4. **Workflow:**
   - Nhan payload: `caption`, `media`, `productTags`, `visibility`, `allow_comments`, `hashtags`.
   - Validate payload.
   - Luu `POSTS` voi trang thai khoi tao (`DRAFT` hoac `ACTIVE` tuy action publish).
5. **State Machine:** `DRAFT -> ACTIVE -> DELETED` (soft delete).
6. **Business Rules:**
   - `productTags[].product_id` la tham chieu den product cua Commerce Service.
   - Khong xac thuc truc tiep DB cua Commerce; chi validate dinh dang/co che API theo integration policy.
7. **Failure Cases:** Media khong hop le, payload vuot gioi han, loi DB.
8. **Events:** Co the phat `POST_CREATED` (noi bo) qua outbox khi can tich hop Notification/Analytics.
9. **Ownership:** Social Service.

### 2. Sua post
1. **Business Goal:** Cho phep tac gia cap nhat noi dung bai viet.
2. **Actors:** User (author).
3. **Preconditions:** Post ton tai, user la author, post chua o trang thai `DELETED`.
4. **Workflow:** Validate quyen -> cap nhat noi dung -> cap nhat `updated_at`.
5. **State Machine:** `DRAFT/ACTIVE` giu nguyen trang thai neu khong co lenh doi trang thai.
6. **Business Rules:** Chi author moi duoc sua post cua minh.
7. **Failure Cases:** 403 khong co quyen, 404 post khong ton tai.
8. **Events:** `POST_UPDATED` (tuy nhu cau dong bo).
9. **Ownership:** Social Service.

### 3. Xoa post (Soft Delete)
1. **Business Goal:** Cho phep user xoa bai viet nhung van bao toan tinh toan ven he thong.
2. **Actors:** User (author), Admin/Moderator (qua luong moderation).
3. **Preconditions:** Post ton tai.
4. **Workflow:** Dat `status = DELETED`, set `deleted_at`, cap nhat `updated_at`.
5. **State Machine:** `DRAFT/ACTIVE -> DELETED`.
6. **Business Rules:** Khong xoa vat ly ngay trong MVP.
7. **Failure Cases:** 404 khong tim thay post, loi DB.
8. **Events:** `POST_DELETED` (neu can dong bo Notification/Moderation/Search index).
9. **Ownership:** Social Service.

---

## III. Phan he: Comment Management (Binh luan va phan hoi)

### 1. Tao comment
1. **Business Goal:** Cho phep user thao luan tren post.
2. **Actors:** User.
3. **Preconditions:** Post `ACTIVE`, `allow_comments = true`, user hop le.
4. **Workflow:**
   - Tao document trong `COMMENTS` voi `parent_comment_id = null`.
   - Tang bo dem `reply_count` cua post (eventual consistency hoac transaction tuy kien truc persistence).
5. **State Machine:** Comment `ACTIVE/DELETED`.
6. **Business Rules:** Khong cho comment vao post da xoa hoac post tat comment.
7. **Failure Cases:** 400/403/404 theo tinh huong.
8. **Events:** Phat `COMMENT_CREATED` qua outbox de Notification Service gui thong bao.
9. **Ownership:** Social Service.

### 2. Reply comment
1. **Business Goal:** Ho tro hoi thoai theo thread.
2. **Actors:** User.
3. **Preconditions:** Comment cha ton tai va `status = ACTIVE`.
4. **Workflow:** Tao comment moi voi `parent_comment_id` tro den comment cha.
5. **State Machine:** Khong thay doi comment cha; comment moi o `ACTIVE`.
6. **Business Rules:** Gioi han do sau thread do Social Service quy dinh (MVP co the cho 1-2 tang de gian luoc).
7. **Failure Cases:** Parent comment khong ton tai/da xoa.
8. **Events:** `COMMENT_CREATED`.
9. **Ownership:** Social Service.

### 3. Xoa comment cua minh
1. **Business Goal:** Cho phep user rut noi dung vi pham/khong con phu hop.
2. **Actors:** User (author), Admin/Moderator.
3. **Preconditions:** Comment ton tai.
4. **Workflow:** Set `status = DELETED`, set `deleted_at`, cap nhat bo dem lien quan.
5. **State Machine:** `ACTIVE -> DELETED`.
6. **Business Rules:** User chi xoa duoc comment cua minh; admin theo quyen moderation.
7. **Failure Cases:** 403/404.
8. **Events:** `COMMENT_DELETED` (neu can dong bo).
9. **Ownership:** Social Service.

---

## IV. Phan he: Engagement (Like/Save Post, Like Comment)

### 1. Like/Unlike post
1. **Business Goal:** Ghi nhan muc do quan tam cua user voi post.
2. **Actors:** User.
3. **Preconditions:** Post ton tai va `ACTIVE`.
4. **Workflow:**
   - Like: insert `POST_LIKES(post_id, user_id)` neu chua ton tai.
   - Unlike: delete record tuong ung.
   - Dong bo `like_count` tren `POSTS` theo co che nhat quan eventual.
5. **State Machine:** Khong thay doi trang thai post.
6. **Business Rules:** Idempotent, unique theo `(post_id, user_id)`.
7. **Failure Cases:** Vi pham unique, post khong ton tai.
8. **Events:** Phat `POST_LIKED` khi like thanh cong de Notification Service xu ly.
9. **Ownership:** Social Service.

### 2. Save/Unsave post + Xem danh sach da luu
1. **Business Goal:** Cho phep user danh dau va xem lai bai viet quan tam.
2. **Actors:** User.
3. **Preconditions:** User da dang nhap.
4. **Workflow:**
   - Save: insert `POST_SAVES`.
   - Unsave: delete `POST_SAVES`.
   - Xem saved posts: join mapping tu `POST_SAVES` den `POSTS`.
5. **State Machine:** Khong thay doi.
6. **Business Rules:** Idempotent theo unique `(post_id, user_id)`.
7. **Failure Cases:** Loi join/truy van, post khong con ton tai.
8. **Events:** Khong bat buoc event external trong MVP.
9. **Ownership:** Social Service.

### 3. Like comment
1. **Business Goal:** Tuong tac voi comment.
2. **Actors:** User.
3. **Preconditions:** Comment ton tai va `ACTIVE`.
4. **Workflow:** Toggle record trong `COMMENT_REACTION` theo `(comment_id, user_id)`, dong bo `like_count`.
5. **State Machine:** Khong thay doi trang thai comment.
6. **Business Rules:** 1 user chi duoc reaction 1 lan tren 1 comment.
7. **Failure Cases:** Comment khong ton tai, vi pham unique.
8. **Events:** Co the phat `COMMENT_LIKED` (tuy chinh sach notification).
9. **Ownership:** Social Service.

---

## V. Phan he: Social Graph (Follow/Unfollow/Profile)

### 1. Follow user
1. **Business Goal:** Tao quan he theo doi de phuc vu social graph va following feed.
2. **Actors:** User.
3. **Preconditions:** Target user ton tai; `follower_id != followee_id`.
4. **Workflow:**
   - Tao record trong `FOLLOWS`.
   - Neu account private: co the tao `PENDING`.
   - Neu account public: `ACCEPTED`.
5. **State Machine:** Follow relation `PENDING -> ACCEPTED` (neu private flow duoc bat).
6. **Business Rules:** Unique `(follower_id, followee_id)`.
7. **Failure Cases:** Tu follow chinh minh, duplicate relation.
8. **Events:** Phat `USER_FOLLOWED` qua outbox de Notification Service gui thong bao.
9. **Ownership:** Social Service.

### 2. Unfollow user
1. **Business Goal:** Huy quan he theo doi.
2. **Actors:** User.
3. **Preconditions:** Quan he follow ton tai.
4. **Workflow:** Xoa record trong `FOLLOWS`.
5. **State Machine:** Relation bi remove.
6. **Business Rules:** Idempotent (xoa relation khong ton tai van tra ket qua an toan).
7. **Failure Cases:** Loi DB.
8. **Events:** Co the phat `USER_UNFOLLOWED` (optional MVP).
9. **Ownership:** Social Service.

### 3. Xem profile user va danh sach followers/following
1. **Business Goal:** Hien thi thong tin social profile va quan he theo doi.
2. **Actors:** User.
3. **Preconditions:** User target ton tai, khong bi xoa.
4. **Workflow:**
   - Lay profile cong khai tu projection du lieu user tai Social Service.
   - Lay counts/list followers, following tu `FOLLOWS`.
5. **State Machine:** Khong thay doi.
6. **Business Rules:** Tuan thu privacy rule (private profile) theo du lieu dong bo tu Auth Service.
7. **Failure Cases:** 404 user khong ton tai.
8. **Events:** Khong bat buoc.
9. **Ownership:** Social Service.

---

## VI. Phan he: Discovery (Search Post & Hashtag)

### 1. Search post don gian
1. **Business Goal:** Ho tro tim kiem noi dung post theo tu khoa.
2. **Actors:** User.
3. **Preconditions:** User da dang nhap.
4. **Workflow:**
   - Search text theo caption/metadata/hastag.
   - Ap dung filter `status = ACTIVE` va visibility.
   - Co the luu lich su vao `SEARCH_HISTORY`.
5. **State Machine:** Khong thay doi.
6. **Business Rules:** Ket qua phai ton trong visibility va follow relation.
7. **Failure Cases:** Query khong hop le, timeout.
8. **Events:** Khong bat buoc.
9. **Ownership:** Social Service.

### 2. Search hashtag don gian
1. **Business Goal:** Tim post theo hashtag.
2. **Actors:** User.
3. **Preconditions:** Hashtag input hop le.
4. **Workflow:** Query `POSTS.hashtags` va phan trang.
5. **State Machine:** Khong thay doi.
6. **Business Rules:** Co index cho `hashtags`; bo qua post khong duoc phep hien thi.
7. **Failure Cases:** Hashtag rong/sai format.
8. **Events:** Khong bat buoc.
9. **Ownership:** Social Service.

---

## VII. Phan he: Event / Integration (Tich hop va dong bo)

### 1. Dong bo du lieu user tu Auth Service
1. **Business Goal:** Dam bao Social co du lieu user local de render feed/profile nhanh, khong cross-DB read.
2. **Actors:** System (consumer worker).
3. **Preconditions:** Nhan duoc event hop le tu message broker.
4. **Workflow:**
   - Consume `USER_CREATED`, `USER_UPDATED`, `USER_DELETED` (va su kien status lien quan suspend neu duoc publish).
   - Upsert projection user trong Social DB.
5. **State Machine:** User projection local duoc cap nhat eventual consistency.
6. **Business Rules:** Consumer phai idempotent (xu ly event lap).
7. **Failure Cases:** Payload thieu field, broker down, dead-letter.
8. **Events:** Social consume event tu Auth; khong phat lai event nguon.
9. **Ownership:** Social Service.

### 2. Publish social domain events bang Outbox
1. **Business Goal:** Dong bo den Notification va cac service khac ma khong mat event.
2. **Actors:** System (Outbox Worker).
3. **Preconditions:** Giao dich domain thanh cong va da ghi event vao `OUTBOX_EVENTS`.
4. **Workflow:**
   - Ghi event trong cung transaction voi thao tac domain.
   - Worker poll event `PENDING`/`FAILED`, publish len broker.
   - Cap nhat trang thai publish va retry count.
5. **State Machine:** `PENDING -> PROCESSING -> PUBLISHED` (hoac `FAILED` neu loi).
6. **Business Rules:** Co gioi han retry; event payload phai co `aggregate_id` + metadata toi thieu.
7. **Failure Cases:** Broker down, loi serialization, retry qua nguong.
8. **Events:** Su dung cac event MVP: `POST_LIKED`, `COMMENT_CREATED`, `USER_FOLLOWED`.
9. **Ownership:** Social Service.

---

## VIII. Phan he: Security, Moderation, Compliance

### 1. Enforcement voi user bi khoa/bi xoa
1. **Business Goal:** Chan thao tac tao moi noi dung va tuong tac tu account khong hop le.
2. **Actors:** System.
3. **Preconditions:** Da dong bo user status tu Auth.
4. **Workflow:** Kiem tra user status tai layer application truoc khi cho phep tao post/comment/like/follow.
5. **State Machine:** Khong doi trang thai domain.
6. **Business Rules:** User `SUSPENDED/DELETED` khong duoc tao moi social action.
7. **Failure Cases:** Du lieu sync tre.
8. **Events:** Co the phat su kien moderation noi bo de audit.
9. **Ownership:** Social Service.

### 2. Tich hop moderation voi Admin Service
1. **Business Goal:** Ho tro an/xoa noi dung vi pham theo quyet dinh moderation.
2. **Actors:** Admin/Moderator, System.
3. **Preconditions:** Co request moderation hop le.
4. **Workflow:** Thuc thi soft delete/hide voi `POST`/`COMMENT`, dong bo log o Admin Service theo integration contract.
5. **State Machine:** Noi dung vi pham ve `DELETED` hoac hidden state theo chinh sach.
6. **Business Rules:** Luu du traces de audit.
7. **Failure Cases:** Xung dot trang thai moderation.
8. **Events:** `POST_DELETED`/`COMMENT_DELETED` (neu can tich hop).
9. **Ownership:** Social Service.

---

## IX. Ghi chu du lieu, indexing va hieu nang

1. **MongoDB indexes (MVP):**
   - `POSTS(status, visibility, created_at desc)`
   - `POSTS(hashtags)`
   - `POSTS(author_id, created_at desc)`
   - `POSTS(author_id, status, created_at desc)`
   - `COMMENTS(post_id, created_at)`
   - `COMMENTS(post_id, status, created_at)`
   - `COMMENTS(parent_comment_id)`
2. **PostgreSQL indexes (MVP):**
   - `FOLLOWS(follower_id, created_at desc)`
   - `FOLLOWS(followee_id)`
   - `SEARCH_HISTORY(user_id, created_at desc)`
3. **Uniqueness & constraints:**
   - `POST_LIKES` unique `(post_id, user_id)`
   - `POST_SAVES` unique `(post_id, user_id)`
   - `COMMENT_REACTION` unique `(comment_id, user_id)`
   - `FOLLOWS` unique `(follower_id, followee_id)` va check `follower_id != followee_id`

---

## X. Ownership va boundaries

1. **Social Service own:** Post/comment/feed-like/save/follow/search-history va social outbox.
2. **Auth Service own:** Danh tinh, xac thuc, role/permission, profile goc.
3. **Commerce Service own:** Product data, ton kho, order lifecycle.
4. **Notification Service own:** Fan-out push/email/in-app dua tren event.
5. **Boundary rule bat buoc:** Khong service nao truy cap truc tiep DB cua service khac.

---

## XI. Danh sach su kien MVP lien quan Social

1. **Consume from Auth:** `USER_CREATED`, `USER_UPDATED`, `USER_DELETED` (va su kien status lien quan suspend neu duoc pub).
2. **Publish from Social:** `POST_LIKED`, `COMMENT_CREATED`, `USER_FOLLOWED`.
3. **Outbox policy:** Tat ca event publish ra broker deu di qua `OUTBOX_EVENTS` de dam bao at-least-once va retry an toan.

