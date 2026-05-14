# Password Recovery Flow

## 1. Overview
Quy trình khôi phục mật khẩu bảo mật cao. Kết nối chặt chẽ giữa Auth Service, Notification Service và Session Management.

## 2. Business Flow Diagram
```mermaid
sequenceDiagram
    actor User
    participant AuthAPI
    participant DB
    participant Kafka
    participant NotificationService

    %% Forgot Password
    User->>AuthAPI: POST /forgot-password (email)
    AuthAPI->>DB: Generate & Save VERIFICATION_TOKENS (type=PASSWORD_RESET)
    AuthAPI->>DB: Save OUTBOX_EVENTS (PASSWORD_RESET_REQUESTED)
    AuthAPI-->>User: 200 OK (Always return success to prevent email enumeration)
    
    DB-->>Kafka: Outbox Worker publish event
    Kafka-->>NotificationService: Consume Event
    NotificationService-->>User: Send Email with Reset Link/OTP

    %% Reset Password
    User->>AuthAPI: POST /reset-password (token, new_password)
    AuthAPI->>DB: Validate Token (Not expired, not used)
    AuthAPI->>DB: Update USERS (password_hash, password_changed_at)
    AuthAPI->>DB: Update VERIFICATION_TOKENS (used_at = now)
    
    %% Critical Security Step
    AuthAPI->>DB: Update ALL REFRESH_TOKEN_SESSION to REVOKED
    AuthAPI->>DB: Save OUTBOX_EVENTS (PASSWORD_CHANGED)
    
    AuthAPI-->>User: 200 OK
```

## 3. Entity Impact & Integration
- `VERIFICATION_TOKENS`: Lưu trữ token reset.
- `USERS`: Cập nhật `password_changed_at`. Cột này rất quan trọng để Gateway đối chiếu xem Access Token được cấp trước hay sau khi đổi mật khẩu (nếu cấp trước -> Reject).
- `REFRESH_TOKEN_SESSION`: Buộc vô hiệu hóa toàn bộ (`REVOKED`).
- Liên kết hệ thống: Bắn event để **Notification Service** thông báo cho user "Mật khẩu của bạn vừa bị đổi, nếu không phải bạn hãy liên hệ admin".