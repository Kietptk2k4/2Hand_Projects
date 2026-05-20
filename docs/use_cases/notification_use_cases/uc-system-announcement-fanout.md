# UC - System Announcement Fan-Out

## 1. Overview

Use case nay mo ta cach Notification Service fan-out system announcement da publish tu Admin Service thanh in-app notifications cho target audience.

## 2. Actors

- **Admin Service:** Own announcement and publish event.
- **Notification Service:** Fan-out notification.
- **Auth Service:** Own user list if all-user enumeration is needed.
- **User:** Receives announcement.

## 3. Related Data

- `notification_events`
- `user_notifications`
- `user_notification_settings`

Logical upstream:

- Admin `system_announcements`
- Auth users/read model for audience enumeration.

## 4. Preconditions

- Admin announcement da publish.
- Event co `announcement_id`, `title`, `content`, `severity`, `is_pinned`, `dismissible`.
- Event co `recipient_user_ids` hoac target audience resolver kha dung.

## 5. Business Rules

- Admin Service owns announcement state.
- Notification Service only creates recipient notification records.
- Fan-out must be batch-based.
- One announcement creates at most one notification per user.
- Pinned/dismissible flags stored in metadata.
- Critical announcement may bypass settings if policy requires.

## 6. Sub-Use Cases

### UC-ANN-01: Fan-Out To Explicit Recipients

Main flow:

1. Notification consumes `SYSTEM_ANNOUNCEMENT_SENT` or `SYSTEM_ANNOUNCEMENT_PUBLISHED`.
2. Worker reads `recipient_user_ids` from payload.
3. Worker inserts `user_notifications` in batches.
4. Worker sets reference `SYSTEM_ANNOUNCEMENT/announcement_id`.
5. Worker marks event completed.

### UC-ANN-02: Fan-Out To Audience

Main flow:

1. Event contains `target_audience`.
2. Worker resolves recipients by paging Auth internal API/projection.
3. Worker inserts notifications in batches.
4. Duplicate insert is treated as success.

Exception flow:

- If audience resolver is unavailable, event fails retryable.

### UC-ANN-03: Dismiss Announcement

Main flow:

1. User deletes/dismisses announcement notification.
2. API validates ownership.
3. API sets `is_deleted = true`.

Postconditions:

- Announcement hidden for that user.

## 7. Failure Cases

- Missing announcement id.
- Missing recipients and no audience resolver.
- Batch partial failure.
- Auth paging unavailable.
- Duplicate fan-out after retry.

## 8. Security

- Announcement content should be user-safe.
- User can dismiss only own notification.
- Notification does not modify Admin announcement state.

## 9. Acceptance Criteria

- Published announcement creates notifications for intended recipients.
- Fan-out is idempotent per user/announcement.
- Pinned/dismissible metadata is preserved.
- Batch retry does not duplicate records.
- All-user fan-out has clear recipient strategy.

