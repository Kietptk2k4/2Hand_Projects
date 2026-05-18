# Functional Requirement (FR) - Đăng nhập bằng Email/Password

## 1. Feature Overview
Chức năng cho phép người dùng đã đăng ký tài khoản xác thực định danh bằng Email và Mật khẩu để truy cập vào hệ thống 2Hands. Sau khi xác thực thành công, hệ thống sẽ cấp phát phiên làm việc thông qua Access Token và Refresh Token, đồng thời ghi nhận lịch sử đăng nhập.

## 2. Actors
* **Guest (Khách):** Người dùng chưa đăng nhập, thao tác trên màn hình Login.
* **User (Người dùng):** Chủ sở hữu tài khoản đang thực hiện xác thực.

## 3. Scope
* **In Scope:**
  * Tiếp nhận và validate thông tin đăng nhập (Email, Password).
  * Kiểm tra trạng thái tài khoản.
  * Sinh và cấp phát Access Token (JWT) & Refresh Token.
  * Cập nhật thời gian đăng nhập cuối (`last_login_at`).
  * Ghi nhận lịch sử đăng nhập vào bảng `LOGIN_LOGS`.
* **Out of Scope:**
  * Đăng nhập bằng OAuth (Google/Facebook) (thuộc FR riêng).
  * Xác thực đa yếu tố (2FA / OTP).
  * Chức năng Quên mật khẩu (thuộc FR riêng).

## 4. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `email` | string | Yes | Định dạng chuẩn email. | "Vui lòng nhập đúng định dạng email." |
| `password` | string | Yes | Không được để trống. | "Vui lòng nhập mật khẩu." |

## 5. Preconditions
* Người dùng đã đăng ký tài khoản thành công bằng email.
* Tài khoản không nằm trong trạng thái `DELETED` hoặc `SUSPENDED`.

## 6. Business Rules
* **Bảo mật thông báo lỗi:** Để chống dò quét tài khoản (Account Enumeration), nếu sai Email hoặc sai Password, hệ thống luôn trả về một thông báo lỗi chung: *"Email hoặc mật khẩu không chính xác."*
* **Kiểm tra trạng thái User:** * Nếu `status` = `SUSPENDED` -> Chặn đăng nhập, trả về lỗi 403 kèm lý do.
  * Nếu `status` = `DELETED` -> Chặn đăng nhập (báo lỗi tài khoản không tồn tại hoặc sai thông tin).
  * Nếu `status` = `PENDING_VERIFICATION` -> Vẫn cho phép đăng nhập nhưng token trả về có thể chứa claims hạn chế, Frontend sẽ điều hướng sang màn hình yêu cầu xác thực Email.
* **Token Generation:** Access Token có thời hạn ngắn (vd: 15-30 phút). Refresh Token có thời hạn dài (vd: 7-30 ngày).

## 7. API Contract
**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Response - 200 OK:**
```json
{
  "code": 200,
  "message": "Đăng nhập thành công.",
  "data": {
    "access_token": "eyJhbG...",
    "refresh_token": "def456...",
    "expires_in": 1800,
    "user": {
      "id": "uuid-123",
      "email": "user@example.com",
      "status": "ACTIVE"
    }
  }
}
```

**Response - 401 Unauthorized:**
```json
{
  "code": 401,
  "message": "Email hoặc mật khẩu không chính xác."
}
```

**Response - 403 Forbidden:**
```json
{
  "code": 403,
  "message": "Tài khoản của bạn đã bị khóa tạm thời."
}
```

## 8. Database Impact
* **USERS:** Cập nhật trường `last_login_at = now()` khi đăng nhập thành công.
* **LOGIN_LOGS:** Thêm mới 1 bản ghi lịch sử `(user_id, login_method='EMAIL', ip_address, user_agent, success_status)`. Áp dụng cho cả trường hợp đăng nhập thành công (`success_status = true`) và sai mật khẩu (`success_status = false`).

## 9. Event Flow
1. Client gửi request đăng nhập (Email, Password) lên Auth Service.
2. Auth Service validate request format (nếu sai trả 400).
3. Auth Service truy vấn bảng `USERS` theo `email`.
   * Nếu không tìm thấy: Ghi log thất bại (tuỳ chọn) -> Trả về 401.
4. Auth Service so sánh hash của `password` gửi lên với `password_hash` trong DB.
   * Nếu không khớp: Ghi `LOGIN_LOGS` (`success_status = false`) -> Trả về 401.
5. Auth Service kiểm tra `status` của tài khoản:
   * Nếu `SUSPENDED`: Trả về 403.
6. Xác thực thành công:
   * Generate Access Token và Refresh Token.
   * Update `last_login_at` trong `USERS`.
   * Insert dòng mới vào `LOGIN_LOGS` (`success_status = true`).
7. Trả về kết quả 200 OK kèm Tokens cho Client.

## 10. Edge Cases
* **Tấn công Brute-force:** User hoặc Bot cố tình thử sai mật khẩu liên tục. Hệ thống cần giới hạn số lần thử sai (vd: 5 lần/15 phút) trên mỗi IP hoặc Email, nếu vượt quá sẽ tạm khóa chức năng đăng nhập của đối tượng đó.
* **Đăng nhập đồng thời:** Cùng 1 tài khoản đăng nhập thành công trên thiết bị B trong khi vẫn đang online ở thiết bị A. (Theo nghiệp vụ MVP mặc định: Chấp nhận đa phiên, quản lý qua Session Management).

## 11. Security
* Không lưu trữ hoặc log `password` plaintext (mật khẩu dạng rõ) ở bất kỳ đâu (console, APM, DB).
* Sử dụng HTTPS/TLS để mã hóa dữ liệu truyền tải trên mạng.
* Chống Brute-force bằng **Rate Limiting** ở tầng API Gateway hoặc Auth Service.
* Thông báo lỗi mập mờ (Generic Error) cho mã 401.

## 12. FE Behavior
* Form có các ô: Email, Password, Checkbox "Ghi nhớ đăng nhập" (tùy chọn), và liên kết "Quên mật khẩu?".
* Có nút con mắt (Toggle) để xem/ẩn password.
* Khi nhấn "Đăng nhập", hiển thị Loading Spinner trên nút và disable form.
* Báo lỗi inline (ngay dưới ô input) nếu bỏ trống hoặc sai định dạng.
* Nhận HTTP 200: Lưu Token vào LocalStorage/Cookies bảo mật, chuyển hướng vào trang chủ (Home) hoặc trang `redirectUrl` trước đó.
* Nhận HTTP 401/403: Hiển thị Toast Message màu đỏ thông báo lỗi từ API.

## 13. Acceptance Criteria
* **AC1:** Người dùng nhập đúng Email và Password của một tài khoản ACTIVE -> Đăng nhập thành công, nhận token, DB cập nhật `last_login_at` và `LOGIN_LOGS`.
* **AC2:** Người dùng nhập sai Email hoặc Password -> Hệ thống từ chối đăng nhập, trả mã 401 với câu báo lỗi chung chung "Email hoặc mật khẩu không chính xác", lưu `LOGIN_LOGS` thất bại.
* **AC3:** Người dùng đăng nhập vào tài khoản có status `SUSPENDED` -> Hệ thống từ chối, trả mã 403 "Tài khoản của bạn đã bị khóa".
* **AC4:** Sau 5 lần nhập sai mật khẩu liên tiếp (từ 1 IP/Email), tính năng đăng nhập cho người dùng đó bị tạm khóa (Rate limited).