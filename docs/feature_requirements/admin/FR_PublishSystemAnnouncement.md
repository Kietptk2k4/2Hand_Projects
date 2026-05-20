# Functional Requirement - Publish System Announcement

## 1. Feature Overview

Cho phep admin publish announcement draft, set `sent_at` va publish event de Notification Service fan-out neu can.

## 2. Actors

- **Admin/Super Admin:** Publish announcement.
- **Notification Service:** Consume event.

## 3. Scope

**In Scope:**

- Change status `DRAFT -> SENT`.
- Set `sent_at`.
- Log admin action.
- Publish `SYSTEM_ANNOUNCEMENT_PUBLISHED`.

**Out of Scope:**

- Notification delivery implementation.
- Per-user dismissal.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/system-announcements/{announcementId}/publish`

**Auth:** Required, permission `SYSTEM_ANNOUNCEMENT_PUBLISH`.

## 5. Business Rules

- Only `DRAFT` announcement can be published.
- Publish sets `sent_at`.
- Critical announcements should be audit logged.
- Event goes through outbox.

## 6. Database Impact

- Update `system_announcements`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required.

## 8. Security

- Permission required.

## 9. Failure Cases

- Announcement not found -> 404.
- Not draft -> 409.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Draft announcement becomes sent.
- `sent_at` is set.
- Outbox event is created.

