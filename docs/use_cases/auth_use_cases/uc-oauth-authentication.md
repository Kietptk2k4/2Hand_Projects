# UC - OAuth Authentication

## 1. Overview
Cung cấp khả năng đăng nhập/đăng ký liền mạch thông qua tài khoản bên thứ 3 (Google, Facebook) mà không cần mật khẩu.

## 2. Actors
* **Guest/User:** Khách hàng sử dụng ứng dụng.

## 3. Sub-Use Cases

### 3.1. Đăng nhập bằng Google/Facebook
* **Pre-conditions:** Client cung cấp Access/ID Token hợp lệ từ Provider.
* **Main Flow:**
  1. Auth Service xác thực Token với server của Google/Facebook và trích xuất `email`, `name`, `avatar`.
  2. Truy vấn bảng `USERS` bằng `email`.
  3. **Trường hợp User mới:**
     * Thêm vào `USERS` (status = `ACTIVE`, `email_verified` = `true`).
     * Thêm `USER_PROFILES`, `USER_SETTINGS`.
     * Thêm bản ghi vào `OAUTH_ACCOUNTS`.
     * Ghi sự kiện `USER_CREATED` vào `OUTBOX_EVENTS`.
  4. **Trường hợp User đã tồn tại:**
     * Đảm bảo `OAUTH_ACCOUNTS` đã có bản ghi liên kết. Cập nhật `last_login_at`.
  5. Tạo Session (`REFRESH_TOKEN_SESSION`) và log (`LOGIN_LOGS`).
  6. Trả về Access Token và Refresh Token cho Client.
* **Exception Flow:** Token OAuth không hợp lệ hoặc thiếu thông tin Email -> Báo lỗi 400/401.

### 3.2. Liên kết/Hủy liên kết tài khoản OAuth (Tương lai/Phụ)
* Cho phép user đang dùng Password liên kết thêm Google vào `OAUTH_ACCOUNTS` để lần sau có thể login bằng Google.