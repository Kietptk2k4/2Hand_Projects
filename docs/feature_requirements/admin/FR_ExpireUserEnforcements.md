# Functional Requirement - Expire User Enforcements

## 1. Feature Overview

Background job tu dong het han enforcement tam thoi khi `expires_at <= now`, cap nhat trang thai va publish event cho Auth/Social/Commerce neu can.

## 2. Actors

- **Scheduler/System:** Runs expiration job.
- **Admin Service:** Owns enforcement records.
- **Other Services:** Consume expiration event.

## 3. Scope

**In Scope:**

- Find active expired enforcements.
- Mark as `EXPIRED`.
- Publish `USER_ENFORCEMENT_EXPIRED`.

**Out of Scope:**

- Manual revoke.
- Permanent ban expiration unless explicitly configured.

## 4. Trigger

- Scheduled job, e.g. every 1-5 minutes depending on scale.

## 5. Business Rules

- Only active temporary enforcements with `expires_at` are eligible.
- Job must be idempotent.
- Each expired enforcement should produce at most one expiration event.
- Permanent enforcements do not expire automatically.

## 6. Database Impact

- Update `user_enforcements.status`.
- Set expiration metadata if schema supports it.
- Insert `outbox_events`.

## 7. Transaction

- Process in small batches.
- Each batch should update enforcement and outbox in same transaction.

## 8. Security

- Internal scheduler only.

## 9. Failure Cases

- Partial batch failure -> retry later.
- Duplicate run -> no duplicate state change/event.

## 10. Acceptance Criteria

- Expired temporary enforcements become `EXPIRED`.
- Expiration events are published through outbox.
- Job is safe to rerun.

