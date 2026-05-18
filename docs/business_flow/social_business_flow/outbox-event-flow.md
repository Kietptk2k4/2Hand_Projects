# Outbox Event Flow

## 1. Overview
Đây là luồng cốt lõi đảm bảo Social Service đồng bộ dữ liệu liên service theo kiến trúc Event-Driven mà không gặp lỗi dual-write. Mọi domain event đều được ghi vào `OUTBOX_EVENTS` trong cùng transaction local trước khi worker publish lên message broker.

## 2. Transactional Outbox State Machine
```mermaid
stateDiagram-v2
    [*] --> PENDING : Business transaction commit
    PENDING --> PROCESSING : Worker polling
    PROCESSING --> PUBLISHED : Broker ACK success
    PROCESSING --> FAILED : Publish error / retry limit
    FAILED --> PENDING : Scheduled retry
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    participant App as Social Service Logic
    participant Mongo as MongoDB
    participant Pg as PostgreSQL (OUTBOX_EVENTS)
    participant Worker as Outbox Worker
    participant Broker as Message Broker

    %% Local transaction for domain change
    App->>Mongo: Write domain data (post/comment/follow relation as needed)
    App->>Pg: Insert OUTBOX_EVENTS(status=PENDING, aggregate_id, payload)
    App-->>App: Commit local transaction boundary

    %% Background publish
    loop Every few seconds / scheduler
        Worker->>Pg: Select events status=PENDING or retryable FAILED
        Worker->>Pg: Update status=PROCESSING
        Worker->>Broker: Publish event payload
        alt ACK success
            Broker-->>Worker: ACK
            Worker->>Pg: Update status=PUBLISHED, published_at=now()
        else Timeout/Error
            Broker-->>Worker: Error
            Worker->>Pg: Update status=FAILED, retry_count += 1
        end
    end
```

## 4. Entity Impact
- `OUTBOX_EVENTS`: lưu và theo dõi toàn bộ vòng đời publish event.
- Domain entities liên quan: `POSTS`, `COMMENTS`, `FOLLOWS`, `POST_LIKES` (tùy event được phát).
- Consumer side phải xử lý idempotent để chấp nhận at-least-once delivery.

## 5. Event Publishing
- **Consume from Auth:** `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`.
- **Publish from Social (MVP):** `POST_LIKED`, `COMMENT_CREATED`, `USER_FOLLOWED`.
- Retry policy dựa trên `retry_count` và trạng thái `PENDING/PROCESSING/FAILED/PUBLISHED`.
