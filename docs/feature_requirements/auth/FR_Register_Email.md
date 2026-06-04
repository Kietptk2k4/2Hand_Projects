# Functional Requirement (FR) - Đăng ký tài khoản bằng Email

## 1. Feature Overview
Chức năng cho phép người dùng (khách) tạo mới một tài khoản trên hệ thống 2Hands bằng địa chỉ email cá nhân. Sau khi đăng ký thành công, tài khoản sẽ ở trạng thái chờ xác thực (`PENDING_VERIFICATION`) và hệ thống sẽ gửi email chứa **mã OTP 6 chữ số** để kích hoạt tài khoản (không dùng verification link).

## 2. Actors
* **Guest (Khách):** Người dùng chưa xác thực, có nhu cầu tham gia vào hệ thống.

## 3. Scope
* **In Scope:** * Tiếp nhận thông tin email và password.
  * Validate dữ liệu đầu vào.
  * Mã hóa mật khẩu và lưu vào Database.
  * Khởi tạo profile và settings mặc định.
  * Ghi nhận event vào bảng Outbox để yêu cầu gửi email xác thực.
* **Out of Scope:** * Đăng ký bằng số điện thoại (chưa làm trong MVP).
  * Gửi lại email xác thực — xem `FR_ResendEmailVerification.md`.
  * Luồng xác thực email (Verify OTP - là một chức năng riêng).

## 4. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `email` | string | Yes | Format chuẩn email, độ dài tối đa 255 ký tự. Không được trùng với tài khoản đã tồn tại ở trạng thái ACTIVE/SUSPENDED. | "Email không đúng định dạng" / "Email đã được sử dụng" |
| `password` | string | Yes | Độ dài 8-32 ký tự, ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số. | "Mật khẩu phải từ 8-32 ký tự, bao gồm chữ hoa, chữ thường và số" |

## 5. Preconditions
* Người dùng có kết nối mạng hợp lệ.
* Địa chỉ email cung cấp chưa được đăng ký trong hệ thống (hoặc đã đăng ký nhưng ở trạng thái `DELETED`).

## 6. Business Rules
* **Mã hóa (Hashing):** Mật khẩu phải được băm (hash) bằng thuật toán mạnh (ví dụ: bcrypt, Argon2) kèm salt trước khi lưu, tuyệt đối không lưu plaintext.
* **Trạng thái khởi tạo:** `status` mặc định là `PENDING_VERIFICATION`, `email_verified` = `false`.
* **Khởi tạo dữ liệu liên quan:** Khi tạo bản ghi ở `USERS`, bắt buộc phải tạo kèm bản ghi trống/mặc định ở `USER_PROFILES` (display_name tự tạo ngẫu nhiên từ email) và `USER_SETTINGS`.
* **Outbox Pattern:** Mọi thao tác ghi DB (`USERS`, `USER_PROFILES`, `USER_SETTINGS`, `OUTBOX_EVENTS`) phải nằm trong một transaction duy nhất (ACID).

