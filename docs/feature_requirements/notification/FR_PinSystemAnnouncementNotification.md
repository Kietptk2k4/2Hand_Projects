# Functional Requirement - Pin System Announcement Notification

## 1. Feature Overview

Represent pinned system announcement in user notification metadata so clients can display it prominently.

## 2. Actors

- **Admin Service:** Publishes announcement with `is_pinned`.
- **Notification Service:** Stores pinned metadata.
- **User Client:** Displays pinned announcement.

## 3. Scope

**In Scope:**

- Store `metadata.is_pinned = true`.
- Preserve announcement severity/order metadata.

**Out of Scope:**

- Admin pin/unpin action.
- Client layout implementation.

## 4. Trigger

- System announcement fan-out with `is_pinned = true`.

## 5. Business Rules

- Pinned state comes from Admin announcement payload.
- Notification Service should not decide pin policy.
- Pinned notification can still be dismissible only if `dismissible = true`.
- Clients should sort/display pinned announcements according to FE behavior later.

## 6. Database Impact

- Insert/update `user_notifications.metadata`.

## 7. Failure Cases

- Missing pin flag -> default false.
- Invalid metadata shape -> sanitize/fail fan-out.

## 8. Acceptance Criteria

- Pinned announcement notification contains pinned metadata.
- Non-pinned announcements do not appear pinned.
- Notification Service does not mutate Admin announcement state.

