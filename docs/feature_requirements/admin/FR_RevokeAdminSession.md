# Functional Requirement - Revoke Admin Session

## 1. Feature Overview

Cho phep super admin hoac admin co permission revoke session cua admin khac thong qua Auth Service.

## 2. Actors

- **Super Admin:** Revoke admin session.
- **Auth Service:** Own session revocation.
- **Admin Service:** Authorize request and delegate.

## 3. Scope

**In Scope:**

- Revoke one admin session.
- Optional revoke all sessions of target admin.
- Write audit log for critical action.

**Out of Scope:**

- Direct Auth DB update.
- Password reset.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/admin-sessions/{sessionId}/revoke`

**Auth:** Required (admin permission)

## 5. Business Rules

- Requires permission such as `ADMIN_SESSION_REVOKE`.
- Target session must belong to an admin user.
- Admin Service calls Auth Service to revoke.
- Revoke action should write `admin_action_logs`.

## 6. Database Impact

- Admin Service: insert `admin_action_logs`.
- Auth Service: update session/token state.

## 7. Transaction

- Admin local audit transaction.
- Auth Service handles session transaction.

## 8. Security

- JWT admin required.
- Do not log token values.

## 9. Failure Cases

- Missing permission -> 403.
- Session not found -> 404.
- Auth Service unavailable -> 503.

## 10. Acceptance Criteria

- Authorized admin can revoke target session.
- Revoked session cannot refresh.
- Action is audit logged.

