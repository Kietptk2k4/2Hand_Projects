# Functional Requirement - Pin System Announcement

## 1. Feature Overview

Cho phep admin pin hoac unpin announcement de hien thi noi bat tren client/admin portal.

## 2. Actors

- **Admin/Super Admin:** Pin/unpin announcement.
- **System:** Persist pinned flag.

## 3. Scope

**In Scope:**

- Update `is_pinned`.
- Audit log if configured.

**Out of Scope:**

- Publish announcement.
- Client layout implementation.

## 4. API Contract

**Endpoint:** `PATCH /admin/api/v1/system-announcements/{announcementId}/pin`

**Auth:** Required, permission `SYSTEM_ANNOUNCEMENT_UPDATE`.

**Request body:**

- `is_pinned`

## 5. Business Rules

- Announcement must exist.
- Pinning cancelled announcement can be rejected by policy.

## 6. Database Impact

- Update `system_announcements.is_pinned`.
- Optional insert `admin_action_logs`.

## 7. Transaction

- Required.

## 8. Security

- Permission required.

## 9. Failure Cases

- Announcement not found -> 404.
- Invalid status -> 409.

## 10. Acceptance Criteria

- Admin can pin/unpin announcement.
- Pinned flag is persisted.

