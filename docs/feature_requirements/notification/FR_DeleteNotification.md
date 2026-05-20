# Functional Requirement - Delete Notification

## 1. Feature Overview

Allow user to hide a notification via soft delete.

## 2. Actors

- **User:** Deletes/hides own notification.
- **Notification API:** Updates soft delete flag.

## 3. Scope

**In Scope:**

- Set `is_deleted = true`.
- Exclude deleted notification from list/unread count.

**Out of Scope:**

- Hard delete.
- Retention cleanup.

## 4. API Contract

**Endpoint:** `DELETE /notification/api/v1/notifications/{notificationId}`

**Auth:** Required user JWT.

## 5. Business Rules

- DELETE defaults to soft delete.
- User can delete only own notification.
- Deleted notification is hidden from default list and unread count.
- Operation is idempotent.

## 6. Database Impact

Update `user_notifications`:

- `is_deleted = true`

## 7. Transaction

- Single-row update.

## 8. Failure Cases

- Notification not found -> 404.
- Other user's notification -> 404/403; prefer 404.
- Unauthorized -> 401.

## 9. Acceptance Criteria

- Deleted notification no longer appears in list.
- Deleted unread notification no longer contributes to unread count.
- Record is not hard deleted.

