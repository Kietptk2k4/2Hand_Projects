# Functional Requirement (FR) - Handle Post Moderated Event

## 1. Feature Overview

Social Service **ap dung** quyet dinh moderation len post khi Admin Service publish `POST_MODERATED` (topic `admin.post.moderated`). Admin **khong** mutate MongoDB Social; Social own trang thai post va visibility tren feed/search/profile.

## 2. Actors

- **Admin Service:** Moderate post (`FR_ModeratePost`), ghi log + outbox.
- **Social consumer:** Apply `HIDE` hoac `REMOVE` len document `POSTS`.

## 3. Scope

- **In Scope:**
  - Consume `POST_MODERATED`.
  - Map `action` → cap nhat post (hide khoi discovery / soft delete).
  - Idempotent theo `moderation_log_id` hoac `event_id`.
  - Optional publish `POST_DELETED` / `POST_HIDDEN` qua Social outbox cho Notification/Search.
- **Out of Scope:**
  - Admin UI moderation (`docs/feature_requirements/admin/FR_ModeratePost.md`).
  - Moderate comment (`COMMENT_MODERATED` — FR rieng neu can).
  - Restore post (`FR_RestorePost` admin — co the la event `POST_RESTORED` sau).

## 4. Preconditions

- Post ton tai trong `POSTS` (ObjectId `post_id` trong payload).
- Event payload hop le tu Admin outbox.

## 5. Event Contract

**Topic:** `admin.post.moderated`

**Event type:** `POST_MODERATED`

**Payload (theo Admin integration):**

```json
{
  "event_id": "uuid",
  "event_type": "POST_MODERATED",
  "post_id": "507f1f77bcf86cd799439011",
  "moderation_log_id": "uuid",
  "action": "HIDE",
  "reason": "Noi dung vi pham chinh sach",
  "moderated_by": "uuid-admin",
  "moderated_at": "2026-05-23T10:00:00Z"
}
```

`note` nội bộ admin **không** có trong payload broker.

## 6. Business Rules

### 6.1 Action mapping (MVP)

| Admin `action` | Social effect | User experience |
|----------------|---------------|-----------------|
| `HIDE` | Post van ton tai DB nhung **khong** xuat hien global/following/search/profile nguoi khac; author co the thay banner "Bi an boi moderation" (optional API field) | An khoi discovery |
| `REMOVE` | `status = DELETED`, set `deleted_at`, `updated_at` | Nhu soft delete; 404 voi viewer thuong |

**Schema extension khuyen nghi (neu can tach HIDE vs DELETE user):**

- Them field `moderation_status`: `NONE` | `HIDDEN` | `REMOVED`.
- `REMOVED` dong bo voi `status = DELETED`.
- `HIDDEN` giu `status = ACTIVE` nhung filter query feed/search.

Neu chua co field: MVP co the map ca `HIDE` va `REMOVE` → `DELETED` (don gian) — **uu tien product:** implement tach HIDE de author van thay post.

### 6.2 Idempotency

- Cung `moderation_log_id` + `action` → no-op.
- Post da `DELETED` va nhan `REMOVE` → skip.
- Post khong ton tai → log warning + ack (khong retry vo han).

### 6.3 Side effects

- Cap nhat `updated_at`.
- Optional: ghi `moderation_reason` (chi author/admin tools, khong public API).
- Khong xoa file MinIO trong MVP (async cleanup sau).

## 7. Alternative: Synchronous internal API (optional)

Neu Admin can verify truoc khi ghi log (khi `admin.integrations.social.enabled=true`):

**Endpoint (internal):** `POST /api/v1/social/internal/posts/{postId}/moderate`

**Auth:** Service token / mTLS (khong JWT user)

**Body:** giong Admin `action`, `reason`, `moderation_log_id`

Social van **uu tien** event-driven lam source of truth cuoi cung de tranh race.

## 8. Database Impact

- **Update MongoDB `posts`:** status / moderation fields / `deleted_at`.
- Optional **PostgreSQL `processed_domain_events`** cho idempotency.
- Optional **OUTBOX_EVENTS:** `POST_HIDDEN` / `POST_DELETED` cho Notification.

## 9. Transaction

- Single-document Mongo update atomic.
- Outbox ghi cung luong neu can notify.

## 10. Security

- Chi consumer trusted hoac internal API tu Admin network.
- Khong cho user tu goi API moderation.

## 11. Failure Cases

- Invalid `post_id` format → DLQ.
- Mongo error → retry; khong ack.
- Unknown `action` → DLQ + alert.

## 12. Acceptance Criteria

- **AC1:** Event `REMOVE` dat post `DELETED`; `FR_ViewPostDetail` tra 404 voi viewer thuong.
- **AC2:** Event `HIDE` loai post khoi `FR_ViewGlobalFeed` nhung author van doc duoc (neu policy bat).
- **AC3:** Event trung khong double-update counters.
- **AC4:** Idempotent theo `moderation_log_id`.

## 13. Related

| FR / Tai lieu | Muc dich |
|---------------|----------|
| `admin/FR_ModeratePost` | Nguon event |
| `docs/api_fe_behavior/admin_api_fe_behavior/ModeratePost-api-and-behavior.md` | Payload & FE admin |
| `FR_DeletePost` | User tu xoa (khac moderation) |
| `docs/business_flow/admin_business_flow/social-content-moderation-flow.md` | Sequence |

## 14. Implementation Notes (hien trang)

- `DeletePostUseCase` cho phep role MODERATOR/ADMIN — co the la HTTP path tam thoi; **consumer event chua co**.
- `PostStatus` hien chi `DRAFT`, `ACTIVE`, `DELETED` — can mo rong neu tach HIDE.
