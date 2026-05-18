# Functional Requirement (FR) - Publish user updated event

## 1. Feature Overview
Chuc nang cho phep Auth Service phat su kien `USER_UPDATED` qua transactional outbox khi thong tin user/public profile thay doi, de dong bo xuong cac service khac theo mo hinh Event-Driven.

Muc tieu:
- Dam bao thay doi profile/avatar/privacy duoc dong bo eventually-consistent giua cac service.
- Tranh dual-write bang cach ghi business data va outbox event trong cung transaction.

## 2. Actors
- **System (Auth Service):** Thanh phan cap nhat user/profile va ghi outbox event.
- **Outbox Worker / Message Broker:** Thanh phan doc outbox va publish bat dong bo.

## 3. Scope
- **In Scope:**
  - Tao outbox event `USER_UPDATED` cho cac luong cap nhat user account.
  - Ghi event vao `OUTBOX_EVENTS` voi `status = PENDING`.
  - Bao dam update business data + outbox event trong cung transaction.
  - Payload toi thieu gom `user_id`, `email` va moc thoi gian update.
- **Out of Scope:**
  - Logic consume event ben Social/Notification/Commerce.
  - Co che worker publish/retry event chi tiet (thuoc FR rieng).
  - API FE truc tiep cho outbox.

## 4. Preconditions
- User da xac thuc va duoc phep thuc hien thao tac cap nhat.
- Du lieu cap nhat hop le theo tung use case (profile/avatar/privacy).
- Database co bang `OUTBOX_EVENTS`.

## 5. Business Rules
- Sau khi update business data thanh cong, he thong phai tao event `USER_UPDATED`.
- Ghi business data va outbox event trong cung transaction (all-or-nothing).
- Event moi tao phai co:
  - `event_type = USER_UPDATED`
  - `source = auth-service`
  - `status = PENDING`
  - `retry_count = 0`
- Payload khong chua du lieu nhay cam (password hash, token raw, ...).
- Neu serialize payload that bai -> rollback transaction va tra loi he thong.

## 6. API Contract (Target)
Day la integration behavior noi bo, khong phai endpoint rieng.

**Cac endpoint trigger `USER_UPDATED` (hien tai):**
- `PUT /api/v1/users/me/profile`
- `PATCH /api/v1/users/me/avatar`
- `PATCH /api/v1/users/me/privacy`

**Response cho client:** Theo FR cua endpoint goc; khong tra outbox event truc tiep.

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `user_id` (payload) | UUID string | Yes | Dung dinh dang UUID | Internal validation |
| `email` (payload) | string | Yes | Email normalized hop le | Internal validation |
| `updated_at` (payload) | ISO-8601 string | Yes | Moc thoi gian cap nhat hop le | Internal validation |
| `event_type` | string | Yes | Phai la `USER_UPDATED` | Internal validation |

## 8. Workflow
1. User goi endpoint cap nhat profile/avatar/privacy.
2. Auth Service validate auth context + du lieu update.
3. Trong cung transaction:
   - update business data (`USER_PROFILES`),
   - tao outbox event `USER_UPDATED` voi `status = PENDING`.
4. Commit transaction.
5. Worker (async) doc outbox `PENDING` va publish len broker (ngoai pham vi FR nay).

## 9. Database Impact
- Write:
  - `USER_PROFILES` (update theo tung nghiep vu).
  - `OUTBOX_EVENTS`:
    - `event_type = USER_UPDATED`
    - `source = auth-service`
    - `payload = json`
    - `status = PENDING`
    - `retry_count = 0`
    - `created_at = now()`
- Khong set `published_at` tai buoc create event.

## 10. Error Handling
- `500`: loi he thong khi serialize payload outbox hoac ghi DB.
- Neu transaction loi -> rollback toan bo (khong de outbox event mo coi).
- Endpoint goc map ma loi theo convention rieng cua endpoint do.

## 11. Security
- Khong dua du lieu nhay cam vao payload event.
- Chi service noi bo duoc phep thao tac outbox.
- Bat buoc HTTPS/TLS cho endpoint client-facing.

## 12. FE Behavior
- FE khong tuong tac truc tiep voi outbox.
- FE chi xu ly ket qua endpoint goc:
  - thanh cong -> cap nhat UI profile/avatar/privacy.
  - that bai -> hien thong bao loi theo API response.

## 13. Acceptance Criteria
- **AC1:** Cap nhat profile thanh cong -> co outbox event `USER_UPDATED`.
- **AC2:** Cap nhat avatar thanh cong -> co outbox event `USER_UPDATED`.
- **AC3:** Toggle privacy thanh cong -> co outbox event `USER_UPDATED`.
- **AC4:** Event moi tao co `status = PENDING`, `retry_count = 0`.
- **AC5:** Neu tao outbox event that bai -> transaction update business data bi rollback.
- **AC6:** Payload event khong chua du lieu nhay cam.

## 14. Current Project Alignment (Hien trang code)
- **Da co nen tang outbox:**
  - `OutboxEvent`, `OutboxStatus`, `OutboxEventRepository`, `OutboxEventRepositoryAdapter`.
  - Schema `OUTBOX_EVENTS` ho tro lifecycle `PENDING/PROCESSING/PUBLISHED/FAILED`.
- **`USER_UPDATED` da duoc implement:**
  - Da co `UserAccountOutboxService.userUpdated(...)` tao payload gom `user_id`, `email`, `updated_at`.
  - Da save event `USER_UPDATED` trong:
    - `UpdateProfileUseCase`
    - `UpdateAvatarUseCase`
    - `TogglePrivacyUseCase`
  - Cac use case tren deu `@Transactional`, bao dam update business data + outbox trong cung transaction.
- **Ket luan hien trang:** Chuc nang publish `USER_UPDATED` cho cac luong user-account update da duoc implement va phu hop huong transactional outbox cua do an.

## 15. Mapping to Existing Project Docs
- `docs/business-spec/auth-service-spec.md` (Event/Integration - Publish user updated event)
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md` (`OUTBOX_EVENTS`, `USER_PROFILES`)
- `docs/feature-requirements/auth/FR_UpdateProfile.md`
- `docs/feature-requirements/auth/FR_UpdateAvatar.md`
- `docs/feature-requirements/auth/FR_TogglePrivateProfile.md`
