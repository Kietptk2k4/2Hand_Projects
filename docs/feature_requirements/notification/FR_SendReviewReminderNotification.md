# Functional Requirement - Send Review Reminder Notification

## 1. Feature Overview

Send optional review reminder after delivered/completed order when buyer has not reviewed eligible item.

## 2. Actors

- **Notification Scheduler:** Triggers reminder job.
- **Commerce Service:** Owns order/review truth.
- **Buyer:** Receives reminder.

## 3. Scope

**In Scope:**

- Send reminder notification when Commerce confirms item is reviewable and not reviewed.
- Use deterministic idempotency key.
- Respect notification settings.

**Out of Scope:**

- Creating review.
- Querying Commerce DB directly.
- Marketing reminders.

## 4. Trigger

- Optional scheduled job after shipment delivered/order completed + configured delay.
- Or Commerce publishes explicit `REVIEW_REMINDER` event.

## 5. Business Rules

- MVP optional.
- Notification Service must not directly read Commerce DB; use event payload or internal API/projection.
- Do not spam user; one reminder per order item/reminder window.
- Suggested event key: `notification.review_reminder.{orderItemId}.{reminderDay}`.
- Default channels: in-app + push.

## 6. Database Impact

- Insert `notification_events` if scheduler creates internal event.
- Insert `user_notifications` if allowed.

## 7. Failure Cases

- Commerce eligibility unavailable -> retry/skip by policy.
- Missing buyer/order item -> failed.
- Duplicate reminder key -> idempotent success.

## 8. Acceptance Criteria

- Eligible buyer receives at most one reminder per configured reminder key.
- Reminder is not sent after review exists.
- Commerce remains source-of-truth for review eligibility.

