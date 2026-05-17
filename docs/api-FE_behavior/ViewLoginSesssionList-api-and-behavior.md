# ViewLoginSesssionList - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "View Login Session List" in Auth Service.

In scope:
- View active login sessions of current authenticated user.
- Display session identification fields for security monitoring UI.

Out of scope:
- Logout current session.
- Logout all sessions.
- Login history timeline (`login_logs`).

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ViewLoginSesssionList.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/use-cases/uc-session-management.md`
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md`

## 3. Endpoint Contract
### Endpoint
- Method: `GET`
- Path: `/api/v1/users/me/sessions`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Notes
- Return only sessions with status `ACTIVE`.
- Session order is newest first (`created_at DESC`).
- Response must not expose `token_hash`.

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach phien dang nhap thanh cong.",
  "data": {
    "sessions": [
      {
        "id": "9cfadc7f-4aa7-4917-a076-d8a5e8bb4be6",
        "device_id": "web-chrome-win11",
        "ip_address": "203.113.10.20",
        "user_agent": "Mozilla/5.0 ...",
        "status": "ACTIVE",
        "created_at": "2026-05-17T09:00:00Z",
        "updated_at": "2026-05-17T09:00:00Z",
        "expires_at": "2026-06-16T09:00:00Z"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

Empty state example:
```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach phien dang nhap thanh cong.",
  "data": {
    "sessions": []
  },
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

## 5. Error Handling
- `401 Unauthorized`: missing/invalid/expired access token.
- `500 Internal Server Error`: unexpected backend/system error.
- `400 Bad Request`: not expected in normal flow because this endpoint has no request body/query validation.

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
- Validate current auth context.
- Query repository:
  - `findByUserIdAndStatus(userId, SessionStatus.ACTIVE)`.
- Repository returns rows ordered by `created_at DESC`.
- Map to response DTO fields:
  - `id`, `device_id`, `ip_address`, `user_agent`, `status`, `created_at`, `updated_at`, `expires_at`.
- Never return `token_hash`.
- If there is no ACTIVE session, return `sessions: []` with HTTP 200.

## 7. FE Behavior
### Data fetching
- Trigger API call when user opens Security/Session screen.
- Read payload from wrapper at `response.data.sessions`.

### UI states
- **Loading:** show skeleton/list placeholder while request is pending.
- **Success with data:** show list cards/rows by newest first.
- **Success empty:** show empty state message, for example: "Khong co phien dang nhap dang hoat dong."
- **Error 500:** show retry CTA and non-technical message.

### 401 handling (refresh flow)
- If API returns 401:
  1. Trigger refresh token flow once (`POST /api/v1/auth/refresh`) via interceptor strategy.
  2. If refresh success, retry `/api/v1/users/me/sessions`.
  3. If refresh fails, clear auth state and redirect login.

### Security UX notes
- Do not render or log sensitive token values.
- Show device/IP/user-agent as informational fields only.

## 8. Acceptance Criteria
- API `GET /api/v1/users/me/sessions` requires JWT and follows standard wrapper.
- Response contains only ACTIVE sessions and is sorted by newest first.
- Response never includes `token_hash`.
- Empty ACTIVE sessions returns `sessions: []` with HTTP 200.
- FE screen supports loading, non-empty, empty, and 401-refresh-retry behavior.

## 9. Prompt for Stitch (UI only)
```text
Create a "Login Sessions" screen for 2Hands account security.

Data source:
- GET /api/v1/users/me/sessions
- Response wrapper: { code, success, message, data, errors, timestamp }
- Use data.sessions array with fields:
  id, device_id, ip_address, user_agent, status, created_at, updated_at, expires_at

Requirements:
- Show loading skeleton while fetching.
- Show session list sorted newest first (already provided by backend).
- Show empty state when sessions is [].
- Handle 401 with refresh-then-retry strategy; if refresh fails, redirect to login.
- Show retry UI for 500 errors.
- Do not display or log any token-like sensitive data.
- Keep layout responsive and accessible.
```
