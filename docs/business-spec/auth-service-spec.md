# Product Specification - Auth Service (MVP)

Tài liệu này định nghĩa chi tiết các nghiệp vụ cốt lõi của Auth Service trong hệ thống 2Hands (MVP), tuân thủ thiết kế Microservices và Event-Driven Architecture.

---

## I. Phân hệ: Authentication (Xác thực)

### 1. Đăng ký tài khoản bằng email
1. **Business Goal:** Cho phép người dùng mới tạo tài khoản để tham gia hệ thống.
2. **Actors:** Guest.
3. **Preconditions:** Email chưa tồn tại trong hệ thống.
4. **Workflow:** - Nhận thông tin đăng ký (Email, Password).
   - Validate định dạng email và độ mạnh mật khẩu.
   - Hash mật khẩu và lưu vào bảng `USERS` với trạng thái `PENDING_VERIFICATION`.
   - Tạo bản ghi mặc định ở `USER_PROFILES` và `USER_SETTINGS`.
   - Lưu Outbox event để yêu cầu gửi mail xác thực.
5. **State Machine:** `None` -> `PENDING_VERIFICATION`.
6. **Business Rules:** Email phải là duy nhất. Mật khẩu ít nhất 8 ký tự.
7. **Failure Cases:** Email đã tồn tại (Lỗi 409), Lỗi kết nối DB.
8. **Events:** `USER_CREATED`, `EMAIL_VERIFICATION_REQUESTED`.
9. **Ownership:** Auth Service.

### 2. Đăng nhập bằng email/password
1. **Business Goal:** Xác thực định danh để cấp quyền truy cập hệ thống.
2. **Actors:** User.
3. **Preconditions:** Tài khoản tồn tại và không ở trạng thái `DELETED` hay `SUSPENDED`.
4. **Workflow:**
   - Nhận Email và Password.
   - So sánh hash mật khẩu.
   - Nếu đúng, cấp cặp Access Token và Refresh Token.
   - Cập nhật `last_login_at` và ghi nhận vào `LOGIN_LOGS`.
5. **State Machine:** Không thay đổi (vẫn là `ACTIVE`).
6. **Business Rules:** Tài khoản `SUSPENDED` hoặc `DELETED` không được đăng nhập.
7. **Failure Cases:** Sai mật khẩu (Lỗi 401), Tài khoản bị khóa (Lỗi 403).
8. **Events:** `USER_LOGGED_IN` (Internal Event để log).
9. **Ownership:** Auth Service.

### 3. Đăng nhập bằng OAuth (Google, Facebook)
1. **Business Goal:** Đơn giản hóa quy trình đăng nhập/đăng ký thông qua bên thứ ba.
2. **Actors:** Guest / User.
3. **Preconditions:** User có tài khoản Google/Facebook hợp lệ.
4. **Workflow:**
   - Client gửi OAuth Token/Code.
   - Auth Service gọi provider (Google/Facebook) lấy profile.
   - Nếu email chưa tồn tại: Tạo mới `USERS` (trạng thái `ACTIVE`) và `USER_PROFILES`.
   - Nếu email đã tồn tại: Cấp luôn Token truy cập.
5. **State Machine:** `None` -> `ACTIVE` (Nếu tạo mới).
6. **Business Rules:** Tài khoản tạo qua OAuth được mặc định là `email_verified = true`.
7. **Failure Cases:** Token OAuth hết hạn, Provider từ chối.
8. **Events:** `USER_CREATED` (Nếu tài khoản mới).
9. **Ownership:** Auth Service.

### 4. Đăng xuất
1. **Business Goal:** Kết thúc an toàn phiên làm việc.
2. **Actors:** User (Đã đăng nhập).
3. **Preconditions:** Gửi yêu cầu kèm Refresh Token hợp lệ.
4. **Workflow:** Vô hiệu hóa (xóa hoặc blacklist) Refresh Token đó trong DB/Redis.
5. **State Machine:** Session State -> `INACTIVE`.
6. **Business Rules:** Client tự xóa token local. Server trả 200 kể cả khi token đã bị xóa trước đó.
7. **Failure Cases:** Lỗi Redis/DB.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 5. Refresh Access Token
1. **Business Goal:** Cấp Access Token mới khi token cũ hết hạn mà không cần đăng nhập lại.
2. **Actors:** Client/App.
3. **Preconditions:** Refresh Token còn hạn và hợp lệ.
4. **Workflow:**
   - Xác thực Refresh Token.
   - Sinh Access Token mới.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Nếu Refresh Token bị dùng lại bất thường, hủy toàn bộ phiên của User.
