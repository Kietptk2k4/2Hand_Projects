# Functional Requirement - View Unread Notifications

## 1. Feature Overview

Allow authenticated user to view unread, non-deleted notifications.

## 2. Actors

- **User:** Views unread notifications.
- **Notification API:** Queries unread records.

## 3. Scope

**In Scope:**

- List unread notifications.
- Pagination.
- Exclude deleted records.

**Out of Scope:**

- Marking as read.
- Real-time badge updates.

## 4. API Contract

**Endpoint:** `GET /notification/api/v1/notifications/unread`

**Auth:** Required user JWT.

## 5. Business Rules

- Filter `user_id = current_user_id`.
- Filter `is_read = false`.
- Filter `is_deleted = false`.
- Sort newest first.

## 6. Database Impact

- Read `user_notifications`.

## 7. Security

- User can read only own notifications.

## 8. Failure Cases

- Unauthorized -> 401.
- Invalid pagination -> 400.

## 9. Acceptance Criteria

- Only unread notifications are returned.
- Deleted notifications are excluded.
- Pagination works consistently.

