# Functional Requirement (FR) - Retry Failed Outbox Events (Social)

## 1. Feature Overview
Cho phep Social Service tu dong retry cac outbox event bi loi de dam bao khong mat thong diep trong mo hinh event-driven.

## 2. Actors
- **System (Scheduler/Worker):** Thanh phan quet va retry outbox events.
- **Message Broker:** Noi nhan event publish.

## 3. Scope
- **In Scope:**
  - Quet event `FAILED` va `PENDING` bi timeout.
  - Retry publish theo chu ky.
  - Cap nhat `retry_count`, `last_error`, `status`.
- **Out of Scope:**
  - Dashboard van hanh chi tiet.

## 4. API Contract
Internal behavior, khong bat buoc endpoint FE.

## 5. Business Rules
- State machine outbox: `PENDING -> PROCESSING -> PUBLISHED/FAILED`.
- Neu vuot nguong retry -> giu `FAILED`, canh bao van hanh.
- Co che retry phai tuan thu at-least-once delivery.

## 6. Database Impact
- `OUTBOX_EVENTS`: read/update theo status va retry policy.

## 7. Transaction
- Moi lan retry cap nhat state can co boundary transaction ro rang.

## 8. Security
- Worker noi bo, khong expose payload event nhay cam.

## 9. Acceptance Criteria
- Event loi duoc retry theo scheduler.
- Event publish thanh cong chuyen `PUBLISHED`.
- Event vuot nguong retry giu `FAILED`.
