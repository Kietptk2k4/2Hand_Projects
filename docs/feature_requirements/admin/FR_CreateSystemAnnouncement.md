# Functional Requirement - Create System Announcement

## 1. Feature Overview

Cho phep admin tao announcement toan he thong o trang thai `DRAFT`.

## 2. Actors

- **Admin/Super Admin:** Tao announcement.
- **Admin Service:** Store draft announcement.

## 3. Scope

**In Scope:**

- Create announcement draft.
- Set severity, pinned and dismissible flags.

**Out of Scope:**

- Publish/fan-out announcement.
- Per-user dismissal tracking.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/system-announcements`

**Auth:** Required, permission `SYSTEM_ANNOUNCEMENT_CREATE`.

**Request body:**

- `title`
- `content`
- `severity`: `INFO`, `WARNING`, `CRITICAL`
- `is_pinned`
- `dismissible`

## 5. Business Rules

- Title and content required.
- Initial status is `DRAFT`.
- `created_by` comes from JWT.

## 6. Database Impact

- Insert `system_announcements`.
- Optional insert `admin_action_logs`.

## 7. Transaction

- Write transaction required.

## 8. Security

- Permission required.

## 9. Failure Cases

- Invalid payload -> 400.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Draft announcement is created.
- Announcement is not sent until publish action.
- Creator admin id is recorded.

