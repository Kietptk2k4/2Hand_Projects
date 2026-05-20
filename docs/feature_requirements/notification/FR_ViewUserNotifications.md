# Functional Requirement - View User Notifications

## 1. Feature Overview

Allow authenticated user to view paginated in-app notifications.

## 2. Actors

- **User:** Views own notifications.
- **Notification API:** Returns notification list.

## 3. Scope

**In Scope:**

- Paginated list.
- Newest-first sorting.
- Exclude soft-deleted records.

**Out of Scope:**

- Admin viewing other users' notifications.
- Hard delete/retention.

## 4. API Contract

**Endpoint:** `GET /notification/api/v1/notifications`

**Auth:** Required user JWT.

**Query params:**

- `page`
- `size`

## 5. Business Rules

- Return only `user_id = current_user_id`.
- Filter `is_deleted = false`.
- Sort by `created_at DESC`.
- Metadata returned must be safe for the recipient.

## 6. Database Impact

- Read `user_notifications`.

## 7. Security

- `user_id` comes from JWT.
- Cross-user access must be impossible.

## 8. Failure Cases

- Unauthorized -> 401.
- Invalid pagination -> 400.

## 9. Acceptance Criteria

- User sees only own non-deleted notifications.
- Response is paginated and newest first.
- Deleted notifications are hidden.

