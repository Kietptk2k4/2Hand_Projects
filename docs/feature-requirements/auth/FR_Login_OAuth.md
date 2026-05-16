# Functional Requirement (FR) - Đăng nhập bằng OAuth (Google, Facebook)

## 1. Feature Overview

Chức năng cho phép người dùng đăng nhập hoặc đăng ký tài khoản mới vào hệ thống 2Hands thông qua tài khoản Google hoặc Facebook bằng chuẩn OAuth2 Authorization Code Flow do Auth Service quản lý.

Hệ thống sử dụng Spring Security OAuth2 Client để thực hiện:

- Redirect người dùng sang Google/Facebook
- Nhận callback xác thực
- Lấy thông tin hồ sơ người dùng
- Tự động đăng ký hoặc đăng nhập
- Cấp phát JWT Access Token và Refresh Token cho hệ thống 2Hands

---

# 2. Actors

## Guest (Khách)

Người dùng chưa có tài khoản, muốn tham gia hệ thống thông qua Google/Facebook.

## User (Người dùng)

Người dùng đã có tài khoản trước đó, muốn đăng nhập nhanh bằng OAuth.

---

# 3. Scope

## In Scope

- Đăng nhập bằng Google OAuth2
- Đăng nhập bằng Facebook OAuth2
- OAuth2 Authorization Code Flow
- Tự động tạo tài khoản mới nếu email chưa tồn tại
- Tự động map với tài khoản email/password đã tồn tại
- Tự động verify email từ nhà cung cấp OAuth
- Cấp JWT Access Token và Refresh Token
- Ghi LOGIN_LOGS
- Gửi OUTBOX_EVENTS khi phát sinh user mới

## Out of Scope

- Unlink tài khoản OAuth
- Apple Login
- X/Twitter Login
- MFA cho OAuth
- OAuth account management UI

---

# 4. OAuth Providers

| Provider | Authorization Endpoint |
|---|---|
| GOOGLE | `/oauth2/authorization/google` |
| FACEBOOK | `/oauth2/authorization/facebook` |

---

# 5. Preconditions

- Auth Service đã cấu hình OAuth2 Client hợp lệ trên Google Cloud Console/Facebook Developer.
- Frontend có thể redirect browser sang Auth Service.
- Người dùng cấp quyền Email và Profile trên màn hình consent.

---

# 6. Business Rules

## 6.1 Email là định danh chính

Email từ Google/Facebook được dùng làm khóa định danh duy nhất để map tài khoản hệ thống.

---

## 6.2 Tự động verify email

User tạo từ OAuth mặc định:

```txt
status = ACTIVE
email_verified = true
```

---

## 6.3 Map với tài khoản cũ

Nếu email OAuth trùng với tài khoản email/password đã tồn tại:

- Không tạo user mới
- Tự động đăng nhập
- Không tạo duplicate account

---

## 6.4 Trạng thái tài khoản

Không cho phép đăng nhập nếu:

```txt
status = SUSPENDED
status = DELETED
```

Trả:

```txt
403 Forbidden
```

---

## 6.5 Avatar đồng bộ

- Chỉ lấy avatar OAuth ở lần đăng ký đầu tiên
- Không tự động overwrite avatar ở các lần login sau

---

# 7. API Contract

## 7.1 OAuth Login Endpoint

### Google

```http
GET /oauth2/authorization/google
```

### Facebook

```http
GET /oauth2/authorization/facebook
```

---

## 7.2 OAuth Callback Endpoint

### Google

```http
GET /login/oauth2/code/google
```

### Facebook

```http
GET /login/oauth2/code/facebook
```

Endpoint callback được Spring Security quản lý nội bộ.

Frontend không gọi trực tiếp endpoint này.

---

# 8. OAuth Flow

## 8.1 Login Flow

1. User click:
   - "Continue with Google"
   - hoặc "Continue with Facebook"

2. Frontend redirect browser sang:

```txt
/oauth2/authorization/{provider}
```

3. Auth Service redirect sang Google/Facebook OAuth Consent Screen.

4. User xác thực và cấp quyền.

5. Google/Facebook callback về:

```txt
/login/oauth2/code/{provider}
```

6. Spring Security:
   - Exchange authorization code
   - Lấy access token từ provider
   - Lấy user profile

