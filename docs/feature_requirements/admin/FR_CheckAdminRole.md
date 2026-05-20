# Functional Requirement - Check Admin Role

## 1. Feature Overview

Kiem tra admin co role phu hop de truy cap admin area hoac nhom tinh nang nhat dinh.

## 2. Actors

- **Admin Service:** Check role from JWT/Auth.
- **Auth Service:** Source-of-truth roles.

## 3. Scope

**In Scope:**

- Check roles such as `MODERATOR`, `SUPPORT`, `SUPER_ADMIN`.
- Use JWT claims or Auth introspection.

**Out of Scope:**

- Role assignment.
- Permission management.

## 4. API Contract

**Endpoint:** Internal authorization utility or `GET /admin/api/v1/me/roles`

**Auth:** Required.

## 5. Business Rules

- Role data comes from Auth Service.
- Admin Service should not define role source-of-truth.
- Role check can be cached only according security policy.

## 6. Database Impact

- Admin Service: none.

## 7. Transaction

- None.

## 8. Security

- JWT must be verified.
- Do not trust client-provided role.

## 9. Failure Cases

- Missing/invalid token -> 401.
- Required role missing -> 403.

## 10. Acceptance Criteria

- Role check uses trusted Auth data.
- Required role missing denies access.
- Client cannot spoof role.

