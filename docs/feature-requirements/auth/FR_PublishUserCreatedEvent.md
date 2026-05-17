# Functional Requirement (FR) - Publish user created event

## 1. Feature Overview
Chuc nang cho phep Auth Service phat su kien `USER_CREATED` qua transactional outbox khi co user moi duoc tao, de dong bo du lieu sang cac service khac theo kien truc Event-Driven.

Muc tieu:
- Bao dam service khac nhan duoc tin hieu tao moi user de xu ly nghiep vu lien quan.
- Tranh dual-write bang cach ghi DB business data va outbox event trong cung transaction.

## 2. Actors
- **System (Auth Service):** Thanh phan tao user va ghi outbox event.
- **Outbox Worker / Message Broker:** Thanh phan doc outbox va publish bat dong bo.

## 3. Scope
- **In Scope:**
  - Tao outbox event `USER_CREATED` voi payload chuan.
  - Ghi event vao `OUTBOX_EVENTS` voi `status = PENDING`.
  - Bao dam ghi user data va outbox event trong cung transaction.
  - Event payload toi thieu gom `user_id`, `email` (co the kem `status` theo implementation).
- **Out of Scope:**
  - Logic consumer ben Social/Notification/Commerce.
  - Co che publish worker/retry chi tiet (thuoc FR rieng).
  - API FE truc tiep cho outbox (khong expose cho FE).

## 4. Preconditions
- Request tao user hop le (qua flow dang ky hoac OAuth).
- Database hoat dong va co bang `OUTBOX_EVENTS`.
- Transaction app layer duoc cau hinh dung.

## 5. Business Rules
- Khi tao user moi thanh cong, he thong phai tao event `USER_CREATED` trong outbox.
- Ghi business data (user/profile/settings...) va outbox event trong cung transaction (all-or-nothing).
- Neu serialize payload that bai -> rollback transaction va tra loi he thong.
- Event moi tao mac dinh `status = PENDING`, `retry_count = 0`.
- Payload khong chua thong tin nhay cam nhu `password_hash`.

## 6. API Contract (Target)
Tinh nang nay la backend internal integration behavior, khong phai endpoint rieng.

**Cac API trigger co kha nang tao `USER_CREATED` (theo thiet ke):**
- `POST /api/v1/auth/register` (register email - sau khi tao user thanh cong).
- `GET /oauth2/authorization/{provider}` callback flow (OAuth - khi tao user moi).

**Response API cho client:** Theo FR cua tung endpoint goc (register/oauth), khong tra outbox event truc tiep.

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `user_id` (payload) | UUID string | Yes | Dung dinh dang UUID | Internal validation |
| `email` (payload) | string | Yes | Email normalized hop le | Internal validation |
| `event_type` | string | Yes | Phai la `USER_CREATED` | Internal validation |
| `status` | enum | Yes | Mac dinh `PENDING` khi moi tao | Internal validation |

## 8. Workflow
1. Use case tao user moi duoc goi.
2. Auth Service validate request + tao du lieu user.
3. Trong cung transaction:
   - ghi business data (`USERS`, `USER_PROFILES`, `USER_SETTINGS`, ...),
   - tao ban ghi `OUTBOX_EVENTS` voi `event_type = USER_CREATED`, `status = PENDING`.
4. Commit transaction.
5. Worker (async) doc outbox `PENDING` va publish len broker (ngoai pham vi FR nay).

## 9. Database Impact
- Write:
  - `USERS`, `USER_PROFILES`, `USER_SETTINGS` (tuy flow).
  - `OUTBOX_EVENTS`:
    - `event_type = USER_CREATED`
    - `source = auth-service`
    - `payload = json`
    - `status = PENDING`
    - `retry_count = 0`
    - `created_at = now()`
- Khong cap nhat `published_at` tai buoc create event.

## 10. Error Handling
- `500`: loi he thong khi serialize payload outbox hoac ghi DB.
- Neu transaction loi -> khong de lai outbox event mo coi (rollback toan bo).
- Endpoint goc (register/oauth) tu mapping ma loi theo convention rieng.

## 11. Security
- Khong dua du lieu nhay cam vao payload (`password_hash`, token raw, ...).
- Chi service noi bo duoc phep thao tac outbox.
- Bat buoc HTTPS/TLS cho endpoint goc client-facing.

## 12. FE Behavior
- FE khong goi hay hien thi truc tiep du lieu outbox.
- FE chi xu ly ket qua endpoint goc (register/oauth):
  - thanh cong -> dieu huong dung flow.
  - that bai -> hien thong bao loi theo API response.

## 13. Acceptance Criteria
- **AC1:** Khi tao user moi thanh cong, he thong co ban ghi `OUTBOX_EVENTS` voi `event_type = USER_CREATED`.
- **AC2:** Ban ghi outbox moi tao co `status = PENDING`, `retry_count = 0`.
- **AC3:** Payload event co toi thieu `user_id` va `email`.
- **AC4:** Neu ghi outbox that bai, transaction tao user bi rollback.
- **AC5:** Khong lo thong tin nhay cam trong payload `USER_CREATED`.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang outbox:**
  - Da co `OutboxEvent`, `OutboxStatus`, `OutboxEventRepository`, `OutboxEventRepositoryAdapter`.
  - Schema `OUTBOX_EVENTS` da ho tro trang thai `PENDING/PROCESSING/PUBLISHED/FAILED`.
- **Trang thai `USER_CREATED` hien tai:**
  - **Da duoc implement cho flow OAuth tao user moi** trong `OAuthLoginUseCase`:
    - tao payload (`user_id`, `email`, `status`),
    - save outbox `event_type = USER_CREATED`, `status = PENDING`.
  - **Flow register email hien tai chua phat `USER_CREATED`**; dang phat `EMAIL_VERIFICATION_REQUESTED` trong `RegisterUserUseCase`.
- **Ket luan hien trang:** Chuc nang publish `USER_CREATED` da co mot phan (OAuth new user). Neu muon dong bo day du theo spec "tao user moi bat ky", can bo sung `USER_CREATED` cho flow register email (thoi diem phat event can theo policy nghiep vu: sau register hay sau verify).

## 15. Mapping to Existing Project Docs
- `docs/business-spec/auth-service-spec.md` (Event/Integration - Publish user created event)
- `docs/use-cases/uc-event-publishing.md` (Publish Event standard flow)
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md` (`OUTBOX_EVENTS`)
- `docs/feature-requirements/auth/FR_Register_Email.md`
- `docs/feature-requirements/auth/FR_Login_OAuth.md`
- `docs/engineering-rules/api-standard.md`
