# Functional Requirement (FR) - Đăng nhập bằng OAuth (Google, Facebook)

## 1. Feature Overview
Chức năng cho phép người dùng đăng nhập hoặc đăng ký tài khoản mới vào hệ thống 2Hands một cách nhanh chóng và liền mạch thông qua tài khoản mạng xã hội (Google, Facebook). Thay vì phải ghi nhớ mật khẩu, hệ thống sẽ sử dụng định danh từ nhà cung cấp dịch vụ (Identity Provider - IdP) để xác thực.

## 2. Actors
* **Guest (Khách):** Người dùng chưa có tài khoản, muốn tham gia hệ thống thông qua Google/Facebook.
* **User (Người dùng):** Người dùng đã có tài khoản (có thể tạo trước đó bằng Email hoặc OAuth), muốn đăng nhập nhanh.

## 3. Scope
* **In Scope:**
  * Xác thực mã/token (Authorization Code hoặc ID Token) từ Google và Facebook.
  * Tự động đăng ký tài khoản mới nếu email từ nhà cung cấp chưa tồn tại trong hệ thống.
  * Tự động đăng nhập và cấp Access/Refresh Token nếu email đã tồn tại.
  * Ghi nhận lịch sử đăng nhập.
  * Đẩy Outbox Event nếu phát sinh đăng ký mới.
* **Out of Scope:**
  * Hủy liên kết (Unlink) tài khoản mạng xã hội.
  * Đăng nhập bằng Apple ID, X (Twitter) (Chưa có trong MVP).

## 4. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `provider` | string | Yes | Chỉ chấp nhận giá trị: `GOOGLE`, `FACEBOOK`. | "Nhà cung cấp không hợp lệ." |
| `token` | string | Yes | ID Token hoặc Access Token lấy từ phía Client cung cấp. Không được rỗng. | "Thiếu thông tin xác thực từ nhà cung cấp." |

## 5. Preconditions
* Khách hàng đã cấp quyền truy cập thông tin cơ bản (Đặc biệt là **Email**) trên màn hình Consent của Google/Facebook.
* Hệ thống 2Hands đã được cấu hình hợp lệ (Client ID, Client Secret) trên Google Cloud Console và Facebook Developer.

## 6. Business Rules
* **Ưu tiên Email làm định danh:** Sử dụng Email trả về từ Google/Facebook làm khóa chính để map với tài khoản hệ thống.
* **Tự động Verify Email:** Tài khoản được tạo qua kênh OAuth mặc định có `status = ACTIVE` và `email_verified = true` (do Google/Facebook đã verify).
* **Map với tài khoản cũ:** Nếu người dùng đã tạo tài khoản bằng Email/Password trước đó, khi dùng OAuth có cùng email, hệ thống sẽ tự động map (gộp) tài khoản và cho phép đăng nhập thành công.
* **Trạng thái tài khoản:** Không cho phép đăng nhập nếu tài khoản đang bị `SUSPENDED` hoặc `DELETED`.

## 7. API Contract
**Endpoint:** `POST /api/v1/auth/oauth/login`

**Request Body:**
```json
{
  "provider": "GOOGLE",
  "token": "eyJhbGciOiJSUz..."
}
```

**Response - 200 OK (Thành công cho cả Đăng ký mới & Đăng nhập):**
```json
{
  "code": 200,
  "message": "Xác thực thành công.",
  "data": {
    "is_new_user": true, 
    "access_token": "eyJhbG...",
    "refresh_token": "def456...",
    "expires_in": 1800,
    "user": {
      "id": "uuid-123",
      "email": "user@gmail.com",
      "status": "ACTIVE"
    }
  }
}
```

**Response - 400 Bad Request (Lỗi thiếu Email scope):**
```json
{
  "code": 400,
  "message": "Vui lòng cấp quyền truy cập Email để sử dụng tính năng này."
}
```

## 8. Database Impact
* **Trường hợp User mới:**
  * **USERS:** Insert dòng mới `(id, email, status='ACTIVE', email_verified=true, password_hash=null, last_login_at=now())`.
  * **USER_PROFILES:** Insert thông tin được IdP trả về `(user_id, display_name, avatar_url)`.
  * **USER_SETTINGS:** Insert dòng mới thiết lập mặc định.
  * **OUTBOX_EVENTS:** Insert sự kiện `USER_CREATED` (vì tạo mới).
* **Trường hợp User đã tồn tại:**
  * **USERS:** Update `last_login_at = now()`.
* **Mọi trường hợp:**
  * **LOGIN_LOGS:** Insert dòng mới ghi log đăng nhập `(user_id, login_method=provider, ip_address, success_status=true)`.

