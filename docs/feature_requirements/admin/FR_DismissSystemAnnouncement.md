# Functional Requirement - Dismiss System Announcement

## 1. Feature Overview

Cho phep user/client dismiss announcement neu `dismissible = true`. MVP schema Admin chua co per-user dismissal table, nen feature nay co the duoc xu ly client-side hoac bo sung schema sau.

## 2. Actors

- **User/Admin Client:** Dismiss announcement.
- **System:** Validate dismissible policy if server-side.

## 3. Scope

**In Scope:**

- Define dismissal behavior.
- Respect `dismissible` flag.

**Out of Scope:**

- Server-side per-user dismissal persistence in current MVP schema.

## 4. API Contract

**Endpoint:** Optional future `POST /admin/api/v1/system-announcements/{announcementId}/dismiss`

**Auth:** Depends on target client.

## 5. Business Rules

- Only announcements with `dismissible = true` can be dismissed.
- Non-dismissible critical announcements remain visible until cancelled or unpinned by admin policy.
- If server-side dismissal is required, add a user-announcement dismissal table later.

## 6. Database Impact

- Current MVP: none.
- Future: insert dismissal record.

## 7. Transaction

- None in current MVP.

## 8. Security

- If server-side, user can dismiss only for self.

## 9. Failure Cases

- Announcement not dismissible -> 409.
- Announcement not found -> 404.

## 10. Acceptance Criteria

- Dismissible flag is respected.
- Current MVP does not require schema changes for client-side dismiss.

