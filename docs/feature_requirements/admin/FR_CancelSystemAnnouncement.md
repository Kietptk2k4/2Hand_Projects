# Functional Requirement - Cancel System Announcement

## 1. Feature Overview

Cho phep admin cancel announcement draft hoac sent announcement theo policy.

## 2. Actors

- **Admin/Super Admin:** Cancel announcement.
- **System:** Update status and optionally notify consumers.

## 3. Scope

**In Scope:**

- Change status to `CANCELLED`.
- Log admin action.
- Publish cancellation event if needed.

**Out of Scope:**

- Per-user notification deletion.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/system-announcements/{announcementId}/cancel`

**Auth:** Required, permission `SYSTEM_ANNOUNCEMENT_CANCEL`.

## 5. Business Rules

- `DRAFT` or `SENT` can be cancelled.
- Already `CANCELLED` is idempotent or 409 by policy.
- Cancelled announcement is not active.

## 6. Database Impact

- Update `system_announcements.status`.
- Insert `admin_action_logs`.
- Optional insert `outbox_events`.

## 7. Transaction

- Required.

## 8. Security

- Permission required.

## 9. Failure Cases

- Announcement not found -> 404.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Announcement becomes cancelled.
- Cancel action is audit logged.
- Cancelled announcement is not shown as active.

