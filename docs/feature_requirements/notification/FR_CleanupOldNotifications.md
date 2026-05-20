# Functional Requirement - Cleanup Old Notifications

## 1. Feature Overview

Optional background job cleans old notifications according to retention policy to control storage growth.

## 2. Actors

- **Scheduler:** Triggers cleanup.
- **Notification Worker:** Cleans records.
- **User:** No direct action.

## 3. Scope

**In Scope:**

- Identify old notifications past retention.
- Soft delete or hard delete according to policy.
- Process in batches.

**Out of Scope:**

- User-triggered delete.
- Audit-critical retention rules outside Notification MVP.

## 4. Trigger

- Scheduled job, for example daily.

## 5. Business Rules

- MVP optional.
- Default retention example: 180 days, configurable.
- Prefer soft delete unless storage retention requires hard delete.
- Do not delete critical/system notifications if policy says retain.
- Cleanup must be idempotent and batch-based.

## 6. Database Impact

- Update or delete from `user_notifications` depending retention policy.

## 7. Transaction

- Process small batches to avoid long locks.

## 8. Failure Cases

- DB timeout -> retry next schedule.
- Invalid retention config -> job should fail safe and not delete.

## 9. Acceptance Criteria

- Old eligible notifications are cleaned safely.
- Recent notifications are not affected.
- Job can rerun without corrupting data.

