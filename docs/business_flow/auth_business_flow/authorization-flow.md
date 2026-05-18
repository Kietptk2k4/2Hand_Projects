# Role & Permission Authorization Flow

## 1. Overview
Cơ chế kiểm soát truy cập (RBAC) cho hệ thống Microservices. Đảm bảo quyền hạn được kiểm tra tập trung, nhanh chóng tại API Gateway hoặc qua Interceptor nội bộ mà không tạo áp lực lên Database.

## 2. Business Flow Diagram
```mermaid
sequenceDiagram
    actor Client
    participant API_Gateway
    participant AuthAPI
    participant DB
    participant TargetService (Commerce/Social)

    %% Login & JWT Generation
    Client->>AuthAPI: POST /login
    AuthAPI->>DB: Get User Roles & Permissions
    AuthAPI->>AuthAPI: Sign JWT (Inject Permissions into payload)
    AuthAPI-->>Client: Return JWT

    %% Request Authorization
    Client->>API_Gateway: Request with Bearer JWT
    API_Gateway->>API_Gateway: Verify JWT Signature & Exp
    API_Gateway->>API_Gateway: Extract Permissions from Claims
    
    alt Missing Permission
        API_Gateway-->>Client: 403 Forbidden
    else Has Permission
        API_Gateway->>TargetService: Forward Request (with User-Id Header)
        TargetService-->>Client: 200 OK (Process Business Logic)
    end
```

## 3. JWT Claim Design
- Thông tin phân quyền (Permission codes) được đính kèm (embedded) trực tiếp vào Payload của Access Token.
- Việc này giúp hệ thống xác thực Stateless: API Gateway chỉ cần parse JWT là biết user có quyền `CREATE_PRODUCT` hay `BAN_USER` không, không cần gọi lại DB của Auth Service.
- Giới hạn: Khi Admin đổi quyền của User, User phải đăng nhập lại (hoặc gọi refresh token) để JWT mới nhận được claim mới.