7. **Failure Cases:** Token hết hạn hoặc bị thu hồi (Lỗi 401).
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 6. Verify Email
1. **Business Goal:** Xác minh quyền sở hữu email.
2. **Actors:** User.
3. **Preconditions:** Tài khoản đang ở trạng thái `PENDING_VERIFICATION`.
4. **Workflow:**
   - Validate token/OTP từ email.
   - Cập nhật `email_verified = true` và `status = ACTIVE`.
5. **State Machine:** `PENDING_VERIFICATION` -> `ACTIVE`.
6. **Business Rules:** OTP/Token có hạn sử dụng (ví dụ 15 phút).
7. **Failure Cases:** Token hết hạn, Token sai.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 7. Quên mật khẩu
1. **Business Goal:** Hỗ trợ người dùng lấy lại quyền truy cập.
2. **Actors:** Guest / User.
3. **Preconditions:** Nhập email hợp lệ.
4. **Workflow:**
   - Tạo mã Reset Password tạm thời.
   - Ghi event `PASSWORD_RESET_REQUESTED` vào Outbox để gửi mail.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Không trả về lỗi nếu email không tồn tại (Tránh dò quét email).
7. **Failure Cases:** Lỗi lưu Outbox.
8. **Events:** `PASSWORD_RESET_REQUESTED`.
9. **Ownership:** Auth Service.

### 8. Đổi mật khẩu
1. **Business Goal:** Cho phép người dùng chủ động đổi mật khẩu.
2. **Actors:** User (Đã đăng nhập).
3. **Preconditions:** Nhập đúng mật khẩu cũ.
4. **Workflow:**
   - Xác nhận mật khẩu cũ.
   - Hash và lưu mật khẩu mới.
   - Cập nhật `password_changed_at`.
   - Hủy toàn bộ session hiện tại.
5. **State Machine:** All Sessions -> `REVOKED`.
6. **Business Rules:** Mật khẩu mới không được trùng mật khẩu cũ.
7. **Failure Cases:** Sai mật khẩu cũ.
8. **Events:** `PASSWORD_CHANGED`.
9. **Ownership:** Auth Service.

---

## II. Phân hệ: User Account (Quản lý Tài khoản)

