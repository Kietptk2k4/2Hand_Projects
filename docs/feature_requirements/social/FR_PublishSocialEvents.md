# Functional Requirement (FR) - Publish Social Events

## 1. Feature Overview
Cho phep Social Service phat cac domain events qua transactional outbox de dong bo den Notification va cac service phu thuoc.

## 2. Actors
- **System (Social Service):** Ghi outbox event trong business transaction.
- **Outbox Worker / Message Broker:** Publish event bat dong bo.

## 3. Scope
- **In Scope:**
  - Ghi `OUTBOX_EVENTS` voi `status = PENDING`.
  - Publish cac event MVP: `POST_LIKED`, `COMMENT_CREATED`, `USER_FOLLOWED`.
  - Cap nhat status `PUBLISHED` khi broker ACK.
- **Out of Scope:**
  - Logic consume chi tiet o downstream services.

## 4. API Contract
Internal integration behavior, khong co endpoint FE truc tiep.

## 5. Business Rules
- Business write va outbox write phai trong cung transaction.
- Payload event phai co `aggregate_id` va metadata toi thieu.
- Consumer side can idempotent theo at-least-once delivery.

## 6. Database Impact
- `OUTBOX_EVENTS`: insert event moi + update lifecycle state.

## 7. Transaction
- Outbox write duoc transaction-bound voi domain write.

## 8. Security
- Khong dua du lieu nhay cam vao payload event.

## 9. Acceptance Criteria
- Domain action thanh cong -> co event tuong ung trong outbox.
- Publish thanh cong -> event chuyen `PUBLISHED`.