## 9. Event Flow
1. Frontend gọi SDK của Google/FB, người dùng đồng ý cấp quyền -> FE nhận được `token`.
2. FE gửi `POST /api/v1/auth/oauth/login` kèm `provider` và `token` xuống Auth Service.
3. Auth Service verify `token` với server của Google/FB.
   * Nếu token sai/hết hạn -> Trả 401 Unauthorized.
4. Lấy thông tin Profile (Email, Name, Picture) từ Google/FB.
   * Nếu không lấy được Email -> Trả 400 Yêu cầu cấp quyền.
5. Auth Service kiểm tra `email` trong bảng `USERS`:
   * **Nếu chưa có:**
     * Mở DB Transaction.
     * Insert `USERS`, `USER_PROFILES`, `USER_SETTINGS`.
     * Insert `OUTBOX_EVENTS` (`USER_CREATED`).
     * Commit Transaction. Đánh dấu cờ `is_new_user = true`.
   * **Nếu đã có:**
     * Kiểm tra `status`. Nếu `SUSPENDED`/`DELETED` -> Trả 403 Forbidden.
     * Update `last_login_at`. Đánh dấu cờ `is_new_user = false`.
6. Auth Service cấp phát Access Token và Refresh Token.
7. Insert vào `LOGIN_LOGS` trạng thái thành công.
8. Trả về HTTP 200 kèm Token cho Frontend.

## 10. Edge Cases
* **Người dùng từ chối chia sẻ Email (Facebook):** Facebook cho phép người dùng bỏ tick chia sẻ email. Hệ thống sẽ bắt buộc phải có email, nếu không trả về lỗi thông báo "Yêu cầu cung cấp Email".
* **Thay đổi avatar trên Google/Facebook:** Nếu User đăng nhập lại, hệ thống có đè avatar mới vào không? (Theo MVP: Chỉ lấy ở lần đăng ký đầu tiên, về sau User tự đổi trên hệ thống 2Hands, không đè tự động để tránh mất dữ liệu tuỳ chỉnh của user).
* **Google API/Facebook API timeout:** Lỗi gọi sang hệ thống 3rd-party. Trả về HTTP 502 Bad Gateway kèm thông báo "Dịch vụ xác thực bên thứ 3 đang gián đoạn".

## 11. Security
* **Xác thực Token:** Phía backend bắt buộc phải verify chữ ký số (Signature) và tham số Audience (`aud` phải trùng với Client ID của 2Hands) của ID Token, tuyệt đối không tin tưởng hoàn toàn dữ liệu payload mà frontend truyền lên.
* **Bảo mật tài khoản:** OAuth Flow không sinh mật khẩu, nếu user bị lộ tài khoản Google thì tài khoản hệ thống cũng bị ảnh hưởng. Nếu có hành động nhạy cảm về sau cần xác minh 2 bước hoặc xác minh lại (ngoài phạm vi MVP).
* **Chống Replay Attack:** Verify thuộc tính thời hạn (exp) và `nonce` trong OAuth token để đảm bảo token không bị dùng lại.

## 12. FE Behavior
* Màn hình Login/Register hiển thị 2 nút: "Tiếp tục với Google" và "Tiếp tục với Facebook".
* Khi click, hiển thị SDK Popup hoặc Redirect sang trang của Google/FB.
* Nhận kết quả từ Google/FB -> Hiển thị Loading Spinner che toàn màn hình và gọi API của hệ thống 2Hands.
* Xử lý HTTP 200: Lưu Token vào hệ thống (Local Storage / Secure Cookie).
  * Nếu `is_new_user = true`: Điều hướng sang trang "Cập nhật hồ sơ (Onboarding)" để người dùng bổ sung số điện thoại (tùy chọn).
  * Nếu `is_new_user = false`: Điều hướng thẳng vào trang Home.
* Xử lý lỗi 40x: Hiển thị Toast Message báo lỗi.

## 13. Acceptance Criteria
* **AC1:** Người dùng chọn Google/FB (chưa từng đăng ký) -> Đăng ký thành công, trạng thái ACTIVE, có đẩy event Outbox `USER_CREATED`, trả về Tokens, tự động map Name/Avatar từ mạng xã hội.
* **AC2:** Người dùng chọn Google/FB (đã đăng ký bằng Email từ trước, trùng email) -> Đăng nhập thành công, không tạo thêm tài khoản mới, cấp lại Token.
* **AC3:** Người dùng gửi token hết hạn hoặc giả mạo -> Hệ thống từ chối xác thực (401 Unauthorized), ghi log lỗi.
* **AC4:** Người dùng có tài khoản đang bị SUSPENDED dùng OAuth để đăng nhập -> Hệ thống từ chối với lỗi 403.
* **AC5:** Backend có cơ chế xác minh Audience của Token trùng khớp với Client ID của hệ thống.