# Event Publishing & Outbox Flow

## 1. Overview
Flow quan trọng nhất để đảm bảo tính toàn vẹn dữ liệu trong hệ thống Microservices phân tán sử dụng Kafka. Xử lý triệt để lỗi "Dual-Write" (Vừa ghi DB vừa đẩy Kafka).

## 2. Transactional Outbox State Machine
```mermaid
stateDiagram-v2
    [*] --> PENDING : Local DB Transaction
    PENDING --> PROCESSING : Worker Polling
    PROCESSING --> PUBLISHED : Kafka Ack Success
    PROCESSING --> FAILED : Kafka Ack Error (Retry Limit Reached)
    FAILED --> PENDING : Cron Job Retry
```

## 3. Business Flow Diagram
```mermaid
sequenceDiagram
    participant AppLogic as Auth Service Logic
    participant DB as PostgreSQL (Local)
    participant Worker as Outbox Worker (Cron/CDC)
    participant Kafka as Kafka Broker

    %% Business Transaction
    AppLogic->>DB: BEGIN Transaction
    AppLogic->>DB: INSERT USERS (Business Data)
    AppLogic->>DB: INSERT OUTBOX_EVENTS (payload, status=PENDING)
    DB-->>AppLogic: COMMIT Transaction
    
    %% Background Publishing
    loop Every few seconds / Debezium CDC
        Worker->>DB: SELECT * FROM outbox_events WHERE status='PENDING'
        Worker->>DB: UPDATE status='PROCESSING'
        Worker->>Kafka: Publish Message
        
        alt Publish Success
            Kafka-->>Worker: ACK
            Worker->>DB: UPDATE status='PUBLISHED', published_at=now()
        else Publish Error / Timeout
            Kafka-->>Worker: Error
            Worker->>DB: UPDATE status='PENDING', retry_count += 1
        end
    end
```

## 4. Tác động của Flow
- Đảm bảo **At-least-once delivery** (Event chắc chắn sẽ được gửi đi, dù có thể gửi trùng). Các Consumer (Social, Notification) phải code logic theo hướng **Idempotent** (Xử lý trùng event không gây lỗi).
- Bảng `OUTBOX_EVENTS` đóng vai trò như một bộ đệm tin cậy. Nếu Kafka chết, dữ liệu vẫn an toàn nằm trong DB. Khi Kafka sống lại, Worker sẽ tiếp tục bơm dữ liệu đi.