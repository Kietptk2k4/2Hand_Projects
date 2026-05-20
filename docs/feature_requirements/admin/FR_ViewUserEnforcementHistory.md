# Functional Requirement - View User Enforcement History

## 1. Feature Overview

Cho phep admin xem lich su enforcement cua mot user, bao gom cac enforcement da active, revoked, expired va log thay doi trang thai.

## 2. Actors

- **Admin/Support/Moderator:** Xem history de dieu tra.
- **System:** Query enforcement records and logs.

## 3. Scope

**In Scope:**

- List enforcements by user.
- Include status transition logs.
- Pagination and sorting newest first.

**Out of Scope:**

- Create/revoke enforcement.
- Auth login history.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/users/{userId}/enforcements/history`

**Auth:** Required, permission `USER_ENFORCEMENT_READ`.

## 5. Business Rules

- Admin must have investigation/enforcement read permission.
- Return all statuses: `ACTIVE`, `REVOKED`, `EXPIRED`.
- Logs should show old/new status, admin id/system, note and timestamp.

## 6. Database Impact

- Read `user_enforcements`.
- Read `user_enforcement_logs`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT admin required.
- No secret/token data returned.

## 9. Failure Cases

- User not found according Auth/support lookup -> 404.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized admin can view enforcement history.
- History includes transition logs.
- Unauthorized admin cannot access.

