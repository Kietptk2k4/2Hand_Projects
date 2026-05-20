# Functional Requirement - Authorize Admin API

## 1. Feature Overview

Dam bao moi Admin API duoc authenticate va authorize dung role/permission truoc khi execute use case.

## 2. Actors

- **Admin API:** Enforce authorization.
- **Auth Service:** Verify token/permission.
- **Admin User:** Goi protected endpoint.

## 3. Scope

**In Scope:**

- Validate JWT.
- Resolve admin identity.
- Check required permission.
- Reject unauthorized request.

**Out of Scope:**

- Login flow.
- Permission assignment.

## 4. API Contract

**Applies to:** All `/admin/api/v1/**` protected endpoints.

## 5. Business Rules

- Authentication happens before authorization.
- Admin id comes from token.
- Required permission is endpoint-specific.
- Destructive actions must require explicit permission, not only role.
- Unauthorized request must not execute domain mutation.

## 6. Database Impact

- None unless failed/critical access attempt logging is enabled.

## 7. Transaction

- Authorization happens before transaction for use case.

## 8. Security

- Fail closed when permission cannot be verified.
- Do not log token.

## 9. Failure Cases

- Missing/invalid token -> 401.
- Missing permission -> 403.
- Auth introspection unavailable -> 503 or deny by default.

## 10. Acceptance Criteria

- Every protected endpoint checks auth.
- Domain mutation never runs for unauthorized request.
- Required permission maps to endpoint action.

