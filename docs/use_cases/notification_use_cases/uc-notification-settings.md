# UC - Notification Settings

## 1. Overview

Use case nay mo ta cach user xem va cap nhat notification preferences theo `event_type`, gom `allow_push`, `allow_email`, `allow_in_app`.

## 2. Actors

- **User:** Quan ly settings cua minh.
- **Notification API:** Read/update settings.
- **Notification Worker:** Apply settings khi route event.

## 3. Related Data

- `user_notification_settings`

## 4. Preconditions

- User authenticated.
- Event type duoc support trong Notification Service policy.

## 5. Business Rules

- `(user_id, event_type)` unique.
- Missing setting uses default policy.
- User chi update settings cua minh.
- Critical event override phai duoc define ro.
- Changing settings khong backfill old notifications.

## 6. Sub-Use Cases

### UC-SETTINGS-01: View Notification Settings

Main flow:

1. User calls `GET /notification-settings`.
2. API authenticates JWT.
3. API loads explicit rows for user.
4. API merges with default channel policy.
5. API returns effective settings.

Postconditions:

- User thay settings hien tai va default effective values.

### UC-SETTINGS-02: Update Notification Setting

Main flow:

1. User calls `PUT /notification-settings/{eventType}`.
2. API validates event type.
3. API validates boolean flags.
4. API upserts row `(user_id, event_type)`.
5. API updates `updated_at`.

Postconditions:

- Future notification delivery uses new settings.

### UC-SETTINGS-03: Apply Settings During Delivery

Main flow:

1. Worker resolves recipient and event type.
2. Worker loads explicit setting.
3. If missing, worker uses default policy.
4. Worker applies channel flags.
5. Worker applies critical override if configured.

## 7. Failure Cases

- Unsupported event type.
- Unauthorized request.
- User tries update another user's settings.
- Invalid payload.
- Concurrent update; last write wins.

## 8. Security

- `user_id` from JWT.
- Settings endpoint must not expose other users.
- Critical override must be auditable in code/spec.

## 9. Acceptance Criteria

- User can view effective settings.
- User can update per-event channel flags.
- Delivery respects settings.
- Missing row uses default policy.
- Settings change affects future events only.

