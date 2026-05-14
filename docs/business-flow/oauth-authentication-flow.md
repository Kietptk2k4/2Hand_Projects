# OAuth Authentication Flow

## 1. Overview
Quy trình đăng nhập/đăng ký liền mạch thông qua Google, Facebook. Đảm bảo ánh xạ (mapping) chính xác dữ liệu từ Provider vào hệ thống 2Hands.

## 2. Business Flow Diagram
```mermaid
sequenceDiagram
    actor Client
    participant AuthAPI
    participant OAuthProvider (Google/FB)
    participant DB

    Client->>OAuthProvider: Login & Consent
    OAuthProvider-->>Client: Return OAuth Token
    Client->>AuthAPI: POST /oauth/login (provider, token)
    AuthAPI->>OAuthProvider: Verify Token & Get Profile
    OAuthProvider-->>AuthAPI: User Info (Email, Name, Avatar)

    AuthAPI->>DB: Check if Email exists in USERS
    alt Existing User
        AuthAPI->>DB: Ensure OAUTH_ACCOUNTS link exists
        AuthAPI->>DB: Update last_login_at
    else New User
        AuthAPI->>DB: Save USERS (status=ACTIVE, email_verified=true)
        AuthAPI->>DB: Save USER_PROFILES
        AuthAPI->>DB: Save OAUTH_ACCOUNTS
        AuthAPI->>DB: Save OUTBOX_EVENTS (USER_CREATED)
    end

    AuthAPI->>DB: Save LOGIN_LOGS
    AuthAPI->>DB: Generate & Save REFRESH_TOKEN_SESSION
    AuthAPI-->>Client: Return JWT Tokens
```

## 3. Business Linking
- Bảng `OAUTH_ACCOUNTS` cho phép 1 user (`user_id`) có thể map với nhiều provider khác nhau (Vd: Cùng 1 email đăng nhập bằng cả Google và Facebook).
- Ưu tiên sử dụng **Email** làm khóa định danh đồng nhất.
- Tài khoản tạo qua OAuth được bypass trạng thái `PENDING_VERIFICATION` và chuyển thẳng sang `ACTIVE`.