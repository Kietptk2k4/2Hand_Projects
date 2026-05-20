# UC - System Announcement

## 1. Overview

Use case nay mo ta quan ly announcement toan platform: tao, publish, cancel, pin va cau hinh dismissible. Announcement co the duoc Notification Service fan-out qua event.

## 2. Actors

- **Admin/Super Admin:** Quan ly announcement.
- **Notification Service:** Consume publish event.
- **System:** Store announcement state.

## 3. Related Data

- `system_announcements`
- `admin_action_logs`
- `outbox_events`

## 4. Business Rules

- Severity: `INFO`, `WARNING`, `CRITICAL`.
- Status: `DRAFT`, `SENT`, `CANCELLED`.
- Publish sets `sent_at`.
- Critical publish should be audit logged.
- Cancelled announcement is not active.

## 5. Sub-Use Cases

### 5.1. Create Announcement

**Main Flow:** Admin creates draft with title, content, severity, pinned, dismissible.

### 5.2. Publish Announcement

**Main Flow:**

1. Admin checks permission.
2. System sets status `SENT` and `sent_at`.
3. System logs action.
4. System publishes `SYSTEM_ANNOUNCEMENT_PUBLISHED`.

### 5.3. Cancel Announcement

**Main Flow:** Admin sets announcement `CANCELLED` and logs action.

### 5.4. Pin Or Unpin Announcement

**Main Flow:** Admin updates `is_pinned`.

## 6. Acceptance Criteria

- Announcement lifecycle is valid.
- Publish requires permission.
- Published announcement has `sent_at`.
- Publish event is emitted through outbox.