### 1. Xem thông tin tài khoản
1. **Business Goal:** Xem thông tin cá nhân của chính mình.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Trả về data từ `USERS`, `USER_PROFILES`, và `USER_SETTINGS`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Trả về đầy đủ (gồm cả email, phone).
7. **Failure Cases:** User không tồn tại (404).
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 2. Cập nhật hồ sơ cá nhân
1. **Business Goal:** Cập nhật thông tin định danh (bio, website, social links).
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Validate payload và cập nhật bảng `USER_PROFILES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Validate URL và JSON format cho social links.
7. **Failure Cases:** Lỗi vượt quá ký tự, format sai.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 3. Cập nhật avatar
1. **Business Goal:** Thay đổi ảnh đại diện.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Cập nhật trường `avatar_url` trong `USER_PROFILES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** URL phải hợp lệ (đã upload lên storage).
7. **Failure Cases:** URL sai format.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 4. Bật/tắt private profile
1. **Business Goal:** Kiểm soát quyền riêng tư.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Đảo giá trị `is_private` trong `USER_PROFILES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Trạng thái này sẽ được Social Service kiểm tra khi hiển thị bài viết.
7. **Failure Cases:** Lỗi DB.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 5. Cập nhật user settings
1. **Business Goal:** Lưu cấu hình giao diện.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Update trường `appearance_mode` trong `USER_SETTINGS`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Chỉ nhận Enum hợp lệ (`LIGHT`, `DARK`, `SYSTEM`).
7. **Failure Cases:** Enum sai.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 6. Soft delete account
1. **Business Goal:** Xóa tài khoản theo yêu cầu nhưng giữ dữ liệu đối soát.
2. **Actors:** User.
3. **Preconditions:** Xác nhận bằng mật khẩu, không có đơn hàng đang giao.
4. **Workflow:** Cập nhật `status = DELETED`, thu hồi toàn bộ token.
5. **State Machine:** `ACTIVE` -> `DELETED`.
6. **Business Rules:** Email có thể được randomize để giải phóng.
7. **Failure Cases:** Sai mật khẩu, Đơn hàng chưa hoàn tất.
8. **Events:** `USER_DELETED`.
9. **Ownership:** Auth Service.

---

## III. Phân hệ: Session Management (Quản lý phiên)

### 1. Xem danh sách phiên đăng nhập
1. **Business Goal:** Giúp người dùng quản lý các thiết bị đang đăng nhập.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Query các Refresh Tokens đang `ACTIVE` của user.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Ẩn bớt IP, trả về tên thiết bị rõ ràng.
7. **Failure Cases:** Lỗi DB.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 2. Logout current session
1. **Business Goal:** Đăng xuất trên thiết bị hiện tại.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Hủy Refresh Token hiện tại.
5. **State Machine:** Session -> `INACTIVE`.
6. **Business Rules:** Client tự clear cache.
7. **Failure Cases:** Token lỗi.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 3. Logout all sessions
1. **Business Goal:** Ngắt kết nối tất cả thiết bị.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Xóa toàn bộ Refresh Tokens của user.
5. **State Machine:** All Sessions -> `REVOKED`.
6. **Business Rules:** Bắt buộc kích hoạt khi User đổi mật khẩu.
7. **Failure Cases:** Lỗi DB.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 4. Theo dõi login history
1. **Business Goal:** Audit bảo mật lịch sử đăng nhập.
2. **Actors:** User / Admin.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Query từ bảng `LOGIN_LOGS` phân trang.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Log cả thành công và thất bại.
7. **Failure Cases:** Pagination sai.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

---

## IV. Phân hệ: Role & Permission (Phân quyền)

### 1. Gán role cho user
1. **Business Goal:** Cấp quyền (Seller, Admin, Moderator) cho User.
2. **Actors:** Super Admin.
3. **Preconditions:** Admin có thẩm quyền cao hơn.
4. **Workflow:** Insert vào `USER_ROLES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Không ai tự gán role cho mình.
7. **Failure Cases:** User/Role không tồn tại.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 2. Thu hồi role khỏi user
1. **Business Goal:** Hạ cấp quyền.
2. **Actors:** Super Admin.
3. **Preconditions:** Admin có thẩm quyền cao hơn.
4. **Workflow:** Delete khỏi `USER_ROLES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Super Admin cuối cùng không thể bị gỡ Role.
7. **Failure Cases:** Lỗi DB.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 3. Xem danh sách role
1. **Business Goal:** Lấy danh sách Roles hệ thống.
2. **Actors:** Admin.
3. **Preconditions:** Quản trị viên.
4. **Workflow:** Query bảng `ROLES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Read-only.
7. **Failure Cases:** Lỗi DB.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 4. Xem permission của role
1. **Business Goal:** Liệt kê các quyền của 1 Role.
2. **Actors:** Admin.
3. **Preconditions:** Quản trị viên.
4. **Workflow:** Query `ROLE_PERMISSIONS` join `PERMISSIONS`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Read-only.
7. **Failure Cases:** Role không tồn tại.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 5. Kiểm tra permission của user
1. **Business Goal:** Lấy danh sách các Permission thực tế của User.
2. **Actors:** System / User.
3. **Preconditions:** User tồn tại.
4. **Workflow:** Aggregate permission từ các Role đang có.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Các quyền không bị trùng lặp.
7. **Failure Cases:** Lỗi Query.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 6. Authorize request theo permission
1. **Business Goal:** Chặn API call không có quyền.
2. **Actors:** System (Interceptor).
3. **Preconditions:** JWT mang theo Permission Claims.
4. **Workflow:** Interceptor đọc Token, so khớp với yêu cầu API.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Xử lý local, không gọi Database để giảm tải.
7. **Failure Cases:** Từ chối (Lỗi 403 Forbidden).
8. **Events:** Không có.
9. **Ownership:** Auth Service.

---

## V. Phân hệ: Security (Bảo mật)

### 1. Ghi nhận login logs
1. **Business Goal:** Trace hành vi đăng nhập.
2. **Actors:** System.
3. **Preconditions:** Có attempt login.
4. **Workflow:** Ghi thông tin (IP, Agent, Status) vào `LOGIN_LOGS`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Xử lý Async không block quá trình login.
7. **Failure Cases:** Bỏ qua nếu lỗi DB.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 2. Revoke token
1. **Business Goal:** Khóa khẩn cấp token.
2. **Actors:** Admin / System.
3. **Preconditions:** Có Token ID.
4. **Workflow:** Đẩy Token vào Redis Blacklist.
5. **State Machine:** Token State -> `REVOKED`.
6. **Business Rules:** Token trong blacklist sẽ bị block ở Gateway.
7. **Failure Cases:** Lỗi Redis.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 3. Invalidate session khi đổi mật khẩu
1. **Business Goal:** Bảo vệ tài khoản khi đổi pass.
2. **Actors:** System.
3. **Preconditions:** Đổi mật khẩu thành công.
4. **Workflow:** Xóa toàn bộ refresh tokens của user.
5. **State Machine:** All Sessions -> `REVOKED`.
6. **Business Rules:** User đang dùng app sẽ bị văng ra ngay lập tức.
7. **Failure Cases:** Lỗi Cache/DB.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

