# Functional Requirement - Check Admin Permission

## 1. Feature Overview

Kiem tra admin co permission cu the de thuc hien admin action nhu suspend user, remove product, hide review hoac update config.

## 2. Actors

- **Admin Service:** Enforce permission.
- **Auth Service:** Own permission source-of-truth.

## 3. Scope

**In Scope:**

- Check required permission for admin action.
- Support JWT claim-based or Auth API-based permission check.

**Out of Scope:**

- Permission assignment.
- Role hierarchy management.

## 4. API Contract

**Endpoint:** Internal authorization utility or Auth internal permission check.

**Input:**

- `admin_id`
- `permission_code`
- optional resource context.

## 5. Business Rules

- Permission must be checked before use case execution.
- Permission comes from Auth Service or trusted JWT claims.
- Missing permission returns 403.

## 6. Database Impact

- Admin Service: none.

## 7. Transaction

- None.

## 8. Security

- Do not trust client-provided permission.
- JWT must be verified.

## 9. Failure Cases

- Auth unavailable -> 503 or deny by default according policy.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Protected actions require explicit permission.
- Missing permission blocks action before domain mutation.
- Permission source is trusted.

