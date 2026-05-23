# Functional Requirement (FR) - Consume Auth User Events

## 1. Feature Overview

Social Service dong bo **user projection local** (`user_projections` MongoDB) tu event Auth/Admin de render feed/profile nhanh, enforce trang thai user va privacy **khong** doc cross-DB Auth.

## 2. Actors

- **System (Social consumer worker):** Subscribe broker, upsert projection.
- **Auth Service:** Publish `USER_CREATED`, `USER_UPDATED`, `USER_DELETED` qua outbox.
- **Admin Service:** Publish enforcement events anh huong trang thai user (`USER_SUSPENDED`, ...).

## 3. Scope

- **In Scope:**
  - Consume va xu ly idempotent cac event user.
  - Upsert/update `user_projections`: `user_id`, `status`, `display_name`, `avatar_url`, `is_private`.
  - Dead-letter / retry policy.
- **Out of Scope:**
  - Publish event nguon (Auth/Admin own).
  - Login/session (Auth own).
  - Migrate du lieu bulk lich su (tool rieng).

## 4. Preconditions

- Kafka (hoac broker) configured; consumer group `social-user-projection`.
- Collection `user_projections` ton tai (MongoDB init script).

## 5. Event Contract

### 5.1 Topics (khuyen nghi)

| Event type | Topic (example) | Nguon |
|------------|-----------------|-------|
| `USER_CREATED` | `auth.user.created` | Auth outbox |
| `USER_UPDATED` | `auth.user.updated` | Auth outbox |
| `USER_DELETED` | `auth.user.deleted` | Auth outbox |
| `USER_SUSPENDED` | `admin.user.suspended` | Admin outbox |
| `USER_BANNED` | `admin.user.banned` | Admin (neu bat) |
| `USER_RESTRICTED` | `admin.user.restricted` | Admin (optional MVP) |
| `USER_ENFORCEMENT_REVOKED` | `admin.user.enforcement.revoked` | Admin |

### 5.2 Payload toi thieu (USER_CREATED / UPDATED)

```json
{
  "event_id": "uuid",
  "event_type": "USER_CREATED",
  "user_id": "uuid",
  "display_name": "User A",
  "avatar_url": "https://cdn.2hands.vn/avatars/...",
  "status": "ACTIVE",
  "is_private": false,
  "occurred_at": "2026-05-21T10:00:00Z"
}
```

### 5.3 Payload enforcement (USER_SUSPENDED)

```json
{
  "event_id": "uuid",
  "event_type": "USER_SUSPENDED",
  "user_id": "uuid",
  "enforcement_id": "uuid",
  "action_type": "SUSPEND",
  "occurred_at": "2026-05-21T10:00:00Z"
}
```

Social map `action_type` → `user_projections.status`:

| Event / action | projection.status |
|----------------|-------------------|
| USER_CREATED | `ACTIVE` (tu payload) |
| USER_UPDATED | cap nhat field profile/privacy |
| USER_DELETED | `DELETED` |
| USER_SUSPENDED / USER_BANNED | `SUSPENDED` |
| USER_ENFORCEMENT_REVOKED (khong con enforcement) | `ACTIVE` |

## 6. Business Rules

- **Idempotent:** luu `processed_event_ids` (Redis/PostgreSQL) hoac unique index `(event_id)`; event trung → skip.
- **Ordering:** uu tien `occurred_at`; event cu hon bi bo qua neu da co ban ghi moi hon (optional version field).
- Khong ghi password/email/phone vao projection.
- `USER_CREATED` khi user chua ton tai → insert; khi da ton tai → upsert (safe retry).
- `USER_DELETED`: khong xoa vat ly projection ngay — set `status = DELETED` de audit; co the job purge sau.
- Consumer **at-least-once**: handler phai idempotent truoc khi ack.

## 7. Database Impact

- **MongoDB `user_projections`:** insert/update.
- Optional **PostgreSQL `processed_domain_events`:** `event_id`, `consumer_name`, `processed_at`.

## 8. Transaction

- Mongo upsert trong transaction session Mongo (neu dung) hoac single-document atomic update.
- Ghi processed event cung luong de tranh double-apply.

## 9. Security

- Consumer chi listen topic trusted; khong expose endpoint public cho event inject.
- Validate schema version; reject payload thieu `user_id`.
- Khong log PII nhay cam.

## 10. Failure Cases

- Payload invalid → log + DLQ, khong ack vo han.
- Mongo down → retry voi backoff; tang `retry_count`.
- User id khong hop le UUID → skip + DLQ.

## 11. Acceptance Criteria

- **AC1:** Sau `USER_CREATED`, projection ton tai voi `display_name`/`avatar_url`.
- **AC2:** `USER_UPDATED` cap nhat `is_private` va avatar.
- **AC3:** `USER_SUSPENDED` → `status = SUSPENDED`; write APIs bi chan (`FR_EnforceUserStatusOnWrite`).
- **AC4:** `USER_DELETED` → `status = DELETED`.
- **AC5:** Event trung `event_id` khong lam sai lech du lieu.

## 12. Related

| FR / Tai lieu | Muc dich |
|---------------|----------|
| `FR_PublishUserCreatedEvent` (auth) | Publish phia Auth |
| `FR_ApplyUserEnforcement` (auth) | Apply phia Auth |
| `FR_EnforceUserStatusOnWrite` | Guard write sau khi projection cap nhat |
| `docs/business-spec/social-service-spec.md` | VII. Event / Integration |
| `docs/business_flow/admin_business_flow/cross-service-integration-flow.md` | Admin → downstream |

## 13. Implementation Notes (hien trang)

- Domain `UserProjection` + repository **da co**; consumer worker **chua co**.
- `CreatePostUseCase` da doc projection de chan `SUSPENDED`/`DELETED`.
- Auth register email co the **chua** phat `USER_CREATED` day du — Social consumer phai tolerate user thieu projection (404 profile) den khi event den.
