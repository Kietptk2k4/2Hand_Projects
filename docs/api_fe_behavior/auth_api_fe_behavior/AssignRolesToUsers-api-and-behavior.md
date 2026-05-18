# AssignRolesToUsers - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "Assign Roles To Users" in Auth Service.

In scope:
- Assign one role to a target user by admin endpoint.
- Validate permission, existence, duplicate assignment, and self-assign policy.
- Return standardized API wrapper for FE integration.

Out of scope:
- Revoke role from user.
- Create/update/delete role definitions.
- Full role/permission management UI.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_AssignRolesToUsers.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/backend-convention.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/database/auth_schema.md`

## 3. API Contract (Method/Path/Auth/Request)
### Endpoint
- Method: `POST`
- Path: `/api/v1/admin/users/{userId}/roles`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Request path param
- `userId`: target user id (UUID format)

### Request body
```json
{
  "role_id": "uuid-role-id"
}
```

### Notes
- Actor must have permission `ASSIGN_ROLE`.
- UUID format is validated for both `userId` and `role_id`.

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Gan role cho user thanh cong.",
  "data": {
    "user_id": "uuid-user-id",
    "role_id": "uuid-role-id"
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 5. Error Handling (400/403/404/409/500)
- `400 Bad Request`
  - invalid UUID format in `userId` or `role_id`.
- `403 Forbidden`
  - actor does not have permission `ASSIGN_ROLE`.
  - actor tries self-assign (blocked by domain policy).
- `404 Not Found`
  - target user not found or target user status is `DELETED`.
  - role not found.
- `409 Conflict`
  - role already assigned to target user.
  - `errors[0]` follows:
    - `field`: `role_id`
    - `reason`: `ALREADY_ASSIGNED`
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
      "field": "role_id",
      "reason": "INVALID_FORMAT"
    }
  ],
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
      "reason": "ALREADY_ASSIGNED"
    }
  ],
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 6. Backend Behavior Summary
- Extract `actorUserId` from JWT principal.
- Validate and parse:
  - `userId` (path) as UUID.
  - `role_id` (body) as UUID.
- Resolve actor permissions by querying current user permissions, then require `ASSIGN_ROLE`.
- Validate target resources:
  - target user exists and is not `DELETED`.
  - role exists.
- Apply domain policy:
  - block self-assign (via `RoleAssignmentDomainService`).
- Prevent duplicate assignment:
  - if mapping already exists -> `409` with `ALREADY_ASSIGNED`.
- On success:
  - persist mapping in `user_roles`.
  - revoke active sessions of target user so permission claims are refreshed on next login/refresh.
- Return response wrapped by `ApiResponse`.

## 7. FE Behavior (Form/Confirm/409/403)
### Form flow
- Admin opens role assignment form for selected user.
- Form has one required input: role selector (`role_id`).
- Submit triggers `POST /api/v1/admin/users/{userId}/roles`.

### Confirm flow
- Before submit, show confirm dialog:
  - title: "Gan role cho user?"
  - description: "Thao tac nay se cap nhat quyen truy cap cua user."

### Loading and success
- Disable submit button while request is pending.
- On success (`200`):
  - show success toast.
  - refresh assigned role list for target user.

### Error handling UX
- `409` with `ALREADY_ASSIGNED`:
  - show inline warning: role already assigned.
- `403`:
  - show no-permission message and hide/disable assign action.
- `400`:
  - map `errors[]` to form-level/field-level validation message.
- `401`:
  1. run refresh-token flow once (`POST /api/v1/auth/refresh`).
  2. if refresh success, retry assign request.
  3. if refresh fails, clear auth state and redirect login.
- `500`:
  - show generic error toast and allow retry.

## 8. Acceptance Criteria
- API requires JWT and returns standard wrapper.
- Actor missing `ASSIGN_ROLE` gets `403`.
- Invalid UUID input gets `400`.
- Missing user/role (or deleted user) gets `404`.
- Duplicate role assignment gets `409` with `ALREADY_ASSIGNED`.
- Self-assign is blocked and mapped to `403`.
- Successful assignment returns `200` with `user_id` and `role_id`.

## 9. Prompt for Stitch (UI only)
```text
Create an admin "Assign Role to User" form for 2Hands Auth Service.

API:
- POST /api/v1/admin/users/{userId}/roles
- Bearer auth required
- Request body: { "role_id": "uuid" }
- Success response data: { "user_id": "...", "role_id": "..." }

Requirements:
- Role dropdown (required) and submit button.
- Confirm dialog before submit.
- Loading state on submit button.
- Handle errors:
  - 400: field validation feedback
  - 403: no-permission message, disable action
  - 404: user/role not found state
  - 409 with reason ALREADY_ASSIGNED: show role-already-assigned message
  - 500: retry-friendly generic error
- Handle 401 with refresh-then-retry; refresh fail -> redirect login.
- Responsive and accessible layout.
```
