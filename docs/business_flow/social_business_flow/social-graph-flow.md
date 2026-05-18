# Social Graph Flow

## 1. Overview
Luồng này mô tả quản lý quan hệ theo dõi trong Social Service: follow, unfollow, xem profile user và danh sách followers/following. Luồng sử dụng bảng `FOLLOWS` để điều khiển social graph và quyền hiển thị cho `FOLLOWERS` visibility.

## 2. State Machine (Follow Relationship)
```mermaid
stateDiagram-v2
    [*] --> NONE
    NONE --> PENDING : Follow private account
    NONE --> ACCEPTED : Follow public account
    PENDING --> ACCEPTED : Approve follow request
    ACCEPTED --> NONE : Unfollow
    PENDING --> NONE : Cancel/Reject request
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    actor UserA as Follower
    actor UserB as Followee
    participant SocialAPI
    participant Pg as PostgreSQL (FOLLOWS, OUTBOX_EVENTS)
    participant Worker as Outbox Worker
    participant Broker as Message Broker

    %% Follow
    UserA->>SocialAPI: Follow UserB
    SocialAPI->>Pg: Insert FOLLOWS(follower_id, followee_id, status)
    SocialAPI->>Pg: Insert OUTBOX_EVENTS(USER_FOLLOWED, PENDING)
    SocialAPI-->>UserA: 200

    %% Unfollow
    UserA->>SocialAPI: Unfollow UserB
    SocialAPI->>Pg: Delete FOLLOWS(follower_id, followee_id)
    SocialAPI-->>UserA: 200

    %% View graph data
    UserA->>SocialAPI: View profile/followers/following
    SocialAPI->>Pg: Query FOLLOWS by follower_id/followee_id
    SocialAPI-->>UserA: Return paginated relationship data

    Worker->>Pg: Poll PENDING USER_FOLLOWED
    Worker->>Broker: Publish USER_FOLLOWED
    Worker->>Pg: Update status=PUBLISHED
```

## 4. Entity Impact
- `FOLLOWS`: tạo/xóa quan hệ follow, trạng thái `PENDING` hoặc `ACCEPTED`.
- `OUTBOX_EVENTS`: lưu event `USER_FOLLOWED`.
- Projection profile local của Social được dùng khi xem profile public.

## 5. Event Publishing
- `USER_FOLLOWED`: event MVP để Notification Service gửi thông báo.
- Có thể mở rộng `USER_UNFOLLOWED` nếu cần nghiệp vụ analytics/notification.
