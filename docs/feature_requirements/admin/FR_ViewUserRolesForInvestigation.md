# Functional Requirement - View User Roles For Investigation

## 1. Feature Overview

Cho phep admin/support xem roles va permissions cua user de dieu tra quyen han, admin abuse hoac cau hinh role sai.

## 2. Actors

- **Support/Admin:** Xem roles/permissions.
- **Auth Service:** Own role/permission source-of-truth.
- **Admin Service:** Authorize and proxy/read response.

## 3. Scope

**In Scope:**

- View assigned roles.
- View effective permissions.
- Include role status if Auth supports.

**Out of Scope:**

- Assign/revoke roles.
- Permission mutation.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/users/{userId}/roles`

**Auth:** Required, permission `USER_INVESTIGATION_READ` or `ROLE_READ`.

## 5. Business Rules

- Auth Service owns roles/permissions.
- Admin Service must not mutate roles in this endpoint.
- Sensitive internal permission metadata can be hidden if not needed.

## 6. Database Impact

- Admin Service: optional audit log.
- Auth Service: read roles/permissions.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Do not trust client-provided role data.

## 9. Failure Cases

- User not found -> 404.
- Auth unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized admin can view user roles.
- Endpoint is read-only.
- Unauthorized admin is denied.