7. Auth Service xử lý business:
   - Tạo user mới nếu chưa tồn tại
   - Hoặc đăng nhập nếu user đã tồn tại

8. Auth Service cấp:
   - Access Token
   - Refresh Token

9. Auth Service redirect về Frontend.

---

# 9. Success Response

## Redirect Success

Backend redirect frontend:

```txt
http://localhost:5173/oauth/success
```

Kèm:

- access token
- refresh token
- trạng thái onboarding

Implementation thực tế:

- Secure Cookie
- hoặc query param
- hoặc temporary auth code

(Chi tiết nằm ở Security Architecture Spec)

---

# 10. Error Response

| Case | HTTP Status | Message |
|---|---|---|
| OAuth token invalid | 401 | "Xác thực OAuth thất bại." |
| Không lấy được email | 400 | "Vui lòng cấp quyền Email để sử dụng tính năng này." |
| User bị khóa | 403 | "Tài khoản hiện không khả dụng." |
| OAuth provider timeout | 502 | "Dịch vụ xác thực bên thứ 3 đang gián đoạn." |

---

# 11. Database Impact

## 11.1 User mới

### USERS

Insert:

```txt
status = ACTIVE
email_verified = true
password_hash = null
last_login_at = now()
```

### USER_PROFILES

Insert:

- display_name
- avatar_url

### USER_SETTINGS

Insert default settings.

### OUTBOX_EVENTS

Insert:

```txt
USER_CREATED
```

---

## 11.2 User đã tồn tại

### USERS

Update:

```txt
last_login_at = now()
```

---

## 11.3 Mọi trường hợp

### LOGIN_LOGS

Insert:

- user_id
- login_method
- provider
- ip_address
- success_status

---

# 12. Security

## 12.1 OAuth2 Authorization Code Flow

Hệ thống sử dụng:

```txt
OAuth2 Authorization Code Flow
```

được quản lý bởi Spring Security OAuth2 Client.

---

## 12.2 CSRF & State Protection

Spring Security tự động:

- generate state
- verify state
- chống CSRF OAuth attack

---

## 12.3 Token Verification

Auth Service phải verify:

- access token
- user info response
- provider identity

Không tin tưởng dữ liệu từ frontend.

---

## 12.4 Audience Validation

Provider response phải đúng với:

- configured client id
- registered OAuth application

---

## 12.5 HTTPS

Production environment bắt buộc sử dụng HTTPS.

---

# 13. FE Behavior

## Login Button

Frontend hiển thị:

- Continue with Google
- Continue with Facebook

---

## Redirect

Frontend redirect browser:

```javascript
window.location.href =
  "http://localhost:3001/oauth2/authorization/google";
```

---

## Loading

Trong quá trình OAuth:

- Hiển thị loading overlay
- Disable login actions

---

## Login Success

### Nếu user mới

Redirect:

```txt
onboarding page
```

### Nếu user cũ

Redirect:

```txt
home page
```

---

## Login Failure

Hiển thị:

- toast error
- retry option

---

# 14. Technical Notes

## Spring Security Components

- OAuth2LoginAuthenticationFilter
- OAuth2UserService
- OAuth2AuthenticationSuccessHandler
- OAuth2AuthenticationFailureHandler

---

## OAuth Providers Config

```yaml
spring.security.oauth2.client.registration
```

---

## OAuth Redirect URI

### Google

```txt
http://localhost:3001/login/oauth2/code/google
```

### Facebook

```txt
http://localhost:3001/login/oauth2/code/facebook
```

---

# 15. Acceptance Criteria

## AC1

User chưa tồn tại:

- OAuth login thành công
- Tạo USERS/PROFILE/SETTINGS
- Đẩy USER_CREATED event
- Trả JWT tokens

---

## AC2

User đã tồn tại:

- Không tạo duplicate account
- Đăng nhập thành công
- Cấp JWT mới

---

## AC3

OAuth callback không hợp lệ:

- Trả 401 Unauthorized
- Ghi log lỗi

---

## AC4

User bị SUSPENDED:

- Không cho login
- Trả 403 Forbidden

---

## AC5

OAuth flow có:

- state validation
- CSRF protection
- authorization code exchange

---

## AC6

Google/Facebook redirect URI khớp với OAuth provider configuration.