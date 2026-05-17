# ViewRoleList - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "View Role List" in Auth Service.

In scope:
- View system role list for admin role-management UI.
- Return role metadata in standardized API response wrapper.
- Enforce authentication and role-management permission.

Out of scope:
- Assign/revoke role actions.
- Create/update/delete role definitions.
- View permission details of each role.

## 2. Source Docs
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
- Path: `/api/v1/admin/roles`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Query params
- None.

### Notes
- Read-only endpoint.
- Actor must have role-management permission (`ASSIGN_ROLE` in current implementation).

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach role thanh cong.",
  "data": {
    "roles": [
      {
        "id": "uuid-role-id",
        "code": "ADMIN",
        "name": "Administrator",
        "created_at": "2026-05-17T10:00:00Z",
        "updated_at": "2026-05-17T10:00:00Z"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

Empty state example:
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach role thanh cong.",
  "data": {
    "roles": []
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 5. Error Handling (401/403/500)
- `401 Unauthorized`
  - missing/invalid/expired JWT.
- `403 Forbidden`
  - actor does not have role-management permission.
- `500 Internal Server Error`
  - unexpected backend/system failure.
- `400 Bad Request`
  - not applicable in normal flow because endpoint has no request body and no query validation.

Example 401:
```json
{
  "code": 401,
  "success": false,
  "message": "Authentication required",
  "data": null,
  "errors": null,
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

## 6. Backend Behavior Summary
- Extract `actorUserId` from Authentication principal.
- Query actor permissions and require role-management permission (`ASSIGN_ROLE` in current logic).
- Read role list from `roles` table via repository `findAll()`.
- Sort by `created_at ASC` (stable order for FE list rendering).
- Map response fields:
  - `id`, `code`, `name`, `created_at`, `updated_at`.
- Never mutate DB (read-only).
- If no roles exist, return `roles: []` with HTTP 200.

## 7. FE Behavior
### Loading
- Show loading state/skeleton when requesting role list.

### Success states
- If `roles.length > 0`: render role list/table/dropdown.
- If `roles.length == 0`: show empty state, for example: "Khong co role nao."

### Error handling UX
- `401`:
  1. trigger refresh flow once (`POST /api/v1/auth/refresh`),
  2. if refresh success, retry `/api/v1/admin/roles`,
  3. if refresh fails, clear auth state and redirect login.
- `403`:
  - show no-permission state/message and hide admin role-management actions.
- `500`:
  - show retry CTA/button.

## 8. Acceptance Criteria
- API `GET /api/v1/admin/roles` requires JWT.
- Actor without role-management permission gets `403`.
- Valid actor with permission gets `200` and standardized wrapper.
- Response includes `data.roles` with required fields.
- Empty role set returns `200` with `roles: []`.
- Endpoint does not mutate DB.

## 9. Prompt for Stitch (UI only)
```text
Create an admin "Role List" screen for 2Hands Auth Service.

API:
- GET /api/v1/admin/roles
- Bearer auth required
- Response data: { roles: [{ id, code, name, created_at, updated_at }] }

Requirements:
- Show loading skeleton while fetching.
- Render role list in table or list view (code, name, timestamps).
- Show empty state when roles is [].
- Handle 401 with refresh-then-retry; if refresh fails -> redirect login.
- Handle 403 with no-permission state.
- Handle 500 with retry button and user-friendly message.
- Responsive and accessible layout.
```
