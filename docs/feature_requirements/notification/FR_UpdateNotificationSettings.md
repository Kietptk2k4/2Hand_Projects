# Functional Requirement - Update Notification Settings

## 1. Feature Overview

Allow authenticated user to update notification channel preferences for one event type.

## 2. Actors

- **User:** Updates preferences.
- **Notification API:** Upserts setting.

## 3. Scope

**In Scope:**

- Update `allow_push`.
- Update `allow_email`.
- Update `allow_in_app`.
- Upsert `(user_id, event_type)`.

**Out of Scope:**

- Changing settings for another user.
- Backfilling old notifications.

## 4. API Contract

**Endpoint:** `PUT /notification/api/v1/notification-settings/{eventType}`

**Auth:** Required user JWT.

**Request body:**

- `allow_push`
- `allow_email`
- `allow_in_app`

## 5. Business Rules

- `event_type` must be supported.
- User can update only own settings.
- Update affects future notification processing only.
- Critical override policies may still apply to security/system events.

## 6. Database Impact

- Insert/update `user_notification_settings`.
- Set `updated_at = now()`.

## 7. Transaction

- Single upsert transaction.

## 8. Failure Cases

- Unsupported event type -> 400.
- Invalid booleans -> 400.
- Unauthorized -> 401.

## 9. Acceptance Criteria

- Setting is persisted for current user/event type.
- Future delivery respects updated setting.
- Existing notifications are unchanged.

