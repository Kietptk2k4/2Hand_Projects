# Functional Requirement - Fan Out System Announcement

## 1. Feature Overview

Fan-out a published Admin system announcement into user in-app notifications.

## 2. Actors

- **Admin Service:** Publishes announcement event.
- **Notification Service:** Creates per-user notifications.
- **Auth Service:** Provides user list/audience if needed.
- **User:** Recipient.

## 3. Scope

**In Scope:**

- Consume `SYSTEM_ANNOUNCEMENT_SENT` or `SYSTEM_ANNOUNCEMENT_PUBLISHED`.
- Resolve recipients/audience.
- Insert announcement notifications in batches.
- Preserve pinned/dismissible metadata.

**Out of Scope:**

- Creating/canceling announcement.
- Marketing campaigns.
- Complex segmentation engine.

## 4. Event Contract

Required payload:

- `announcement_id`
- `title`
- `content`
- `severity`
- `is_pinned`
- `dismissible`
- `recipient_user_ids` or `target_audience`

## 5. Business Rules

- Admin Service owns announcement state.
- Fan-out must be batch-based.
- One announcement creates at most one notification per user.
- Critical announcement may bypass settings if policy says mandatory.
- Reference: `SYSTEM_ANNOUNCEMENT/announcement_id`.

## 6. Database Impact

- Insert many `user_notifications`.
- Update `notification_events`.

## 7. Transaction

- Process fan-out in batches.
- Duplicate insert per user/announcement is treated as success.

## 8. Failure Cases

- Missing announcement id -> failed.
- No recipients and no audience resolver -> failed.
- Batch partial failure -> retry idempotently.

## 9. Acceptance Criteria

- Target users receive announcement notifications.
- Pinned/dismissible flags are stored in metadata.
- Retry does not duplicate per-user announcement records.

