# Discovery Flow

## 1. Overview
Luồng này mô tả discovery MVP của Social Service gồm search post theo từ khóa, search hashtag, và lưu search history. Kết quả tìm kiếm phải tuân thủ trạng thái nội dung và visibility rules.

## 2. State Machine (Search Lifecycle)
```mermaid
stateDiagram-v2
    [*] --> QUERY_RECEIVED
    QUERY_RECEIVED --> VALIDATED : Query valid
    QUERY_RECEIVED --> REJECTED : Query invalid
    VALIDATED --> RESULT_GENERATED : Search success
    RESULT_GENERATED --> HISTORY_UPDATED : Save/refresh search history
    RESULT_GENERATED --> HISTORY_SKIPPED : History write failed (best-effort)
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    actor User
    participant SocialAPI
    participant Mongo as MongoDB (POSTS)
    participant Pg as PostgreSQL (SEARCH_HISTORY)

    User->>SocialAPI: Search by keyword/hashtag
    SocialAPI->>SocialAPI: Validate query params
    SocialAPI->>Mongo: Query POSTS(status=ACTIVE, visibility rule)
    SocialAPI-->>User: Return paginated results

    opt Save search history (best-effort)
        SocialAPI->>Pg: Insert/Update SEARCH_HISTORY(user_id, keyword)
    end
```

## 4. Entity Impact
- `POSTS`: dữ liệu nguồn cho search theo caption/hashtags.
- `SEARCH_HISTORY`: lưu/refresh từ khóa người dùng đã tìm kiếm.
- Không thay đổi business state của post/comment trong flow discovery.

## 5. Event Publishing
- Không có event publish bắt buộc trong flow discovery MVP.
- Lưu search history theo cơ chế best-effort, không làm fail response chính.
