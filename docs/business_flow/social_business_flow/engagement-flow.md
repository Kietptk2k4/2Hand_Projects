# Engagement Flow

## 1. Overview
Luồng này mô tả các tương tác chính của user trên nội dung social: like/unlike post, save/unsave post, xem saved posts, like comment. Luồng đảm bảo idempotency qua unique constraints và cập nhật counters theo mô hình eventual consistency.

## 2. State Machine (User Engagement Relation)
```mermaid
stateDiagram-v2
    [*] --> NONE
    NONE --> LIKED_POST : Like Post
    LIKED_POST --> NONE : Unlike Post

    NONE --> SAVED_POST : Save Post
    SAVED_POST --> NONE : Unsave Post

    NONE --> LIKED_COMMENT : Like Comment
    LIKED_COMMENT --> NONE : Unlike Comment (toggle)
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    actor User
    participant SocialAPI
    participant Mongo as MongoDB (POSTS/COMMENTS)
    participant Pg as PostgreSQL (POST_LIKES, POST_SAVES, COMMENT_REACTION, OUTBOX_EVENTS)
    participant Worker as Outbox Worker
    participant Broker as Message Broker

    %% Like post
    User->>SocialAPI: Like Post
    SocialAPI->>Pg: Insert POST_LIKES(post_id,user_id)
    SocialAPI->>Mongo: Increment POSTS.like_count
    SocialAPI->>Pg: Insert OUTBOX_EVENTS(POST_LIKED, PENDING)
    SocialAPI-->>User: 200

    %% Save post
    User->>SocialAPI: Save/Unsave Post
    SocialAPI->>Pg: Insert/Delete POST_SAVES
    SocialAPI-->>User: 200

    %% Like comment
    User->>SocialAPI: Like Comment
    SocialAPI->>Pg: Toggle COMMENT_REACTION(comment_id,user_id)
    SocialAPI->>Mongo: Update COMMENTS.like_count
    SocialAPI-->>User: 200

    Worker->>Pg: Poll PENDING events
    Worker->>Broker: Publish POST_LIKED
    Worker->>Pg: Update status=PUBLISHED
```

## 4. Entity Impact
- `POST_LIKES`: mapping like post theo `(post_id, user_id)`.
- `POST_SAVES`: mapping lưu bài viết theo `(post_id, user_id)`.
- `COMMENT_REACTION`: mapping like comment theo `(comment_id, user_id)`.
- `POSTS.like_count`, `COMMENTS.like_count`: cập nhật counter hiển thị.
- `OUTBOX_EVENTS`: ghi event tương tác cần phát ra ngoài.

## 5. Event Publishing
- `POST_LIKED`: event MVP bắt buộc để Notification Service xử lý.
- Có thể mở rộng `COMMENT_LIKED` theo chính sách notification.
