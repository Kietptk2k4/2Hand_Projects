# RevokeRoleFromUser - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "Revoke Role From User" in Auth Service.

In scope:
- Revoke one role from a target user by admin endpoint.
- Validate permission, existence, assignment state, self-revoke policy, and last-super-admin protection.
- Return standardized API wrapper for FE integration.

Out of scope:
- Assign role to user.
- Create/update/delete role definitions.
- Full role/permission management UI.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_RevokeRoleFromUser..md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/backend-convention.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/use-cases/uc-role-permission-management.md`
- `docs/business-flow/authorization-flow.md`
- `docs/database/auth_schema.md`

## 3. Endpoint Contract
### Endpoint
- Method: `DELETE`
- Path: `/api/v1/admin/users/{userId}/roles/{roleId}`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Request path params
- `userId`: target user id (UUID format)
- `roleId`: role id to revoke (UUID format)

### Request body
- No request body required.

### Notes
- Actor must have role-management permission (`ASSIGN_ROLE` in current implementation).
- Domain policy blocks self-revoke and protects last super admin.

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Thu hoi role khoi user thanh cong.",
  "data": {
    "user_id": "uuid-user-id",
    "role_id": "uuid-role-id"
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 5. Error Handling (400/401/403/404/409/500)
- `400 Bad Request`
  - invalid UUID format in `userId` or `roleId`.
- `401 Unauthorized`
  - missing/invalid/expired JWT.
- `403 Forbidden`
  - actor does not have permission to manage roles.
  - actor tries self-revoke (blocked by domain policy).
  - last-super-admin protection is triggered.
- `404 Not Found`
  - target user not found or target user status is `DELETED`.
  - role not found.
- `409 Conflict`
  - role is not assigned to target user.
  - `errors[0]` follows:
    - `field`: `role_id`
    - `reason`: `ROLE_NOT_ASSIGNED`
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

Example 403:
```json
{
  "code": 403,
  "success": false,
  "message": "Access denied",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

Example 409:
```json
{
  "code": 409,
  "success": false,
  "message": "Resource conflict",
  "data": null,
  "errors": [
    {
      "field": "role_id",
      "reason": "ROLE_NOT_ASSIGNED"
    }
  ],
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 6. Backend Behavior Summary
- Extract `actorUserId` from JWT principal.
- Validate and parse:
  - `userId` (path) as UUID.
  - `roleId` (path) as UUID.
- Resolve actor permissions by querying current user permissions, then require role-management permission (`ASSIGN_ROLE` in current logic).
- Validate target resources:
  - target user exists and is not `DELETED`.
  - role exists.
- Load current role assignment of target user.
- Conflict check:
  - if target user does not have `roleId` -> `409` with `ROLE_NOT_ASSIGNED`.
- Apply domain policies:
  - block self-revoke via `ensureCanRevokeRole`.
  - if role code is `ADMIN`, protect last holder via `ensureNotRevokingLastSuperAdmin`.
- On success:
  - revoke role in domain aggregate and persist to `user_roles`.
  - revoke active sessions of target user so permission claims are refreshed on next login/refresh.
- Return response wrapped by `ApiResponse`.

## 7. FE Behavior
### Confirm modal
- Before revoke action, show confirm dialog:
  - title: "Thu hoi role?"
  - description: "Nguoi dung co the mat quyen truy cap lien quan den role nay."

### Loading and success
- Disable revoke button while request is pending.
- On success (`200`):
  - show success toast.
  - refresh target user role list.

### Error handling UX
- `409` with `ROLE_NOT_ASSIGNED`:
  - show info/warning that role is already not assigned; refresh role list.
- `403`:
  - show no-permission / protected-policy message and disable further revoke actions if needed.
- `400`:
  - show validation message for invalid route params.
- `401`:
  1. run refresh-token flow once (`POST /api/v1/auth/refresh`).
  2. if refresh success, retry revoke request.
  3. if refresh fails, clear auth state and redirect login.
- `500`:
  - show generic error toast and allow retry.

## 8. Acceptance Criteria
- API requires JWT and returns standard wrapper.
- Actor missing role-management permission gets `403`.
- Invalid UUID input gets `400`.
- Missing user/role (or deleted user) gets `404`.
- Role not assigned to target user gets `409` with `ROLE_NOT_ASSIGNED`.
- Self-revoke is blocked and mapped to `403`.
- Last-super-admin revoke is blocked and mapped to `403`.
- Successful revoke returns `200` with `user_id` and `role_id`.

## 9. Prompt for Stitch (UI only)
```text
Create an admin "Revoke Role From User" interaction for 2Hands Auth Service.

API:
- DELETE /api/v1/admin/users/{userId}/roles/{roleId}
- Bearer auth required
- Success response data: { "user_id": "...", "role_id": "..." }

Requirements:
- Role list with revoke action button per role.
- Confirm modal before revoke.
- Loading state on clicked revoke action.
- On success: show toast and refresh role list.
- Handle errors:
  - 400: invalid param message
  - 401: refresh then retry; refresh fail -> redirect login
  - 403: no-permission/policy-protected message
  - 404: user/role not found state
  - 409 with reason ROLE_NOT_ASSIGNED: show already-removed message + refresh
  - 500: retry-friendly generic error
- Responsive and accessible UI.
```
