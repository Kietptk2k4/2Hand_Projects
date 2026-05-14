# Authentication Lifecycle Flow

## 1. Overview
Đây là luồng cốt lõi nhất của Auth Service, quản lý toàn bộ vòng đời của một người dùng từ khi đăng ký, xác thực, đăng nhập, duy trì phiên làm việc cho đến khi đăng xuất.

## 2. State Machine (User Status)
```mermaid
stateDiagram-v2
    [*] --> PENDING_VERIFICATION : Register
    PENDING_VERIFICATION --> ACTIVE : Verify Email
    ACTIVE --> SUSPENDED : Admin Suspend
    SUSPENDED --> ACTIVE : Admin Restore
    ACTIVE --> DELETED : Soft Delete
    SUSPENDED --> DELETED : Soft Delete
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    actor Client
    participant AuthAPI
    participant DB
    participant OutboxWorker
    participant MessageBroker

    %% Register
    Client->>AuthAPI: POST /register (email, pass)
    AuthAPI->>DB: Save USERS (PENDING), PROFILES, SETTINGS
    AuthAPI->>DB: Save VERIFICATION_TOKENS (OTP)
    AuthAPI->>DB: Save OUTBOX_EVENTS (EMAIL_VERIFICATION_REQUESTED)
    DB-->>AuthAPI: Commit Transaction
    AuthAPI-->>Client: 201 Created

    OutboxWorker->>DB: Poll PENDING events
    OutboxWorker->>MessageBroker: Publish to Notification Service

    %% Verify
    Client->>AuthAPI: POST /verify-email (token)
    AuthAPI->>DB: Check Token & Update USERS (status=ACTIVE, email_verified=true)
    AuthAPI-->>Client: 200 OK

    %% Login
    Client->>AuthAPI: POST /login (email, pass)
    AuthAPI->>DB: Validate Hash
    AuthAPI->>DB: Update last_login_at & Save LOGIN_LOGS
    AuthAPI->>DB: Save REFRESH_TOKEN_SESSION
    AuthAPI-->>Client: Return Access Token & Refresh Token

    %% Refresh
    Client->>AuthAPI: POST /refresh (Refresh Token)
    AuthAPI->>DB: Validate Token Status (ACTIVE)
    AuthAPI-->>Client: Return New Access Token

    %% Logout
    Client->>AuthAPI: POST /logout
    AuthAPI->>DB: Update REFRESH_TOKEN_SESSION (status=LOGGED_OUT)
    AuthAPI-->>Client: 200 OK
```

## 4. Entity Impact
- `USERS`: Quản lý trạng thái cốt lõi (`status`, `last_login_at`).
- `VERIFICATION_TOKENS`: Lưu mã xác thực thời hạn ngắn.
- `REFRESH_TOKEN_SESSION`: Lưu trạng thái phiên đăng nhập.
- `LOGIN_LOGS`: Tracking Audit.
- `OUTBOX_EVENTS`: Đảm bảo đồng bộ Event.

## 5. Event Publishing
- `USER_CREATED`: Phát khi tạo user thành công.
- `EMAIL_VERIFICATION_REQUESTED`: Yêu cầu Notification Service gửi email.