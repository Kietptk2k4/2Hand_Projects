# CheckUserPermission - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "Check User Permission" in Auth Service.

In scope:
- View effective permission list of a specific user.
- Aggregate permissions from user-role and role-permission mappings.
- Return standardized API response wrapper.

Out of scope:
- Assign/revoke role to user.
- Assign/revoke permission to role.
- Generic authorize middleware behavior (covered by separate FR).

## 2. Source Docs
- `docs/feature-requirements/auth/FR_CheckUserPermission.md`
- `docs/feature-requirements/auth/FR_AuthorizeRequestAccordingToPermission.md`
- `docs/feature-requirements/auth/FR_AssignRolesToUsers.md`
- `docs/feature-requirements/auth/FR_ViewPermissionsOfRole.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/use-cases/uc-role-permission-management.md`
- `docs/business-flow/authorization-flow.md`
- `docs/database/auth_schema.md`

## 3. Endpoint Contract
### Endpoint
- Method: `GET`
- Path: `/api/v1/admin/users/{userId}/permissions`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Request path param
- `userId`: target user id (UUID format)

### Request body
- No request body required.

### Notes
- Actor must have role/permission-management permission (`ASSIGN_ROLE` in current implementation).
- Endpoint is read-only.

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach permission cua user thanh cong.",
  "data": {
    "user_id": "uuid-user-id",
    "permissions": [
      {
        "code": "ASSIGN_ROLE"
      },
      {
        "code": "USER_UPDATE"
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
  "message": "Lay danh sach permission cua user thanh cong.",
  "data": {
    "user_id": "uuid-user-id",
    "permissions": []
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 5. Error Handling (400/401/403/404/500)
- `400 Bad Request`
  - invalid UUID format in `userId`.
- `401 Unauthorized`
  - missing/invalid/expired JWT.
- `403 Forbidden`
  - actor does not have role/permission-management permission.
- `404 Not Found`
  - target user does not exist or target user is `DELETED`.
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
      "field": "userId",
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
- Validate `userId` path variable as UUID.
- Resolve actor permissions and require role-management permission (`ASSIGN_ROLE` in current logic).
- Query target user by `userId`:
  - missing or `DELETED` -> `404`.
- Aggregate permission codes by user:
  - from `user_roles` -> `role_permissions` -> `permissions`.
- Remove duplicates (query returns distinct values) and sort permissions by code.
- Return `200` with `data.user_id` and `data.permissions`.
- Read-only endpoint; no DB mutation.

## 7. FE Behavior
### Loading
- Show loading state/skeleton while fetching user permissions.

### Success states
- If `permissions.length > 0`: render permission list.
- If `permissions.length == 0`: show empty state, for example: "User chua co permission nao."

### Error handling UX
- `400`:
  - show invalid user id / invalid navigation state message.
- `401`:
  1. trigger refresh flow once (`POST /api/v1/auth/refresh`),
  2. if refresh success, retry request,
  3. if refresh fails, clear auth state and redirect login.
- `403`:
  - show no-permission state/message.
- `404`:
  - show user-not-found/deleted state and fallback action.
- `500`:
  - show retry CTA/button.

## 8. Acceptance Criteria
- API `GET /api/v1/admin/users/{userId}/permissions` requires JWT.
- Actor without role-management permission gets `403`.
- Invalid `userId` format gets `400`.
- Missing/deleted target user gets `404`.
- Existing user without any permission returns `200` with `permissions: []`.
- Existing user with permissions returns `200` and deduplicated permission list.
- Endpoint is read-only and follows standard response wrapper.

## 9. Prompt for Stitch (UI only)
```text
Create an admin "User Permission Detail" screen for 2Hands Auth Service.

API:
- GET /api/v1/admin/users/{userId}/permissions
- Bearer auth required
- Response data:
  user_id: string
  permissions: [{ code }]

Requirements:
- Show user_id header/summary section.
- Show loading skeleton while fetching.
- Render permissions list when data exists.
- Show empty state when permissions is [].
- Handle 400 invalid userId state.
- Handle 401 with refresh-then-retry; refresh fail -> redirect login.
- Handle 403 no-permission state.
- Handle 404 user-not-found/deleted state with fallback action.
- Handle 500 with retry button and user-friendly error.
- Responsive and accessible layout.
```
