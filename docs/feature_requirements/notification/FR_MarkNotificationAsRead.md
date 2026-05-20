# Functional Requirement - Mark Notification As Read

## 1. Feature Overview

Allow user to mark one notification as read.

## 2. Actors

- **User:** Marks notification read.
- **Notification API:** Updates read state.

## 3. Scope

**In Scope:**

- Update `is_read`.
- Set `read_at`.
- Idempotent read operation.

**Out of Scope:**

- Opening linked resource.

## 4. API Contract

**Endpoint:** `PATCH /notification/api/v1/notifications/{notificationId}/read`

**Auth:** Required user JWT.

## 5. Business Rules

- User can mark only own notification.
- If already read, return success without changing `read_at` unless policy says refresh.
- Deleted notification can be rejected or treated as hidden; prefer 404.

## 6. Database Impact

Update `user_notifications`:

- `is_read = true`
- `read_at = now()` if previously unread

## 7. Transaction

- Single-row update.

## 8. Failure Cases

- Notification not found -> 404.
- Other user's notification -> 404/403; prefer 404.
- Unauthorized -> 401.

## 9. Acceptance Criteria

- Unread notification becomes read.
- `read_at` is set.
- Operation is idempotent.