## 7. API Contract
**Endpoint:** `POST /api/v1/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Response - 201 Created:**
```json
{
  "code": 201,
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.",
  "data": {
    "user_id": "uuid-1234-5678",
    "email": "user@example.com",
    "status": "PENDING_VERIFICATION"
  }
}
```

**Response - 409 Conflict:**
```json
{
  "code": 409,
  "message": "Email đã được sử dụng."
}
```

## 8. Database Impact
Thực thi transaction ghi vào các bảng (PostgreSQL):
1. **USERS:** Insert dòng mới `(id, email, password_hash, status='PENDING_VERIFICATION', email_verified=false, created_at)`.
2. **USER_PROFILES:** Insert dòng mới `(user_id, display_name)`.
3. **USER_SETTINGS:** Insert dòng mới `(user_id, appearance_mode='SYSTEM')`.
4. **OUTBOX_EVENTS:** Insert dòng mới `(id, event_type='EMAIL_VERIFICATION_REQUESTED', payload=..., status='PENDING')`. Payload gồm `verification_code` (OTP 6 chữ số; có thể kèm field legacy `verification_token` cùng giá trị OTP cho tương thích Kafka).
5. **VERIFICATION_TOKENS:** Insert bản ghi `EMAIL_VERIFY` với `token_hash` (hash OTP, không lưu plaintext).

## 9. Event Flow
1. API Gateway nhận Request và chuyển tới Auth Service.
2. Auth Service validate Request. Nếu failed -> Trả lỗi 400.
3. Kiểm tra DB xem email đã tồn tại chưa. Nếu có -> Trả lỗi 409.
4. Mở DB Transaction.
5. Hash Password và lưu `USERS`, `USER_PROFILES`, `USER_SETTINGS`.
6. Insert event `EMAIL_VERIFICATION_REQUESTED` vào `OUTBOX_EVENTS`.
7. Commit DB Transaction.
8. Trả Response 201 cho Frontend.
9. (Asynchronous) Worker của Auth Service đọc `OUTBOX_EVENTS` và đẩy vào Message Broker (Kafka) để Notification Service gửi email chứa **mã OTP** (không build verification link).

## 10. Edge Cases
* **Email đã đăng ký nhưng chưa verify (PENDING_VERIFICATION) và OTP đã hết hạn:** Hệ thống xử lý thế nào? (Giải pháp: Có thể overwrite tài khoản cũ hoặc hướng dẫn người dùng qua màn hình Resend OTP).
* **Trùng lúc nhiều request đăng ký cùng 1 email:** Dựa vào constraints `UNIQUE` trên database (`email_normalized`) để block các request đến sau, throw exception và trả về 409.

## 11. Security
* **Rate Limiting:** Giới hạn số lượng request đăng ký từ 1 IP (vd: tối đa 5 requests/giờ) để chống bot spam tạo tài khoản ảo.
* **Logging:** Tuyệt đối không log thông tin password trong server logs hay APM.
* **SQL Injection:** Dùng Parameterized Queries/ORM để ngăn chặn SQLi.

## 12. FE Behavior
* Form đăng ký bao gồm 3 trường: Email, Password, Confirm Password.
* **Inline validation:**
  * Hiển thị lỗi ngay khi người dùng gõ sai format email hoặc mật khẩu không đủ độ mạnh.
  * Báo lỗi ngay nếu Password và Confirm Password không khớp.
* Nút "Đăng ký" bị disable cho đến khi tất cả các trường hợp lệ.
* Hiển thị loading spinner trên nút khi đang gọi API.
* Chuyển hướng sang màn hình "Kiểm tra Email (Nhập OTP)" nếu nhận được HTTP 201.
* Hiển thị Toast Message báo lỗi rõ ràng nếu nhận được HTTP 409 (Email tồn tại) hoặc 400.

## 13. Acceptance Criteria
* **AC1:** Giao diện cho phép khách nhập email, mật khẩu và xác nhận mật khẩu.
* **AC2:** Nếu nhập email đúng định dạng, mật khẩu hợp lệ và gửi form thành công, dữ liệu phải được lưu vào DB ở trạng thái `PENDING_VERIFICATION` và màn hình chuyển sang trang nhập mã xác nhận.
* **AC3:** Nếu gửi email đã tồn tại (ở trạng thái ACTIVE), hệ thống trả về thông báo lỗi "Email đã được sử dụng" và không tạo dữ liệu rác.
* **AC4:** Database tạo thành công 1 bản ghi trong bảng `OUTBOX_EVENTS` cùng lúc với việc tạo user, đảm bảo không xảy ra bất đồng bộ dữ liệu.
* **AC5:** Nếu hệ thống bị spam đăng ký liên tục từ 1 IP, hệ thống phải chặn lại bằng Rate Limiting (thông báo "Bạn thao tác quá nhanh, thử lại sau").