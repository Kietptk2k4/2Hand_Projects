# ViewPermissionsOfRole - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "View Permissions Of Role" in Auth Service.

In scope:
- View permissions of a specific role for admin role-management UI.
- Return role metadata and permission list in standardized API response wrapper.
- Enforce authentication and role-management permission.

Out of scope:
- Assign/revoke role for user.
- Assign/revoke permission for role.
- Create/update/delete role or permission definitions.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ViewPermissionsOfRole.md`
- `docs/feature-requirements/auth/FR_ViewRoleList.md`
- `docs/feature-requirements/auth/FR_AssignRolesToUsers.md`
- `docs/feature-requirements/auth/FR_RevokeRoleFromUser..md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/use-cases/uc-role-permission-management.md`
- `docs/business-flow/authorization-flow.md`
- `docs/database/auth_schema.md`

## 3. Endpoint Contract
### Endpoint
- Method: `GET`
- Path: `/api/v1/admin/roles/{roleId}/permissions`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Request path param
- `roleId`: target role id (UUID format)

### Request body
- No request body required.

### Notes
- Actor must have role-management permission (`ASSIGN_ROLE` in current implementation).
- Read-only endpoint.

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach permission cua role thanh cong.",
  "data": {
    "role": {
      "id": "uuid-role-id",
      "code": "ADMIN",
      "name": "Administrator"
    },
    "permissions": [
      {
        "code": "USER_READ",
        "description": "Read user information"
      },
      {
        "code": "USER_UPDATE",
        "description": "Update user information"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

Empty permission list example:
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach permission cua role thanh cong.",
  "data": {
    "role": {
      "id": "uuid-role-id",
      "code": "MODERATOR",
      "name": "Moderator"
    },
    "permissions": []
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 5. Error Handling (400/401/403/404/500)
- `400 Bad Request`
  - invalid UUID format in `roleId`.
- `401 Unauthorized`
  - missing/invalid/expired JWT.
- `403 Forbidden`
  - actor does not have role-management permission.
- `404 Not Found`
  - role does not exist.
- `500 Internal Server Error`
  - unexpected backend/system failure.

Example 400:
```json
{
  "code": 400,
  "success": false,
  "message": "Du lieu khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "roleId",
      "reason": "INVALID_FORMAT"
    }
  ],
  "timestamp": "2026-05-17T10:00:00Z"
}
```

Example 404:
```json
{
  "code": 404,
  "success": false,
  "message": "Resource not found",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 6. Backend Behavior Summary
- Extract `actorUserId` from Authentication principal.
- Validate `roleId` path variable as UUID.
- Resolve actor permissions by querying current user permissions, then require role-management permission (`ASSIGN_ROLE` in current logic).
- Query role by `roleId`:
  - role missing -> `404`.
- Query role permissions from `role_permissions` join `permissions`.
- Return:
  - `role`: `id`, `code`, `name`
  - `permissions`: list of `{ code, description }`
- If role has no permissions, return `200` with `permissions: []`.
- Endpoint is read-only and does not mutate DB.

## 7. FE Behavior
### Loading
- Show loading state/skeleton while fetching role permission details.

### Success states
- If `permissions.length > 0`: render role permission list.
- If `permissions.length == 0`: show empty state, for example: "Role chua co permission."

### Error handling UX
- `400`:
  - show invalid role id / invalid navigation state message.
- `401`:
  1. trigger refresh flow once (`POST /api/v1/auth/refresh`),
  2. if refresh success, retry request,
  3. if refresh fails, clear auth state and redirect login.
- `403`:
  - show no-permission state/message.
- `404`:
  - show role-not-found state and fallback action back to role list.
- `500`:
  - show retry CTA/button.

## 8. Acceptance Criteria
- API `GET /api/v1/admin/roles/{roleId}/permissions` requires JWT.
- Actor without role-management permission gets `403`.
- Invalid `roleId` format gets `400`.
- Missing role gets `404`.
- Existing role with no permission returns `200` and `permissions: []`.
- Existing role with permissions returns `200` and expected list.
- Endpoint is read-only and keeps standardized response wrapper.

## 9. Prompt for Stitch (UI only)
```text
Create an admin "Role Permissions Detail" screen for 2Hands Auth Service.

API:
- GET /api/v1/admin/roles/{roleId}/permissions
- Bearer auth required
- Response data:
  role: { id, code, name }
  permissions: [{ code, description }]

Requirements:
- Show role header information (code, name).
- Show loading skeleton while fetching.
- Render permissions list when data exists.
- Show empty state when permissions is [].
- Handle 400 invalid roleId state.
- Handle 401 with refresh-then-retry; refresh fail -> redirect login.
- Handle 403 no-permission state.
- Handle 404 role-not-found state with back-to-role-list action.
- Handle 500 with retry button and user-friendly error.
- Responsive and accessible layout.
```
