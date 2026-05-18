# UC - Event Publishing (Social Transactional Outbox)

## 1. Overview
Use Case nay dam bao Social Service dong bo du lieu lien service theo Event-Driven Architecture ma khong mat event. Moi thay doi domain can publish ra ngoai deu di qua `OUTBOX_EVENTS` trong cung transaction voi thao tac nghiep vu, sau do worker publish len message broker va retry an toan khi loi.

## 2. Actors
* **Outbox Worker / Consumer Worker:** Background process cua Social Service.
* **Message Broker:** He thong trung chuyen event giua cac microservices.
* **System:** Thuc thi retry policy, dead-letter handling va idempotent processing.

## 3. Sub-Use Cases

### 3.1. Dong bo user data tu Auth events (Consume Flow)
* **Pre-conditions:** Auth Service da publish event hop le (`USER_CREATED`, `USER_UPDATED`, `USER_DELETED`).
* **Main Flow:**
  1. Consumer worker cua Social nhan event tu broker.
  2. He thong validate payload va event version.
  3. He thong upsert user projection local de phuc vu feed/profile rendering.
  4. He thong danh dau offset/ack sau khi xu ly thanh cong.
* **Exception Flow:**
  * Payload thieu field -> Dua vao dead-letter hoac queue loi.
  * Event trung lap -> Xu ly idempotent, khong tao ban ghi trung.
* **Post-conditions:** Du lieu user local cua Social nhat quan eventual voi Auth.

### 3.2. Publish social domain events (Standard Outbox Flow)
* **Pre-conditions:** Giao dich nghiep vu thanh cong va da ghi event vao `OUTBOX_EVENTS` trang thai `PENDING`.
* **Main Flow:**
  1. Worker quet `OUTBOX_EVENTS` lay cac event `PENDING`/`FAILED` den han retry.
  2. Chuyen trang thai event sang `PROCESSING`.
  3. Publish payload len broker (topic naming theo convention).
  4. Neu ACK thanh cong -> set `status = PUBLISHED`, cap nhat `published_at`.
  5. Neu that bai -> tang `retry_count`, dua ve `FAILED` hoac `PENDING` theo retry policy.
* **Exception Flow:** Broker timeout/serialization error -> retry theo chu ky.
* **Post-conditions:** Event duoc publish at-least-once, khong mat event.

### 3.3. Retry failed outbox events
* **Pre-conditions:** Co event o `FAILED` hoac `PROCESSING` bi tre qua timeout.
* **Main Flow:**
  1. Cron job quet event gap loi.
  2. Thu publish lai.
  3. Cap nhat `retry_count += 1`.
  4. Neu vuot nguong retry -> giu `FAILED`, ghi canh bao de can thiep van hanh.
* **Exception Flow:** Broker down dai han -> worker tiep tuc retry theo backoff policy.
* **Post-conditions:** Cac event tam thoi loi duoc khoi phuc toi da theo policy.

### 3.4. Danh sach su kien MVP cua Social publish
* **Pre-conditions:** Domain action tuong ung thanh cong.
* **Main Flow:**
  1. Like post thanh cong -> ghi event `POST_LIKED`.
  2. Tao comment/reply thanh cong -> ghi event `COMMENT_CREATED`.
  3. Follow user thanh cong -> ghi event `USER_FOLLOWED`.
* **Exception Flow:** Loi ghi outbox trong transaction -> rollback thao tac nghiep vu.
* **Post-conditions:** Notification Service va cac service phu thuoc nhan duoc event can thiet.
