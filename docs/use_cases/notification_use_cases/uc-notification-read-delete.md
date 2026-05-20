# UC - Notification Read Delete

## 1. Overview

Use case nay mo ta cac API user-facing de xem notification, xem unread count, mark read, mark all read va soft delete notification.

## 2. Actors

- **User:** Read/manage own notifications.
- **Notification API:** Enforce JWT and ownership.
- **Notification DB:** Stores `user_notifications`.

## 3. Related Data

- `user_notifications`

## 4. Preconditions

- User authenticated.
- Notification records exist for user.

## 5. Business Rules

- User chi thao tac notification cua chinh minh.
- Default list excludes `is_deleted = true`.
- Unread count uses `is_read = false AND is_deleted = false`.
- Mark read is idempotent.
- Delete is soft delete.
- No hard delete by user API in MVP.

## 6. Sub-Use Cases

### UC-READ-01: View Notification List

Main flow:

1. User calls `GET /notifications`.
2. API authenticates JWT.
3. API queries by `user_id` and `is_deleted = false`.
4. API sorts newest first and paginates.
5. API returns notification list.

### UC-READ-02: View Unread Notifications And Count

Main flow:

1. User calls `GET /notifications/unread` or `/unread-count`.
2. API filters `is_read = false` and `is_deleted = false`.
3. API returns paginated unread list or count.

### UC-READ-03: Mark Notification As Read

Main flow:

1. User calls `PATCH /notifications/{notificationId}/read`.
2. API validates ownership.
3. If unread, set `is_read = true`, `read_at = now()`.
4. If already read, return success.

### UC-READ-04: Mark All Notifications As Read

Main flow:

1. User calls `PATCH /notifications/read-all`.
2. API updates current user's unread non-deleted rows.
3. API sets `read_at` for updated rows.

### UC-READ-05: Soft Delete Notification

Main flow:

1. User calls `DELETE /notifications/{notificationId}`.
2. API validates ownership.
3. API sets `is_deleted = true`.

## 7. Failure Cases

- Unauthenticated request.
- Notification not found.
- Notification belongs to another user.
- Invalid pagination.
- Concurrent mark read/delete.

## 8. Security

- Prefer 404 for cross-user notification id to avoid existence leak.
- `user_id` from JWT.
- Response metadata must be sanitized.

## 9. Acceptance Criteria

- User sees only own non-deleted notifications.
- Unread count is accurate.
- Mark read sets `read_at`.
- Mark all affects only current user.
- Delete hides without hard deleting.

