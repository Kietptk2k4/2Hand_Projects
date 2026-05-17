# Functional Requirement (FR) - Retry failed outbox events

## 1. Feature Overview
Chuc nang cho phep Auth Service tu dong retry cac outbox event publish that bai hoac bi ket, nham dam bao do tin cay cua co che Event-Driven va tranh mat thong diep lien service.

Muc tieu:
- Dam bao event trong `OUTBOX_EVENTS` duoc xu ly theo huong at-least-once delivery.
- Tu dong phuc hoi sau su co tam thoi cua message broker ma khong can can thiep tay ngay lap tuc.
- Giam rui ro event bi "treo" vo thoi han trong `PENDING`/`FAILED`.

## 2. Actors
- **System (Auth Service Scheduler/Worker):** Thanh phan chay dinh ky de quet va retry outbox event.
- **Message Broker:** Dich vu nhan event (Kafka/RabbitMQ) va tra ket qua ACK/NACK.
- **Operator/Admin (gian tiep):** Theo doi canh bao khi event vuot gioi han retry.

## 3. Scope
- **In Scope:**
  - Dinh ky quet `OUTBOX_EVENTS` de tim event can retry.
  - Retry publish cho event o trang thai `FAILED` va event `PENDING` bi timeout.
  - Cap nhat state machine outbox dung theo ket qua publish.
  - Tang `retry_count`, cap nhat `last_error`, `published_at` khi can.
  - Ap dung gioi han retry toi da (`max_retries`) de tranh retry vo han.
- **Out of Scope:**
  - UI quan tri outbox chi tiet.
  - Co che dead-letter queue o he thong broker ben ngoai.
  - Logic consume event tai service downstream.

