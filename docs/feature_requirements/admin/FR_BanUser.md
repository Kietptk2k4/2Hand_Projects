# Functional Requirement - Ban User

## 1. Feature Overview

Cho phep admin ban user. Trong MVP, ban co the duoc Auth apply nhu suspended status, nhung Admin Service van luu enforcement type `BAN` de phan biet business decision.

## 2. Actors

- **Admin/Moderator:** Ban user.
- **Auth Service:** Apply login/session blocking.
- **Admin Service:** Store ban enforcement and publish event.

## 3. Scope

**In Scope:**

- Create `BAN` enforcement.
- Write logs and audit.
- Publish `USER_BANNED`.

**Out of Scope:**

- Permanent deletion.
- Legal/case management.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/users/{userId}/ban`

**Auth:** Required, permission `USER_SUSPEND` or dedicated `USER_BAN`.

**Request body:**

- `reason_code`
- `description`
- `expires_at` optional, usually null for permanent ban.

## 5. Business Rules

- Reason required.
- Ban enforcement status starts `ACTIVE`.
- MVP Auth effect can be same as suspend.
- User sessions should be revoked.
- Event must distinguish `USER_BANNED`.

## 6. Database Impact

- Insert `user_enforcements`.
- Insert `user_enforcement_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required for Admin DB writes.

## 8. Security

- Permission required.
- Admin id from JWT.

## 9. Failure Cases

- Missing permission -> 403.
- User not found -> 404.
- Invalid payload -> 400.

## 10. Acceptance Criteria

- Authorized admin can ban user.
- Ban is recorded separately from suspend.
- Event is published for Auth/Social/Commerce handling.

