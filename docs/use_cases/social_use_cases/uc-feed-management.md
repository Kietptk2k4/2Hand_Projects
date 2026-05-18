# UC - Feed Management

## 1. Overview
Use Case nay mo ta cach Social Service phuc vu hai loai feed MVP: Global Feed va Following Feed. Feed chi hien thi post hop le theo trang thai, visibility va quan he follow, dam bao nguoi dung nhan duoc noi dung dung pham vi quyen truy cap.

## 2. Actors
* **User:** Xem Global Feed va Following Feed.
* **System:** Xu ly query, phan trang va filter visibility.

## 3. Sub-Use Cases

### 3.1. Xem Global Feed
* **Pre-conditions:** User da dang nhap.
* **Main Flow:**
  1. User gui request lay feed tong.
  2. He thong query `POSTS` voi dieu kien `status = ACTIVE`.
  3. He thong filter them `visibility = PUBLIC`.
  4. He thong sap xep theo `created_at` giam dan va tra ve theo phan trang.
* **Exception Flow:**
  * Tham so phan trang sai -> Bao loi 400.
  * Loi truy van MongoDB -> Bao loi 500.
* **Post-conditions:** User nhan danh sach post cong khai moi nhat.

### 3.2. Xem Following Feed
* **Pre-conditions:** User da dang nhap; user co follow graph trong `FOLLOWS`.
* **Main Flow:**
  1. He thong lay danh sach `followee_id` ma user dang theo doi (`status = ACCEPTED`).
  2. He thong query `POSTS` theo `author_id` nam trong danh sach tren va `status = ACTIVE`.
  3. He thong ap dung rule visibility:
     * `PUBLIC`: hien thi binh thuong.
     * `FOLLOWERS`: chi hien thi neu ton tai quan he follow hop le.
  4. He thong sap xep, phan trang va tra ket qua.
* **Exception Flow:** Follow relation query that bai hoac timeout -> Bao loi 500.
* **Post-conditions:** User nhan feed uu tien noi dung tu nguoi dang theo doi.

### 3.3. Loai tru noi dung khong hop le khoi feed
* **Pre-conditions:** Co post o trang thai `DRAFT` hoac `DELETED`.
* **Main Flow:**
  1. He thong bo qua post `DRAFT`.
  2. He thong bo qua post `DELETED`.
  3. He thong khong tra ve bai viet bi an do moderation.
* **Exception Flow:** Khong co.
* **Post-conditions:** Feed chi chua noi dung co the hien thi cho user.

### 3.4. Phan trang feed
* **Pre-conditions:** User truyen tham so phan trang hop le.
* **Main Flow:**
  1. He thong ap dung co che cursor/offset theo implementation.
  2. He thong gioi han so phan tu moi trang.
  3. He thong tra ve ket qua theo API response wrapper cua project.
* **Exception Flow:** Token cursor khong hop le -> Bao loi 400.
* **Post-conditions:** Feed tai nhanh va on dinh voi du lieu lon.
