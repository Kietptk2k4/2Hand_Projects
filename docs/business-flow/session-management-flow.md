# Session Management Flow

## 1. Overview
Luồng này đảm bảo an toàn cho các phiên làm việc của người dùng, giúp ngăn chặn việc đánh cắp token, cho phép người dùng kiểm soát thiết bị và đảm bảo đăng xuất sạch sẽ.

## 2. Session State Machine
```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Login
    ACTIVE --> EXPIRED : TTL Timeout
    ACTIVE --> REVOKED : Admin/User Revoke
    ACTIVE --> LOGGED_OUT : User Logout
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    actor Client
    participant AuthAPI
    participant Redis_DB

    %% Login Create Session
    Client->>AuthAPI: Login Success
    AuthAPI->>Redis_DB: Insert REFRESH_TOKEN_SESSION (ACTIVE)
    AuthAPI-->>Client: Return Tokens

    %% Refresh Access Token
    Client->>AuthAPI: Request new Access Token
    AuthAPI->>Redis_DB: Check Session Status
    alt Status != ACTIVE
        AuthAPI-->>Client: 401 Unauthorized (Force re-login)
    else Status == ACTIVE
        AuthAPI-->>Client: Return New Access Token (Optional: Rotate Refresh Token)
    end

    %% Revoke / Logout All
    Client->>AuthAPI: Logout All Sessions / Change Password
    AuthAPI->>Redis_DB: Update ALL user's sessions to REVOKED
    AuthAPI-->>Client: 200 OK
```

## 4. Ngăn chặn các rủi ro bảo mật (Critical Handling)
- **Refresh Token Reuse:** Nếu hệ thống áp dụng cơ chế Rotate Refresh Token (cấp lại RT mới mỗi lần lấy AT), khi phát hiện một RT cũ (đã rotate) bị sử dụng lại, hệ thống sẽ ngay lập tức **REVOKE ALL** session của user đó vì dấu hiệu token đã bị lộ.
- **Revoke sai session:** Mọi thao tác Revoke/Logout phải verify `user_id` trong JWT khớp với `user_id` của session bị tác động.
- **Logout sạch (Invalidate):** Khi gọi "Logout All Sessions", mọi Access Token hiện tại (dù chưa hết hạn) cũng cần được chặn thông qua Redis Blacklist tại tầng API Gateway.