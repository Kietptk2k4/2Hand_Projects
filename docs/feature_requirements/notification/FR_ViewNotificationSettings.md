# Functional Requirement - View Notification Settings

## 1. Feature Overview

Allow authenticated user to view effective notification settings per event type.

## 2. Actors

- **User:** Views own settings.
- **Notification API:** Returns explicit and default settings.

## 3. Scope

**In Scope:**

- Read current user's settings.
- Merge with default channel policy.
- Return `allow_push`, `allow_email`, `allow_in_app`.

**Out of Scope:**

- Admin editing user settings.
- Marketing subscription center.

## 4. API Contract

**Endpoint:** `GET /notification/api/v1/notification-settings`

**Auth:** Required user JWT.

## 5. Business Rules

- User sees only own settings.
- Missing row uses default policy.
- Response should identify whether each setting is explicit or default if useful for UI.

## 6. Database Impact

- Read `user_notification_settings`.

## 7. Failure Cases

- Unauthorized -> 401.
- DB unavailable -> 503.

## 8. Security

- `user_id` comes from JWT.

## 9. Acceptance Criteria

- User can view effective settings.
- Defaults are shown when no explicit row exists.
- Other users' settings are never returned.

