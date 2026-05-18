# Post Lifecycle Flow

## 1. Overview
Luồng này mô tả vòng đời bài viết và bình luận trong Social Service: tạo, chỉnh sửa, xóa mềm post; tạo comment/reply; xóa comment. Luồng bám theo trạng thái `DRAFT/ACTIVE/DELETED` của post và `ACTIVE/DELETED` của comment.

## 2. State Machine (Post & Comment Status)
```mermaid
stateDiagram-v2
    [*] --> DRAFT : Create draft post
    DRAFT --> ACTIVE : Publish post
    ACTIVE --> ACTIVE : Edit post
    DRAFT --> DELETED : Soft delete
    ACTIVE --> DELETED : Soft delete

    state "Comment" as CommentState {
      [*] --> ACTIVE : Create comment/reply
      ACTIVE --> DELETED : Delete comment
    }
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    actor User
    participant SocialAPI
    participant Mongo as MongoDB (POSTS/COMMENTS)
    participant Pg as PostgreSQL (OUTBOX_EVENTS)
    participant Worker as Outbox Worker
    participant Broker as Message Broker

    %% Create or edit post
    User->>SocialAPI: Create/Edit Post (caption, media, tags, visibility)
    SocialAPI->>Mongo: Insert/Update POSTS
    SocialAPI-->>User: 200/201

    %% Soft delete post
    User->>SocialAPI: Delete Post
    SocialAPI->>Mongo: Update status=DELETED, deleted_at
    SocialAPI-->>User: 200

    %% Comment / Reply
    User->>SocialAPI: Create Comment/Reply
    SocialAPI->>Mongo: Insert COMMENTS
    SocialAPI->>Mongo: Update POSTS.reply_count
    SocialAPI->>Pg: Insert OUTBOX_EVENTS(COMMENT_CREATED, PENDING)
    SocialAPI-->>User: 201

    Worker->>Pg: Poll PENDING events
    Worker->>Broker: Publish COMMENT_CREATED
    Worker->>Pg: Update status=PUBLISHED
```

## 4. Entity Impact
- `POSTS`: tạo/sửa/xóa mềm bài viết, cập nhật `status`, `updated_at`, `deleted_at`.
- `COMMENTS`: tạo comment/reply, xóa mềm comment.
- `OUTBOX_EVENTS`: ghi event `COMMENT_CREATED` (và event mở rộng như `POST_CREATED`, `POST_UPDATED`, `POST_DELETED` nếu bật).

## 5. Event Publishing
- `COMMENT_CREATED`: phát sau khi tạo comment/reply thành công.
- Có thể mở rộng: `POST_CREATED`, `POST_UPDATED`, `POST_DELETED`, `COMMENT_DELETED`.
