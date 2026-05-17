# TrackLoginHistory - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "Track Login History" in Auth Service.

In scope:
- View login history of current authenticated user.
- Support pagination using `limit` and `offset`.
- Include both successful and failed login attempts.

Out of scope:
- Session list management.
- Logout current/all session actions.
- Admin view of other users' login history.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_TrackLoginHistory.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/ViewLoginSesssionList-api-and-behavior.md`
- `docs/api-FE_behavior/LogoutAllSesssion-api-and-behavior.md`
- `docs/use-cases/uc-session-management.md`
- `docs/database/auth_schema.md`

## 3. Endpoint Contract
### Endpoint
- Method: `GET`
- Path: `/api/v1/users/me/login-history`
- Auth: Required (`Authorization: Bearer <access_token>`)

### Query Params
- `limit` (optional): default `20`, min `1`, max `100`
- `offset` (optional): default `0`, min `0`

### Notes
- Backend returns records ordered by `created_at DESC`.
- Response includes only audit-safe fields (`login_method`, `ip_address`, `user_agent`, `success`, `created_at`).

## 4. Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay lich su dang nhap thanh cong.",
  "data": {
    "items": [
      {
        "id": "a59411f3-201a-4872-acde-55591524f1a8",
        "login_method": "EMAIL",
        "ip_address": "203.113.10.20",
        "user_agent": "Mozilla/5.0 ...",
        "success": true,
        "created_at": "2026-05-17T09:00:00Z"
      },
      {
        "id": "c96b5e84-c5d7-4620-adce-86bb4e594cd3",
        "login_method": "GOOGLE",
        "ip_address": "10.10.1.25",
        "user_agent": "Chrome/125.0",
        "success": false,
        "created_at": "2026-05-16T07:10:00Z"
      }
    ],
    "limit": 20,
    "offset": 0
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
  "message": "Lay lich su dang nhap thanh cong.",
  "data": {
    "items": [],
    "limit": 20,
    "offset": 0
  },
  "errors": null,
  "timestamp": "2026-05-17T09:01:00Z"
}
```

## 5. Error Handling (400/401/500)
- `400 Bad Request`:
  - invalid `limit` (out of range 1..100)
  - invalid `offset` (< 0)
- `401 Unauthorized`:
  - missing/invalid/expired JWT
- `500 Internal Server Error`:
  - unexpected backend/system failure

Example 400:
```json
{
  "code": 400,
  "success": false,
  "message": "Limit khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "limit",
      "reason": "INVALID_RANGE"
    }
  ],
  "timestamp": "2026-05-17T09:01:00Z"
}
```

## 6. Backend Behavior Summary
- Extract `userId` from Authentication principal (`/users/me` convention).
- Validate pagination input:
  - `limit` in range `[1..100]`
  - `offset >= 0`
- Query repository:
  - `loginLogRepository.findByUserId(userId, limit, offset)`
- Repository SQL returns results in `created_at DESC`.
- Include both success and failure attempts.
- Do not return sensitive data (`password`, `token`, `token_hash`).
- If no logs found, return `items: []` with HTTP 200.

## 7. FE Behavior
### Loading
- Show list skeleton/loading rows while request is pending.

### Pagination / load-more
- Initial call uses `limit=20&offset=0`.
- For "Load more":
  - keep same `limit`
  - increase `offset` by `limit`
  - append new items to existing list.

### Empty state
- If `items` is empty:
  - show empty state text, e.g. "Chua co lich su dang nhap."

### 401 refresh flow
- On 401:
  1. Trigger refresh flow once (`POST /api/v1/auth/refresh`) through interceptor.
  2. If refresh success, retry login-history request.
  3. If refresh fails, clear local auth state and redirect login.

### UI/security notes
- Show badge/icon for `success=true/false`.
- Avoid exposing sensitive security internals; display only returned audit fields.

## 8. Acceptance Criteria
- API `GET /api/v1/users/me/login-history` requires JWT and follows response wrapper standard.
- Response contains `items`, `limit`, `offset`.
- Records are returned newest first.
- Both success and failed attempts are returned when available.
- Invalid pagination params return 400.
- Missing/invalid JWT returns 401.
- Empty logs return HTTP 200 with `items: []`.

## 9. Prompt for Stitch (UI only)
```text
Create a "Login History" screen for 2Hands account security.

API:
- GET /api/v1/users/me/login-history
- Query params: limit (default 20, 1..100), offset (default 0, >=0)
- Auth required (Bearer token)

Response data:
- items[] with fields:
  id, login_method, ip_address, user_agent, success, created_at
- limit, offset

Requirements:
- Show loading skeleton while fetching.
- Show paginated list with "Load more" behavior using limit/offset.
- Show empty state when no items.
- Show success/failure badge for each history entry.
- Handle 400 validation errors for pagination.
- Handle 401 with refresh-then-retry strategy; refresh fail -> redirect login.
- Show retry-friendly message for 500.
- Responsive and accessible layout.
```
