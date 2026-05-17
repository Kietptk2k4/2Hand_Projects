# LogoutAllSesssion - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "Logout All Sessions" in Auth Service.

In scope:
- Logout all active login sessions of current authenticated user.
- Idempotent response behavior for repeated requests.

Out of scope:
- Logout only current session (`POST /api/v1/auth/logout`).
- Session list query (`GET /api/v1/users/me/sessions`).
- Access token blacklist at API Gateway layer.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_LogoutAllSesssion.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/Logout-api-and-behavior.md`
- `docs/api-FE_behavior/ViewLoginSesssionList-api-and-behavior.md`
- `docs/use-cases/uc-session-management.md`
- `docs/business-flow/session-management-flow.md`

## 3. Endpoint Contract
### Endpoint
- Method: `POST`
- Path: `/api/v1/users/me/sessions/logout-all`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Request Body
- No request body required.

### Notes
- Endpoint is user-scoped via `/users/me` convention.
- Backend revokes only sessions with `status = ACTIVE`.

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Dang xuat tat ca phien dang nhap thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

## 5. Error Handling
- `401 Unauthorized`: missing/invalid/expired access token.
- `500 Internal Server Error`: unexpected backend/system error.
- `400 Bad Request`: not applicable in normal flow because no request body/query payload validation is required.

Example 401:
```json
{
  "code": 401,
  "success": false,
  "message": "Authentication required",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

## 6. Backend Behavior Summary
- Extract `userId` from Authentication principal (`/users/me` convention).
- Validate auth context and user availability.
- Execute `refreshTokenSessionRepository.revokeAllByUserId(userId)`.
- Repository updates only rows where `status = ACTIVE` to `REVOKED`.
- Idempotent behavior:
  - if updated rows = 0, API still returns 200 success.
- No request body required, no token value exposed in response.

## 7. FE Behavior
### Confirm action
- User clicks `Dang xuat tat ca thiet bi`.
- Show confirmation modal:
  - title: "Dang xuat tat ca thiet bi?"
  - description: "Ban se can dang nhap lai tren cac thiet bi khac."

### Loading
- While API request is pending:
  - disable confirm button
  - show loading indicator on action button.

### Success flow (200)
- Clear local auth state:
  - remove access token
  - remove refresh token
  - clear user/profile cache
- Redirect to login screen.
- Optional toast: "Da dang xuat tat ca phien dang nhap."

### 401 handling (refresh flow)
- If API returns 401:
  1. Attempt refresh once (`POST /api/v1/auth/refresh`) via auth interceptor.
  2. If refresh success, retry logout-all request.
  3. If refresh fails, clear local auth and redirect login.

### 500 handling
- Keep current page and show retry-friendly error message.
- Do not log token values in UI console/analytics.

## 8. Acceptance Criteria
- API `POST /api/v1/users/me/sessions/logout-all` requires JWT and follows standard response wrapper.
- If user has ACTIVE sessions, they are updated to `REVOKED`.
- If user has no ACTIVE sessions, API still returns 200 success.
- Missing/invalid JWT returns 401.
- FE flow supports confirm modal, loading state, success redirect, and 401 refresh-retry behavior.

## 9. Prompt for Stitch (UI only)
```text
Create UI flow for "Dang xuat tat ca thiet bi" in account security page.

API:
- POST /api/v1/users/me/sessions/logout-all
- JWT required, no request body
- Success message: "Dang xuat tat ca phien dang nhap thanh cong."

Requirements:
- Add a danger-style button: "Dang xuat tat ca thiet bi"
- Show confirmation modal before submit
- Show loading state while calling API
- On success: clear local auth state and redirect to Login
- Handle 401 with refresh-then-retry strategy; refresh fail -> redirect Login
- Show retry-friendly error message for 500
- Responsive and accessible UI
```
