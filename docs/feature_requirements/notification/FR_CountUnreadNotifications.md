# Functional Requirement - Count Unread Notifications

## 1. Feature Overview

Return unread notification count for authenticated user, used for badge display.

## 2. Actors

- **User:** Sees unread badge.
- **Notification API:** Calculates count.

## 3. Scope

**In Scope:**

- Count unread non-deleted notifications.

**Out of Scope:**

- WebSocket live updates.
- Push badge sync with mobile OS.

## 4. API Contract

**Endpoint:** `GET /notification/api/v1/notifications/unread-count`

**Auth:** Required user JWT.

## 5. Business Rules

- Count only current user's records.
- Count only `is_read = false AND is_deleted = false`.
- Query should use index `(user_id, is_read, created_at)` with deleted filter.

## 6. Database Impact

- Read aggregate count from `user_notifications`.

## 7. Security

- `user_id` from JWT.

## 8. Failure Cases

- Unauthorized -> 401.
- DB unavailable -> 503.

## 9. Acceptance Criteria

- Count matches unread list total.
- Deleted notifications are not counted.
- User cannot count another user's notifications.

