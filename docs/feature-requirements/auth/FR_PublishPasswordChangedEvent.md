# Functional Requirement (FR) - Publish password changed event

## 1. Feature Overview
Chuc nang cho phep Auth Service phat su kien `PASSWORD_CHANGED` qua transactional outbox khi user doi mat khau thanh cong, de dong bo xuong Notification Service va cac service lien quan theo kien truc Event-Driven.

Muc tieu:
- Phat tin hieu bao mat sau khi mat khau thay doi (vi du gui email canh bao).
- Dam bao tinh nhat quan bang transactional outbox, tranh dual-write.

## 2. Actors
- **System (Auth Service):** Thanh phan doi mat khau va ghi outbox event.
- **Outbox Worker / Message Broker:** Thanh phan doc outbox va publish bat dong bo.

## 3. Scope
- **In Scope:**
  - Tao outbox event `PASSWORD_CHANGED` khi doi mat khau thanh cong.
  - Ghi event vao `OUTBOX_EVENTS` voi `status = PENDING`.
  - Bao dam update password + revoke sessions + outbox insert trong cung transaction.
  - Payload toi thieu gom `user_id`, `email`, moc thoi gian doi mat khau.
- **Out of Scope:**
  - Logic consume event ben Notification Service.
  - Co che publish worker/retry event chi tiet (thuoc FR rieng).
  - API FE truc tiep cho outbox.

## 4. Preconditions
- User da dang nhap hop le.
- Payload doi mat khau hop le.
- Mat khau cu xac thuc dung.
- Database co bang `OUTBOX_EVENTS`.

## 5. Business Rules
- Doi mat khau thanh cong thi bat buoc tao event `PASSWORD_CHANGED`.
- Trong cung transaction phai co:
  - update `USERS.password_hash`,
  - update `USERS.password_changed_at`,
  - revoke all refresh sessions cua user,
  - insert outbox `PASSWORD_CHANGED`.
- Event moi tao phai co:
  - `event_type = PASSWORD_CHANGED`
  - `source = auth-service`
  - `status = PENDING`
  - `retry_count = 0`
- Payload khong chua du lieu nhay cam (mat khau moi, password hash, token raw, ...).
- Neu serialize payload that bai -> rollback transaction va tra loi he thong.

## 6. API Contract (Target)
Day la integration behavior noi bo, khong phai endpoint rieng.

**Endpoint trigger chinh:**
- `POST /api/v1/auth/change-password`

**Response cho client:** Theo FR endpoint goc change-password; khong tra outbox event truc tiep.

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `user_id` (payload) | UUID string | Yes | Dung dinh dang UUID | Internal validation |
| `email` (payload) | string | Yes | Email normalized hop le | Internal validation |
| `changed_at` (payload) | ISO-8601 string | Yes | Moc thoi gian doi mat khau hop le | Internal validation |
| `event_type` | string | Yes | Phai la `PASSWORD_CHANGED` | Internal validation |

## 8. Workflow
1. User goi endpoint change-password va cung cap mat khau cu/moi.
2. Auth Service validate auth context + payload + xac thuc mat khau cu.
3. Trong cung transaction:
   - cap nhat mat khau moi + `password_changed_at`,
   - revoke all refresh sessions cua user,
   - tao outbox event `PASSWORD_CHANGED` voi `status = PENDING`.
4. Commit transaction.
5. Worker (async) doc outbox `PENDING` va publish len broker (ngoai pham vi FR nay).

## 9. Database Impact
- Write:
  - `USERS`: cap nhat `password_hash`, `password_changed_at`, `updated_at`.
  - `REFRESH_TOKEN_SESSION`: revoke all ACTIVE sessions cua user.
  - `OUTBOX_EVENTS`:
    - `event_type = PASSWORD_CHANGED`
    - `source = auth-service`
    - `payload = json`
    - `status = PENDING`
    - `retry_count = 0`
    - `created_at = now()`
- Khong set `published_at` tai buoc create event.

## 10. Error Handling
- `400`: payload sai, current password sai, confirm password khong khop.
- `401`: unauthorized.
- `500`: loi he thong khi serialize payload outbox hoac ghi DB.
- Neu transaction loi -> rollback toan bo, khong de outbox event mo coi.

## 11. Security
- Tuyet doi khong dua mat khau/password hash vao payload event.
- Bat buoc hash mat khau moi truoc khi luu DB.
- Bat buoc HTTPS/TLS cho endpoint client-facing.
- Sau doi mat khau phai revoke session de giam rui ro session chiem doat.

## 12. FE Behavior
- FE khong tuong tac truc tiep voi outbox.
- FE chi goi endpoint change-password:
  - neu thanh cong -> thong bao thanh cong va xu ly logout/dang nhap lai theo policy.
  - neu that bai -> hien thong bao loi theo API response.

## 13. Acceptance Criteria
- **AC1:** Doi mat khau thanh cong -> co outbox event `PASSWORD_CHANGED`.
- **AC2:** Event moi tao co `status = PENDING`, `retry_count = 0`.
- **AC3:** Payload event co toi thieu `user_id`, `email`, `changed_at`.
- **AC4:** Update password + revoke sessions + outbox insert nam trong cung transaction.
- **AC5:** Neu tao outbox event that bai -> rollback thay doi password.
- **AC6:** Payload event khong chua du lieu nhay cam.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang outbox:**
  - `OutboxEvent`, `OutboxStatus`, `OutboxEventRepository`, `OutboxEventRepositoryAdapter`.
  - Schema `OUTBOX_EVENTS` ho tro lifecycle `PENDING/PROCESSING/PUBLISHED/FAILED`.
- **`PASSWORD_CHANGED` da duoc implement:**
  - `ChangePasswordUseCase` da tao payload gom `user_id`, `email`, `changed_at`.
  - Da save outbox event voi `event_type = PASSWORD_CHANGED`, `status = PENDING`.
  - Use case `@Transactional`, bao dam doi mat khau + revoke sessions + outbox insert trong cung transaction.
- **Ket luan hien trang:** Chuc nang publish `PASSWORD_CHANGED` theo huong transactional outbox da duoc implement va phu hop voi logic change-password hien tai.

## 15. Mapping to Existing Project Docs
- `docs/feature-requirements/auth/FR_ChangePassword.md`
- `docs/business-spec/auth-service-spec.md` (Event/Integration - Publish password changed event)
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md` (`OUTBOX_EVENTS`, `USERS`, `REFRESH_TOKEN_SESSION`)
