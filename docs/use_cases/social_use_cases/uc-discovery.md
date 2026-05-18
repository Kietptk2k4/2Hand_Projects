# UC - Discovery

## 1. Overview
Use Case nay mo ta nhom nghiep vu discovery cua Social Service trong MVP, bao gom tim kiem post theo tu khoa, tim kiem theo hashtag, va quan ly lich su tim kiem. Ket qua tim kiem phai tuan thu visibility rule, follow relation va trang thai noi dung.

## 2. Actors
* **User:** Tim kiem noi dung tren he thong.
* **System:** Xu ly truy van tim kiem, phan trang va luu search history.

## 3. Sub-Use Cases

### 3.1. Search post don gian theo tu khoa
* **Pre-conditions:** User da dang nhap; keyword hop le.
* **Main Flow:**
  1. User nhap tu khoa tim kiem.
  2. He thong truy van `POSTS` theo caption/metadata/hashtags.
  3. He thong chi lay post `status = ACTIVE`.
  4. He thong ap dung rule visibility (`PUBLIC`, `FOLLOWERS` + relation follow).
  5. He thong tra ket qua theo phan trang.
* **Exception Flow:**
  * Keyword rong/sai format -> Bao loi 400.
  * Loi truy van -> Bao loi 500.
* **Post-conditions:** User nhan danh sach post phu hop voi quyen truy cap.

### 3.2. Search hashtag don gian
* **Pre-conditions:** Hashtag input hop le.
* **Main Flow:**
  1. User nhap hashtag can tim.
  2. He thong query `POSTS.hashtags`.
  3. He thong bo qua post khong hop le (`DRAFT`/`DELETED`/khong du quyen xem).
* **Exception Flow:** Hashtag khong hop le -> Bao loi 400.
* **Post-conditions:** User xem duoc danh sach post theo hashtag.

### 3.3. Luu lich su tim kiem (Search History)
* **Pre-conditions:** User da dang nhap; keyword hop le.
* **Main Flow:**
  1. Sau moi lan search thanh cong, he thong ghi/refresh keyword vao `SEARCH_HISTORY`.
  2. He thong cap nhat `updated_at` neu keyword da ton tai theo policy.
  3. He thong ho tro query lich su theo `user_id`, sap xep `created_at` giam dan.
* **Exception Flow:** Loi DB khi ghi lich su -> Khong lam fail truy van search chinh (best-effort).
* **Post-conditions:** User co lich su tim kiem de goi y va tai su dung.
