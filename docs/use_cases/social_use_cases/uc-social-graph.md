# UC - Social Graph Management

## 1. Overview
Use Case nay mo ta nghiep vu quan ly quan he xa hoi trong Social Service, bao gom follow/unfollow, xem profile user, va xem danh sach followers/following. Cac quan he follow duoc luu o `FOLLOWS` va tuan thu rule private/public profile tu du lieu dong bo Auth.

## 2. Actors
* **User:** Follow/unfollow va xem thong tin social profile.
* **System:** Xu ly relation graph, cap nhat denormalized profile va event.

## 3. Sub-Use Cases

### 3.1. Follow user
* **Pre-conditions:** User da dang nhap; target user ton tai; `follower_id != followee_id`.
* **Main Flow:**
  1. User gui request follow.
  2. He thong kiem tra profile target la public hay private.
  3. Neu public -> tao `FOLLOWS` voi `status = ACCEPTED`.
  4. Neu private -> tao `FOLLOWS` voi `status = PENDING` (neu flow private duoc bat trong MVP).
  5. He thong co the ghi event `USER_FOLLOWED` vao `OUTBOX_EVENTS`.
* **Exception Flow:**
  * Tu follow chinh minh -> Bao loi 400.
  * Relation da ton tai -> Xu ly idempotent hoac bao loi 409 tuy API policy.
* **Post-conditions:** Quan he follow duoc tao hoac cap nhat dung trang thai.

### 3.2. Unfollow user
* **Pre-conditions:** Quan he follow ton tai.
* **Main Flow:**
  1. User gui request unfollow.
  2. He thong xoa relation tu `FOLLOWS`.
* **Exception Flow:** Quan he khong ton tai -> Xu ly idempotent (tra ket qua an toan).
* **Post-conditions:** User khong con theo doi target user.

### 3.3. Xem profile user
* **Pre-conditions:** Target user ton tai va khong bi xoa.
* **Main Flow:**
  1. He thong lay du lieu profile tu projection local cua Social Service.
  2. He thong tra thong tin cong khai va social counts.
  3. He thong ap dung privacy rule theo trang thai user/profile dong bo tu Auth.
* **Exception Flow:** Target user khong ton tai -> Bao loi 404.
* **Post-conditions:** User nhan duoc profile phu hop voi quyen truy cap.

### 3.4. Xem danh sach followers/following
* **Pre-conditions:** Target user ton tai.
* **Main Flow:**
  1. User yeu cau danh sach followers hoac following.
  2. He thong query `FOLLOWS` theo `followee_id` (followers) hoac `follower_id` (following).
  3. He thong tra ket qua theo phan trang.
* **Exception Flow:** Tham so phan trang khong hop le -> Bao loi 400.
* **Post-conditions:** Danh sach quan he xa hoi duoc tra ve day du theo policy.
