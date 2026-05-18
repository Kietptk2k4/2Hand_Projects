# User Profile & Privacy Flow

## 1. Overview
Quản lý thông tin hiển thị của người dùng và đảm bảo tính nhất quán dữ liệu (Eventual Consistency) với các service khác như Social Service.

## 2. Business Flow Diagram
```mermaid
sequenceDiagram
    actor User
    participant AuthAPI
    participant DB
    participant Kafka
    participant SocialService

    User->>AuthAPI: PUT /profile (display_name, avatar, is_private)
    AuthAPI->>DB: Update USER_PROFILES
    AuthAPI->>DB: Update USER_SETTINGS
    AuthAPI->>DB: Save OUTBOX_EVENTS (USER_UPDATED)
    AuthAPI-->>User: 200 OK

    DB-->>Kafka: Outbox Worker publish
    Kafka-->>SocialService: Consume USER_UPDATED event
    SocialService->>SocialService: Update local user cache (MongoDB)
```

## 3. Eventual Consistency & Privacy
- **Profile Ownership:** Auth Service là Nguồn sự thật (Source of Truth) cho thông tin Profile.
- **Data Sync:** Social Service không gọi trực tiếp AuthAPI mỗi khi load Feed. Nó nhận event `USER_UPDATED` để tự đồng bộ `display_name` và `avatar_url` trong MongoDB cục bộ.
- **Privacy Visibility:** Trường `is_private` trong `USER_PROFILES` sẽ quyết định xem các API Get Profile public có trả về thông tin chi tiết (bio, social_links) hay không. Cờ này cũng được sync sang Social Service để ẩn/hiện bài viết.