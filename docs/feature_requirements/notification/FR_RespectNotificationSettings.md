# Functional Requirement - Respect Notification Settings

## 1. Feature Overview

Ensure notification delivery respects user's per-event channel preferences.

## 2. Actors

- **Notification Worker:** Reads and applies settings.
- **User:** Owns preferences.

## 3. Scope

**In Scope:**

- Apply `allow_push`.
- Apply `allow_email`.
- Apply `allow_in_app`.
- Use default policy if setting row missing.

**Out of Scope:**

- Admin changing user preferences.
- Marketing subscription management.

## 4. Business Rules

- Lookup setting by `(user_id, event_type)`.
- Missing row falls back to default event policy.
- `allow_in_app = false` prevents in-app creation unless mandatory critical event.
- `allow_push = false` prevents push.
- `allow_email = false` prevents email unless critical security/account override applies.
- Settings affect future processing only.

## 5. Database Impact

- Read `user_notification_settings`.
- Channel-specific inserts/sends are skipped based on flags.

## 6. Failure Cases

- Settings DB read fails -> retry event.
- Invalid event type config -> fail event.

## 7. Security

- User settings are private to owner.
- Critical override behavior must be explicit in code/config.

## 8. Acceptance Criteria

- Disabled channel is not used for normal events.
- Missing setting uses documented defaults.
- Critical overrides are limited and deliberate.

