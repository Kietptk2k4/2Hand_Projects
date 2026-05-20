# Functional Requirement - Initialize Default Notification Settings

## 1. Feature Overview

Initialize default notification settings for a user when needed, such as after `USER_CREATED` or first settings page access.

## 2. Actors

- **Notification Service:** Creates default rows.
- **User:** Receives default preferences.
- **Auth Service:** May publish `USER_CREATED`.

## 3. Scope

**In Scope:**

- Create default settings for supported event types.
- Avoid duplicate settings.
- Use configured channel policy.

**Out of Scope:**

- User onboarding UI.
- Marketing preference import.

## 4. Trigger

- `USER_CREATED` event.
- Lazy initialization on `GET /notification-settings`.

## 5. Business Rules

- Initialization must be idempotent.
- Do not overwrite explicit user settings.
- Defaults come from application/config policy.
- New event types can be added lazily without migrating all users immediately.

## 6. Database Impact

- Insert missing `user_notification_settings` rows.

## 7. Transaction

- Batch insert/upsert with conflict ignore.

## 8. Failure Cases

- Duplicate row -> ignore.
- Unsupported default config -> fail startup or initialization.
- DB unavailable -> retry/lazy initialize later.

## 9. Acceptance Criteria

- New user has sensible effective notification settings.
- Re-running initialization does not duplicate or overwrite explicit settings.
- Missing rows still resolve through default policy.

