# Functional Requirement - Create In-App Notification

## 1. Feature Overview

Create a user-visible in-app notification in `user_notifications` from a processed notification event.

## 2. Actors

- **Notification Worker:** Creates notification.
- **User:** Recipient.

## 3. Scope

**In Scope:**

- Generate title/content.
- Set recipient, actor, reference and metadata.
- Respect `allow_in_app`.
- Prevent duplicate notifications.

**Out of Scope:**

- User list/read APIs.
- Push/email provider calls.

## 4. Business Rules

- Recipient must be resolved server-side.
- `title` and `content` come from trusted template.
- `metadata` must be sanitized.
- `delivery_status = SENT` when in-app record is ready and no required delivery remains.
- Unique key prevents duplicate per event/user/reference.
- Self social notification should be skipped.

## 5. Database Impact

Insert `user_notifications`:

- `notification_event_id`
- `user_id`
- `actor_id`
- `type`
- `title`
- `content`
- `reference_type`
- `reference_id`
- `metadata`
- `is_read = false`
- `is_deleted = false`

## 6. Transaction

- Insert should be atomic with event processing state update where possible.

## 7. Security

- Do not expose private content in notification body.
- Reference access is still authorized by owner service.

## 8. Failure Cases

- Missing recipient -> fail event.
- Missing template -> fail event.
- Duplicate insert -> treat as success.

## 9. Acceptance Criteria

- Notification appears for correct user.
- Notification is unread by default.
- Duplicate event retry does not duplicate record.

