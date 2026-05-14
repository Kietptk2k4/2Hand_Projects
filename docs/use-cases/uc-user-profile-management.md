# UC - User Profile Management

## 1. Overview
Quản lý thông tin định danh, tùy chỉnh cá nhân, và quyền riêng tư (Privacy) của User.

## 2. Actors
* **User:** Quản lý profile của chính mình.
* **Guest/Other Users:** Xem public profile của user khác.

## 3. Sub-Use Cases

### 3.1. Xem thông tin tài khoản (View Profile)
* **Main Flow (Self):** Trả về toàn bộ dữ liệu từ `USERS`, `USER_PROFILES`, `USER_SETTINGS`.
* **Main Flow (Public):**
  1. Client yêu cầu xem profile của User A.
  2. Hệ thống kiểm tra `USER_PROFILES.is_private`.
  3. Nếu `false`: Trả về `display_name`, `avatar_url`, `bio`, `social_links`.
  4. Nếu `true`: Chỉ trả về `display_name`, `avatar_url` (ẩn bio/social_links).

### 3.2. Cập nhật hồ sơ & Avatar
* **Pre-conditions:** User đang đăng nhập.
* **Main Flow:**
  1. User gửi dữ liệu cập nhật (`display_name`, `bio`, `website`, `social_links`, `avatar_url`).
  2. Hệ thống cập nhật bảng `USER_PROFILES`.
  3. Ghi event `USER_UPDATED` vào `OUTBOX_EVENTS` để Social Service đồng bộ avatar/name sang các bài Post/Comment.

### 3.3. Cập nhật Privacy & Settings
* **Main Flow:**
  1. User gửi yêu cầu chuyển đổi `is_private` hoặc đổi `appearance_mode` (Light/Dark).
  2. Cập nhật `USER_PROFILES` (với Privacy) hoặc `USER_SETTINGS` (với Appearance).
  3. Nếu đổi `is_private`, ghi event `USER_UPDATED`.

### 3.4. Soft Delete Account
* **Pre-conditions:** Nhập đúng mật khẩu xác nhận. Không có đơn hàng đang giao (cần check chéo Commerce - Tùy logic).
* **Main Flow:**
  1. Cập nhật `USERS.status` = `DELETED`, `deleted_at` = now().
  2. Gọi hàm Invalidate All Sessions (Logout All).
  3. Ghi event `USER_DELETED` vào `OUTBOX_EVENTS`.
