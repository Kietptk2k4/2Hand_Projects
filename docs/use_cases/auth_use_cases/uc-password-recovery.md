# UC - Password Recovery

## 1. Overview
Use Case quản lý các nghiệp vụ liên quan đến khôi phục, đặt lại và thay đổi mật khẩu nhằm đảm bảo an toàn cho tài khoản người dùng. Khi mật khẩu thay đổi, bắt buộc phải Invalidate các session hiện tại.

## 2. Actors
* **Guest:** Người dùng quên mật khẩu (chưa đăng nhập).
* **User:** Người dùng chủ động đổi mật khẩu (đã đăng nhập).

## 3. Sub-Use Cases

### 3.1. Quên mật khẩu (Forgot Password)
* **Pre-conditions:** Email tồn tại trong bảng `USERS`.
* **Main Flow:**
  1. Guest nhập email.
  2. Hệ thống tạo mã Token/OTP lưu vào `VERIFICATION_TOKENS` (type = `PASSWORD_RESET`).
  3. Ghi sự kiện `PASSWORD_RESET_REQUESTED` vào `OUTBOX_EVENTS`.
* **Exception Flow:** Email không tồn tại -> Vẫn trả về 200 OK (tránh bị dò quét email) nhưng không gửi mail.

### 3.2. Đặt lại mật khẩu (Reset Password)
* **Pre-conditions:** Guest có token hợp lệ.
* **Main Flow:**
  1. Guest nhập mật khẩu mới và gửi kèm Token.
  2. Hệ thống xác thực `VERIFICATION_TOKENS`.
  3. Cập nhật `USERS` (`password_hash` mới, `password_changed_at` = now).
  4. Đánh dấu `VERIFICATION_TOKENS` đã sử dụng (`used_at`).
  5. Gọi hàm Invalidate All Sessions (xem 3.4).
  6. Ghi event `PASSWORD_CHANGED` vào `OUTBOX_EVENTS` để thông báo bảo mật.

### 3.3. Đổi mật khẩu (Change Password)
* **Pre-conditions:** User đang đăng nhập và nhớ mật khẩu cũ.
* **Main Flow:**
  1. User nhập mật khẩu cũ và mật khẩu mới.
  2. Hệ thống kiểm tra mật khẩu cũ.
  3. Cập nhật `USERS` (`password_hash`, `password_changed_at` = now).
  4. Gọi hàm Invalidate All Sessions.
  5. Ghi event `PASSWORD_CHANGED` vào `OUTBOX_EVENTS`.
* **Exception Flow:** Sai mật khẩu cũ -> Trả lỗi 400.

### 3.4. Session Invalidation (Internal Action)
* **Trigger:** Khi có nghiệp vụ đổi/reset mật khẩu.
* **Main Flow:**
  1. Tìm tất cả các bản ghi trong `REFRESH_TOKEN_SESSION` của `user_id` có trạng thái `ACTIVE`.
  2. Cập nhật toàn bộ thành `REVOKED`.
* **Post-conditions:** Mọi thiết bị khác đang đăng nhập sẽ bị văng ra (buộc login lại).