## 4. Preconditions
- Da co bang `OUTBOX_EVENTS` va enum trang thai (`PENDING`, `PROCESSING`, `PUBLISHED`, `FAILED`).
- Da co du lieu outbox duoc tao tu cac luong business (`USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, `PASSWORD_CHANGED`, ...).
- Auth Service co ket noi toi message broker.
- Scheduler/background worker duoc bat trong runtime profile phu hop.

## 5. Business Rules
- Worker chay dinh ky (vi du moi 1-5 phut) de quet event can retry.
- Tap event can xu ly gom:
  - `FAILED` va `retry_count < max_retries`.
  - `PENDING` nhung da vuot nguong timeout xu ly.
- Truoc khi publish, event duoc danh dau `PROCESSING` de han che xu ly trung.
- Neu publish thanh cong:
  - set `status = PUBLISHED`,
  - set `published_at = now`,
  - co the xoa/clear `last_error`.
- Neu publish that bai:
  - tang `retry_count += 1`,
  - cap nhat `last_error`,
  - dat lai `status = FAILED` (hoac `PENDING` tuy chinh sach retry cycle thong nhat).
- Neu dat nguong `max_retries`:
  - giu event o `FAILED`,
  - ghi log canh bao muc cao de operator theo doi va xu ly tay khi can.
- Co che retry phai idempotent o cap consumer vi mo hinh at-least-once co the gay giao event trung.

## 6. API Contract (Target)
Day la backend integration behavior noi bo, khong phai API client-facing bat buoc.

**Co che trigger chinh:**
- Scheduler/worker noi bo (cron-like) trong Auth Service.

**Tuy chon mo rong (khong bat buoc trong MVP):**
- Endpoint admin de trigger retry thu cong, chi dung cho van hanh.

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `status` | enum | Yes | Thuoc `PENDING/PROCESSING/PUBLISHED/FAILED` | Internal validation |
| `retry_count` | int | Yes | `>= 0`, tang moi lan retry that bai | Internal validation |
| `published_at` | timestamp | No | Bat buoc co khi `status = PUBLISHED` | Internal validation |
| `last_error` | text | No | Luu thong tin loi publish gan nhat | Internal validation |
| `event_type` | string | Yes | Khong rong, dung contract event da dinh nghia | Internal validation |

## 8. Workflow
1. Scheduler kich hoat theo chu ky cau hinh.
2. Worker query danh sach outbox event can retry theo tieu chi status + timeout + max retries.
3. Worker danh dau event dang xu ly (`PROCESSING`) trong pham vi transaction/nguyen tac lock phu hop.
4. Worker goi publisher gui payload sang broker.
5. Neu ACK thanh cong:
   - cap nhat `PUBLISHED`,
   - set `published_at`.
6. Neu NACK/exception:
   - tang `retry_count`,
   - set `last_error`,
   - doi status theo chinh sach (`FAILED` hoac quay lai `PENDING`).
7. Neu `retry_count` da vuot nguong:
   - dung retry tu dong cho event do,
   - giu `FAILED` va phat canh bao.

## 9. Database Impact
- Read:
  - `OUTBOX_EVENTS` theo `status`, `retry_count`, `created_at`/moc timeout.
- Write:
  - Cap nhat `status` (`PROCESSING`, `PUBLISHED`, `FAILED`),
  - Cap nhat `retry_count`,
  - Cap nhat `last_error`,
  - Cap nhat `published_at` khi publish thanh cong.
- Khuyen nghi index:
  - index theo `status`,
  - index ket hop `(status, retry_count, created_at)` neu can toi uu polling.

## 10. Error Handling
- Loi ket noi broker tam thoi: ghi `last_error`, tang `retry_count`, event se duoc retry o chu ky sau.
- Loi payload khong hop le (serialization/contract): dat `FAILED`, log chi tiet de can thiep tay.
- Loi DB khi cap nhat state: rollback buoc hien tai, khong danh dau thanh cong gia.
- Co timeout bao ve de tranh event nam `PROCESSING` vo han (co co che quay lai retry lane sau).

## 11. Security
- Worker va publisher chi chay noi bo service, khong expose payload nhay cam ra ngoai.
- Khong log du lieu nhay cam trong `last_error`.
- Neu co endpoint admin trigger retry thu cong, bat buoc RBAC permission muc cao.
- Bao dam TLS cho kenh ket noi message broker (neu ha tang ho tro).

## 12. FE Behavior
- Khong co FE flow truc tiep trong pham vi bat buoc cua FR nay.
- Neu bo sung man hinh van hanh sau nay:
  - FE chi hien thi thong ke tong quan outbox (`PENDING/FAILED/PUBLISHED`),
  - Ho tro thao tac trigger retry thu cong cho operator co quyen.

## 13. Acceptance Criteria
- **AC1:** Event `FAILED` va chua vuot `max_retries` duoc worker lay len va retry dinh ky.
- **AC2:** Event publish lai thanh cong duoc cap nhat `status = PUBLISHED` va co `published_at`.
- **AC3:** Event retry that bai duoc tang `retry_count` va cap nhat `last_error`.
- **AC4:** Event vuot nguong retry duoc giu `FAILED` va sinh canh bao van hanh.
- **AC5:** Co co che xu ly event `PENDING` bi timeout de tranh ket ban ghi.
- **AC6:** Co che retry khong lam mat event, tuan theo mo hinh at-least-once delivery.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang outbox:**
  - Domain da co `OutboxEvent`, `OutboxStatus`, `OutboxEventRepository`.
  - Persistence da co `OutboxEventRepositoryAdapter` de insert outbox event.
  - Schema DB da co bang `OUTBOX_EVENTS` voi cac cot `status`, `retry_count`, `published_at`, `last_error`.
  - Cac use case business da ghi outbox voi `status = PENDING` (register, oauth login, update profile/avatar/privacy, soft delete, change password, forgot password).
- **Chua thay phan retry worker duoc implement trong auth-service:**
  - Chua co scheduler (`@Scheduled`/equivalent) cho outbox.
  - Chua co service poll `OUTBOX_EVENTS` va publish sang broker.
  - Chua co logic chuyen state `PENDING/FAILED -> PROCESSING -> PUBLISHED/FAILED` khi retry.
  - Chua co logic cap nhat `retry_count`/`last_error` theo lan retry.
- **Ket luan hien trang:** Do an da co "write side" cua transactional outbox, nhung chua co "publish/retry side". FR nay dinh nghia phan con thieu de hoan thien luong retry failed outbox events.

## 15. Mapping to Existing Project Docs
- `docs/use-cases/uc-event-publishing.md` (muc Retry Failed Outbox Events)
- `docs/business-flow/outbox-event-flow.md` (state machine outbox)
- `docs/business-spec/auth-service-spec.md` (Event/Integration - Retry failed outbox events)
- `docs/database/auth_schema.md` (`OUTBOX_EVENTS`)
