# Functional Requirement (FR) - Publish user deleted event

## 1. Feature Overview
Chuc nang cho phep Auth Service phat su kien `USER_DELETED` qua transactional outbox khi user bi soft-delete, de dong bo xuong cac service khac theo kien truc Event-Driven.

Muc tieu:
- Bao dam cac service khac nhan duoc tin hieu vo hieu hoa/an du lieu user da xoa mem.
- Tranh dual-write bang cach ghi thay doi business data va outbox event trong cung transaction.

## 2. Actors
- **System (Auth Service):** Thanh phan xu ly soft-delete va ghi outbox event.
- **Outbox Worker / Message Broker:** Thanh phan doc outbox va publish bat dong bo.

## 3. Scope
- **In Scope:**
  - Tao outbox event `USER_DELETED` khi soft-delete thanh cong.
  - Ghi event vao `OUTBOX_EVENTS` voi `status = PENDING`.
  - Bao dam soft-delete data + revoke sessions + outbox event trong cung transaction.
  - Payload toi thieu gom `user_id`, `email`, moc thoi gian xoa.
- **Out of Scope:**
  - Hard delete vat ly du lieu user.
  - Logic consume event ben Social/Notification/Commerce.
  - Co che publish worker/retry event chi tiet (thuoc FR rieng).
  - API FE truc tiep cho outbox.

## 4. Preconditions
- User da dang nhap hop le.
- Password xac nhan xoa tai khoan hop le.
- User chua o trang thai `DELETED`.
- Database co bang `OUTBOX_EVENTS`.

## 5. Business Rules
- Khi soft-delete thanh cong, he thong phai tao event `USER_DELETED`.
- Trong cung transaction:
  - update `USERS.status = DELETED`,
  - set `deleted_at`,
  - revoke refresh sessions,
  - insert outbox event `USER_DELETED`.
- Event moi tao phai co:
  - `event_type = USER_DELETED`
  - `source = auth-service`
  - `status = PENDING`
  - `retry_count = 0`
- Payload khong chua du lieu nhay cam (password hash, token raw, ...).
- Neu serialize payload that bai -> rollback transaction va tra loi he thong.

## 6. API Contract (Target)
Day la integration behavior noi bo, khong phai endpoint rieng.

**Endpoint trigger chinh:**
- `POST /api/v1/users/me/soft-delete`

**Response cho client:** Theo FR endpoint goc soft-delete; khong tra outbox event truc tiep.

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `user_id` (payload) | UUID string | Yes | Dung dinh dang UUID | Internal validation |
| `email` (payload) | string | Yes | Email normalized hop le | Internal validation |
| `deleted_at` (payload) | ISO-8601 string | Yes | Moc thoi gian xoa hop le | Internal validation |
| `event_type` | string | Yes | Phai la `USER_DELETED` | Internal validation |

## 8. Workflow
1. User goi endpoint soft-delete va cung cap password xac nhan.
2. Auth Service validate auth context + password + trang thai user.
3. Trong cung transaction:
   - cap nhat `USERS` sang `DELETED`,
   - revoke toan bo refresh sessions cua user,
   - tao outbox event `USER_DELETED` voi `status = PENDING`.
4. Commit transaction.
5. Worker (async) doc outbox `PENDING` va publish len broker (ngoai pham vi FR nay).

## 9. Database Impact
- Write:
  - `USERS`: update `status = DELETED`, `deleted_at`, `updated_at`.
  - `REFRESH_TOKEN_SESSION`: revoke all ACTIVE sessions cua user.
  - `OUTBOX_EVENTS`:
    - `event_type = USER_DELETED`
    - `source = auth-service`
    - `payload = json`
    - `status = PENDING`
    - `retry_count = 0`
    - `created_at = now()`
- Khong set `published_at` tai buoc create event.

## 10. Error Handling
- `400`: password xac nhan sai.
- `401`: unauthorized.
- `409`: tai khoan da o trang thai `DELETED`.
- `500`: loi he thong khi serialize payload outbox hoac ghi DB.
- Neu transaction loi -> rollback toan bo, khong de outbox event mo coi.

## 11. Security
- Khong dua du lieu nhay cam vao payload event.
- Bat buoc xac nhan bang mat khau truoc soft-delete.
- Chi service noi bo duoc phep thao tac outbox.
- Bat buoc HTTPS/TLS cho endpoint client-facing.

## 12. FE Behavior
- FE khong tuong tac truc tiep voi outbox.
- FE chi goi endpoint soft-delete:
  - neu thanh cong -> clear auth state va dieu huong login/landing.
  - neu that bai -> hien thong bao loi theo API response.

## 13. Acceptance Criteria
- **AC1:** Soft-delete thanh cong -> co outbox event `USER_DELETED`.
- **AC2:** Event moi tao co `status = PENDING`, `retry_count = 0`.
- **AC3:** Payload event co toi thieu `user_id`, `email`, `deleted_at`.
- **AC4:** Soft-delete + session revoke + outbox insert nam trong cung transaction.
- **AC5:** Neu tao outbox event that bai -> rollback thay doi soft-delete.
- **AC6:** Payload event khong chua du lieu nhay cam.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang outbox:**
  - `OutboxEvent`, `OutboxStatus`, `OutboxEventRepository`, `OutboxEventRepositoryAdapter`.
  - Schema `OUTBOX_EVENTS` ho tro lifecycle `PENDING/PROCESSING/PUBLISHED/FAILED`.
- **`USER_DELETED` da duoc implement:**
  - Da co `UserAccountOutboxService.userDeleted(...)` tao payload gom `user_id`, `email`, `deleted_at`.
  - `SoftDeleteAccountUseCase` da:
    - update user sang `DELETED`,
    - revoke all sessions,
    - save outbox event `USER_DELETED`.
  - Use case duoc danh dau `@Transactional`, bao dam atomicity.
- **Ket luan hien trang:** Chuc nang publish `USER_DELETED` theo huong transactional outbox da duoc implement va dong bo voi soft-delete flow.

## 15. Mapping to Existing Project Docs
- `docs/business-spec/auth-service-spec.md` (Event/Integration - Publish user deleted event)
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md` (`OUTBOX_EVENTS`, `USERS`, `REFRESH_TOKEN_SESSION`)
- `docs/feature-requirements/auth/FR_SoftDeleteAccount.md`
