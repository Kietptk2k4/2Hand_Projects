# UC - User Authentication

## 1. Overview
Use Case này quản lý vòng đời xác thực cơ bản của người dùng (User Authentication Lifecycle), bao gồm việc đăng ký tài khoản mới, xác thực email, đăng nhập, duy trì phiên làm việc bằng Refresh Token và đăng xuất.

## 2. Actors
* **Guest:** Người dùng chưa đăng nhập.
* **User:** Người dùng đã có tài khoản và đã đăng nhập.

## 3. Sub-Use Cases

### 3.1. Đăng ký tài khoản (Register)
* **Pre-conditions:** Email và số điện thoại chưa tồn tại trong bảng `USERS` (trừ trường hợp đã bị Hard Delete).
* **Main Flow:**
  1. Guest nhập email, password.
  2. Hệ thống validate định dạng.
  3. Hệ thống tạo bản ghi mới trong `USERS` với `status` = `PENDING_VERIFICATION`, `email_verified` = `false`.
  4. Hệ thống tạo các bản ghi mặc định trong `USER_PROFILES`, `USER_SETTINGS`.
  5. Hệ thống sinh mã OTP/Token lưu vào `VERIFICATION_TOKENS` (type = `EMAIL_VERIFY`).
  6. Ghi event `EMAIL_VERIFICATION_REQUESTED` vào `OUTBOX_EVENTS`.
* **Exception Flow:** Email đã tồn tại -> Báo lỗi 409 Conflict.
* **Post-conditions:** Trạng thái tài khoản là chờ xác thực. Có event trong Outbox.

### 3.2. Xác thực Email (Verify Email)
* **Pre-conditions:** User đang ở trạng thái `PENDING_VERIFICATION`. Có token hợp lệ.
* **Main Flow:**
  1. Guest/User gửi request chứa token.
  2. Hệ thống kiểm tra `VERIFICATION_TOKENS` xem token có hợp lệ và chưa hết hạn không.
  3. Cập nhật `VERIFICATION_TOKENS` (`used_at` = now).
  4. Cập nhật `USERS` (`status` = `ACTIVE`, `email_verified` = `true`).
  5. Ghi event `USER_CREATED` vào `OUTBOX_EVENTS` để các service khác biết user đã chính thức hoạt động.
* **Exception Flow:** Token sai hoặc hết hạn -> Báo lỗi 400.

### 3.3. Đăng nhập bằng Email/Password (Login)
* **Pre-conditions:** User đã verify (status = `ACTIVE`) hoặc ít nhất không bị `SUSPENDED`/`DELETED`.
* **Main Flow:**
  1. User nhập email và password.
  2. Hệ thống so sánh `password_hash`.
  3. Đăng nhập thành công, tạo `REFRESH_TOKEN_SESSION` (status = `ACTIVE`).
  4. Cập nhật `USERS.last_login_at` = now.
  5. Ghi nhận thành công vào `LOGIN_LOGS`.
  6. Trả về Access Token và Refresh Token.
* **Exception Flow:**
  * Sai mật khẩu -> Ghi nhận thất bại vào `LOGIN_LOGS` -> Báo lỗi 401.
  * Status = `SUSPENDED`/`DELETED` -> Báo lỗi 403 Forbidden.

### 3.4. Refresh Access Token
* **Pre-conditions:** User có Refresh Token hợp lệ (status = `ACTIVE` trong DB).
* **Main Flow:**
  1. Client gửi Refresh Token.
  2. Hệ thống kiểm tra tính hợp lệ và thời hạn (`expires_at`) trong `REFRESH_TOKEN_SESSION`.
  3. Cấp phát Access Token mới.
* **Exception Flow:** Token không tồn tại, hết hạn, hoặc bị `REVOKED` -> Trả lỗi 401, yêu cầu login lại.

### 3.5. Đăng xuất (Logout)
* **Pre-conditions:** User đang có phiên đăng nhập hợp lệ.
* **Main Flow:**
  1. Client gửi request Logout kèm Refresh Token.
  2. Cập nhật `REFRESH_TOKEN_SESSION.status` = `LOGGED_OUT`.
  3. Client tự xóa token ở local.
* **Post-conditions:** Session hiện tại bị vô hiệu hóa.