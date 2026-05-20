# Functional Requirement - Dismiss Announcement Notification

## 1. Feature Overview

Allow user to dismiss a dismissible system announcement notification.

## 2. Actors

- **User:** Dismisses announcement.
- **Notification API:** Validates notification metadata and ownership.

## 3. Scope

**In Scope:**

- Validate announcement is dismissible.
- Soft delete the announcement notification.

**Out of Scope:**

- Canceling Admin announcement.
- Dismissing announcement for other users.

## 4. API Contract

**Endpoint:** `POST /notification/api/v1/notifications/{notificationId}/dismiss`

**Auth:** Required user JWT.

## 5. Business Rules

- Notification must belong to current user.
- Notification `reference_type` should be `SYSTEM_ANNOUNCEMENT`.
- `metadata.dismissible = true` required.
- Dismiss maps to `is_deleted = true`.
- Non-dismissible announcement cannot be dismissed.

## 6. Database Impact

- Update `user_notifications.is_deleted`.

## 7. Failure Cases

- Notification not found -> 404.
- Not an announcement -> 400/409.
- Not dismissible -> 409.
- Unauthorized -> 401.

## 8. Security

- User can dismiss only own notification.
- Do not mutate Admin Service announcement state.

## 9. Acceptance Criteria

- Dismissible announcement is hidden for that user.
- Non-dismissible announcement remains visible.
- Dismiss does not affect other users.

