# Functional Requirement - Mark All Notifications As Read

## 1. Feature Overview

Allow authenticated user to mark all own unread notifications as read.

## 2. Actors

- **User:** Marks all notifications read.
- **Notification API:** Performs bulk update.

## 3. Scope

**In Scope:**

- Bulk update current user's unread non-deleted notifications.
- Return updated count.

**Out of Scope:**

- Marking deleted notifications.
- Cross-user admin operation.

## 4. API Contract

**Endpoint:** `PATCH /notification/api/v1/notifications/read-all`

**Auth:** Required user JWT.

## 5. Business Rules

- Update only `user_id = current_user_id`.
- Update only `is_read = false AND is_deleted = false`.
- Set `read_at = now()` for updated rows.
- Operation is idempotent.

## 6. Database Impact

- Bulk update `user_notifications`.

## 7. Transaction

- One transaction for bulk update.
- For very large user datasets, batch update can be introduced later.

## 8. Failure Cases

- Unauthorized -> 401.
- DB timeout -> retryable API failure.

## 9. Acceptance Criteria

- All current user's visible unread notifications become read.
- Other users' notifications are unaffected.
- Repeating request returns success and zero/new updated count.

