# Feed Delivery Flow

## 1. Overview
Luồng này mô tả cách Social Service trả về Global Feed và Following Feed. Hệ thống chỉ trả post `ACTIVE`, áp dụng filter visibility (`PUBLIC`, `FOLLOWERS`) và quan hệ follow hợp lệ để đảm bảo đúng quyền truy cập.

## 2. State Machine (Feed Eligibility)
```mermaid
stateDiagram-v2
    [*] --> INELIGIBLE : Post created as DRAFT
    INELIGIBLE --> ELIGIBLE_GLOBAL : status=ACTIVE, visibility=PUBLIC
    INELIGIBLE --> ELIGIBLE_FOLLOWING : status=ACTIVE, visibility=FOLLOWERS and relation accepted
    ELIGIBLE_GLOBAL --> INELIGIBLE : status=DELETED or moderation hide
    ELIGIBLE_FOLLOWING --> INELIGIBLE : unfollow/private rule/status change
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    actor User
    participant SocialAPI
    participant Mongo as MongoDB (POSTS)
    participant Pg as PostgreSQL (FOLLOWS)

    alt Global Feed
        User->>SocialAPI: GET /feed/global
        SocialAPI->>Mongo: Query POSTS(status=ACTIVE, visibility=PUBLIC)
        SocialAPI-->>User: Return paginated posts
    else Following Feed
        User->>SocialAPI: GET /feed/following
        SocialAPI->>Pg: Query followee_ids(status=ACCEPTED)
        SocialAPI->>Mongo: Query POSTS(author_id in followee_ids, status=ACTIVE)
        SocialAPI->>SocialAPI: Apply visibility (PUBLIC/FOLLOWERS)
        SocialAPI-->>User: Return paginated posts
    end
```

## 4. Entity Impact
- `POSTS`: dữ liệu nguồn của feed; đọc theo index `status/visibility/created_at`, `author_id/status/created_at`.
- `FOLLOWS`: xác định social graph cho Following Feed.
- Không có thay đổi trạng thái dữ liệu bắt buộc trong flow đọc feed.

## 5. Event Publishing
- Không có event publish bắt buộc trong luồng đọc feed.
- Nếu bật analytics nội bộ, có thể phát event read-only (không thuộc MVP bắt buộc).
