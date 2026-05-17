# AuthorizeRequestAccordingToPermission - API and Behavior Spec

## 1. Scope
This document defines backend authorization behavior and frontend handling for the feature "Authorize Request According To Permission" in Auth Service.

In scope:
- Enforce permission-based authorization for protected admin/RBAC endpoints.
- Return standardized `401`/`403` responses in API wrapper format.
- Document FE handling for unauthorized and forbidden states.

Out of scope:
- Role assignment/revocation business logic details.
- Role/permission CRUD features.
- Generic policy engine design (ABAC, policy DSL, etc.).

## 2. Source Docs
- `docs/feature-requirements/auth/FR_AuthorizeRequestAccordingToPermission.md`
- `docs/feature-requirements/auth/FR_AssignRolesToUsers.md`
- `docs/feature-requirements/auth/FR_ViewRoleList.md`
- `docs/feature-requirements/auth/FR_ViewPermissionsOfRole.md`
- `docs/feature-requirements/auth/FR_CheckUserPermission.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/use-cases/uc-role-permission-management.md`
- `docs/business-flow/authorization-flow.md`

## 3. Authorization Contract (Cross-Cutting)
This is a cross-cutting behavior, not a single endpoint.

For protected endpoints:
- Missing/invalid/expired JWT -> `401 Unauthorized`
- Valid JWT but missing required permission -> `403 Forbidden`
- Valid JWT and sufficient permission -> request proceeds to business logic

Current RBAC endpoints covered:
- `GET /api/v1/admin/roles`
- `GET /api/v1/admin/roles/{roleId}/permissions`
- `GET /api/v1/admin/users/{userId}/permissions`
- `POST /api/v1/admin/users/{userId}/roles`
- `DELETE /api/v1/admin/users/{userId}/roles/{roleId}`

Current permission rule:
- Role-management endpoints require permission `ASSIGN_ROLE` (checked in use cases).

## 4. Standard Error Responses
### 401 Unauthorized (Authentication required)
```json
{
  "code": 401,
  "success": false,
  "message": "Authentication required",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-18T00:00:00Z"
}
```

### 403 Forbidden (Access denied)
```json
{
  "code": 403,
  "success": false,
  "message": "Access denied",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-18T00:00:00Z"
}
```

## 5. Backend Behavior Summary
- Security layer (`JwtAuthenticationFilter` + `SecurityConfig`) enforces authentication for admin RBAC routes.
- Each RBAC use case validates actor authorization by:
  1. reading actor permission set via `PermissionQueryRepository.findPermissionCodesByUserId(actorUserId)`,
  2. checking required permission with `AuthorizationDomainService.hasPermission(...)`.
- If permission check fails, use case throws `AppException(ErrorCode.FORBIDDEN, ...)` -> standardized `403`.
- If authentication context is missing/invalid, controller/use case returns standardized `401`.
- Authorization flow is read-only; no DB mutation in permission-check steps.

## 6. FE Behavior
### Authentication failure (`401`)
- Attempt refresh flow once (`POST /api/v1/auth/refresh`) via interceptor.
- If refresh succeeds, retry original request.
- If refresh fails, clear local auth state and redirect to login.

### Authorization failure (`403`)
- Show "no permission" UX state/message.
- Hide or disable forbidden admin actions.
- Do not auto-retry `403` requests in a loop.

### Security UX notes
- Never display or log token-like sensitive data on client.
- Keep messaging generic ("Access denied"), avoid exposing internal permission names to end users.

## 7. Acceptance Criteria
- Protected RBAC endpoints return `401` when JWT is missing/invalid.
- Protected RBAC endpoints return `403` when actor lacks required permission.
- Authorized actors can access protected RBAC endpoints normally.
- Error responses follow standard wrapper format (`code/success/message/data/errors/timestamp`).
- FE supports refresh-retry for `401` and no-permission state for `403`.

## 8. Prompt for Stitch (UI only)
```text
Create admin-state UI patterns for authorization handling in 2Hands.

Context:
- Protected RBAC APIs can return:
  - 401 Authentication required
  - 403 Access denied

Requirements:
- Add reusable UI states/components for:
  1) Unauthorized (401): session expired/login required
  2) Forbidden (403): no permission to access feature
- Support refresh-then-retry behavior for 401 in data-fetch pages.
- If refresh fails: clear auth state and route to Login page.
- For 403: show friendly no-permission message and hide restricted actions.
- Include retry action where appropriate for transient failures after re-auth.
- Ensure responsive and accessible design.
```
