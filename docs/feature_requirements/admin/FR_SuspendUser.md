# Functional Requirement - Suspend User

## 1. Feature Overview

Cho phep admin suspend user. Suspend la enforcement lam user khong login duoc va Auth Service can revoke active refresh sessions.

## 2. Actors

- **Admin/Moderator:** Suspend user.
- **Auth Service:** Apply user status and revoke sessions.
- **Admin Service:** Store enforcement, audit and publish event.

## 3. Scope

**In Scope:**

- Create `SUSPEND` enforcement.
- Write enforcement log.
- Write admin action log.
- Publish `USER_SUSPENDED`.

**Out of Scope:**

- Password reset.
- Appeal workflow.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/users/{userId}/suspend`

**Auth:** Required, permission `USER_SUSPEND`.

**Request body:**

- `reason_code`
- `description`
- `expires_at` optional

## 5. Business Rules

- Reason required.
- Temporary suspend requires future `expires_at`.
- Permanent suspend has `expires_at = null`.
- Enforcement status starts `ACTIVE`.
- Auth should set user status `SUSPENDED` and revoke refresh sessions.

## 6. Database Impact

- Insert `user_enforcements`.
- Insert `user_enforcement_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required for local Admin DB writes.

## 8. Security

- Permission required.
- Admin id from JWT.

## 9. Failure Cases

- Missing permission -> 403.
- User not found -> 404.
- Invalid expiration -> 400.

## 10. Acceptance Criteria

- Authorized admin can suspend user.
- Enforcement/audit/outbox records are created.
- Auth receives suspend event or command.