---

## VI. Phân hệ: Profile & Privacy (Hồ sơ & Riêng tư)

### 1. Xem public profile user
1. **Business Goal:** Hiển thị thông tin người dùng cho người khác.
2. **Actors:** Anyone.
3. **Preconditions:** User target không bị xóa.
4. **Workflow:** Query `USER_PROFILES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Nếu `is_private=true`, ẩn `bio` và `social_links`.
7. **Failure Cases:** 404 Not Found.
8. **Events:** Không có.
9. **Ownership:** Auth Service.

### 2. Cập nhật display name
1. **Business Goal:** Đổi tên hiển thị.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Cập nhật cột `display_name` trong `USER_PROFILES`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Không chứa ký tự vi phạm, giới hạn độ dài.
7. **Failure Cases:** Lỗi Validation.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 3. Cập nhật privacy setting
1. **Business Goal:** Đổi trạng thái Public/Private.
2. **Actors:** User.
3. **Preconditions:** Đang đăng nhập.
4. **Workflow:** Update cột `is_private`.
5. **State Machine:** Không thay đổi.
6. **Business Rules:** Toggle on/off nhanh.
7. **Failure Cases:** Lỗi DB.
8. **Events:** `USER_UPDATED`.
9. **Ownership:** Auth Service.

---

## VII. Phân hệ: Event / Integration (Tích hợp & Sự kiện)

### 1. Publish user created event
1. **Business Goal:** Đồng bộ hệ thống khi có User mới.
2. **Actors:** System.
3. **Preconditions:** Đăng ký thành công.
4. **Workflow:** Ghi Event -> Broker đẩy đi -> Đổi trạng thái Outbox.
5. **State Machine:** Outbox: `PENDING` -> `COMPLETED`.
6. **Business Rules:** Payload có `user_id`, `email`.
7. **Failure Cases:** Broker Down (Event giữ PENDING).
8. **Events:** Phát `USER_CREATED`.
9. **Ownership:** Auth Service.

### 2. Publish user updated event
1. **Business Goal:** Đồng bộ tên/avatar xuống Social Service.
2. **Actors:** System.
3. **Preconditions:** Đổi profile thành công.
4. **Workflow:** Ghi Event -> Broker đẩy đi.
5. **State Machine:** Outbox: `PENDING` -> `COMPLETED`.
6. **Business Rules:** Bảo đảm Eventual Consistency.
7. **Failure Cases:** Broker Down.
8. **Events:** Phát `USER_UPDATED`.
9. **Ownership:** Auth Service.

### 3. Publish user deleted event
1. **Business Goal:** Báo hiệu các service khác ẩn dữ liệu user.
2. **Actors:** System.
3. **Preconditions:** Soft delete thành công.
4. **Workflow:** Ghi Event -> Broker đẩy đi.
5. **State Machine:** Outbox: `PENDING` -> `COMPLETED`.
6. **Business Rules:** Các service khác nhận event và Disable data thay vì xóa vật lý.
7. **Failure Cases:** Broker Down.
8. **Events:** Phát `USER_DELETED`.
9. **Ownership:** Auth Service.

### 4. Publish password changed event
1. **Business Goal:** Gửi email cảnh báo bảo mật.
2. **Actors:** System.
3. **Preconditions:** Đổi/Reset pass thành công.
4. **Workflow:** Ghi Event -> Broker -> Notification Service nhận.
5. **State Machine:** Outbox: `PENDING` -> `COMPLETED`.
6. **Business Rules:** Không gửi kèm mật khẩu mới.
7. **Failure Cases:** Broker Down.
8. **Events:** Phát `PASSWORD_CHANGED`.
9. **Ownership:** Auth Service.

### 5. Retry failed outbox events
1. **Business Goal:** Đảm bảo không mất mát thông điệp (Resilience).
2. **Actors:** System (Cron Job).
3. **Preconditions:** Có Outbox event bị kẹt `FAILED` hoặc `PENDING` lâu.
4. **Workflow:** Scan DB -> Thử đẩy lại Broker -> Tăng `retry_count`.
5. **State Machine:** Outbox: `FAILED` -> `PROCESSING` -> `COMPLETED`.
6. **Business Rules:** Giới hạn Max retries, quá hạn đưa log lỗi.
7. **Failure Cases:** Lỗi vĩnh viễn cần xử lý tay.
8. **Events:** Replay các event cũ.
9. **Ownership:** Auth Service.