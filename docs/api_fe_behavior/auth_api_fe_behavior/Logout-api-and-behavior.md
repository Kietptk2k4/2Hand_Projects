# Logout - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for `Logout` in Auth Service.

In scope:
- Receive refresh token from client
- Invalidate current refresh token session
- Return idempotent success response

Out of scope:
- Logout all sessions
- Admin revoke flow
- Login/refresh implementation

## 2. Source Docs
- `docs/feature-requirements/auth/FR_Logout_Email.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/business-flow/authentication-lifecycle-flow.md`
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. API Contract
### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/logout`
- Auth: Public endpoint with token-based logout request body

### Request Body
```json
{
  "refresh_token": "rft_abc123..."
}
```

### Validation
- `refresh_token`: required, non-empty

### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Dang xuat thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

### Error Responses
- `400` invalid payload
- `500` internal error

Note:
- If token is already logged out/revoked/not found, API still returns `200` (idempotent behavior).

## 4. Backend Behavior (Authoritative)
### Main Flow
1. Validate request.
2. Hash `refresh_token` with sha256.
3. Find refresh session by `token_hash`.
4. If session exists and status is `ACTIVE`:
- update status to `LOGGED_OUT`
- update `updated_at`
5. If session does not exist or already invalid:
- do not throw business error
- continue return success
6. Return 200.

## 5. Session Rules
- Logout only invalidates current session.
- Session states considered already invalid: `LOGGED_OUT`, `REVOKED`, `EXPIRED`.
- API is idempotent to improve UX and avoid logout race issues.

## 6. Database Impact
- Table: `refresh_token_sessions`
- Update target session:
- `status = LOGGED_OUT`
- `updated_at = now()`

No outbox event required for current project logic.

## 7. Security Rules
- Never log raw refresh token.
- Use token hash only in lookup/update flow.
- Use HTTPS/TLS.
- Optional: add rate limit for logout endpoint.

## 8. FE Behavior (for Stitch/UI generation)
### Trigger
- User clicks `Logout` from profile/menu/settings.

### UX Flow
1. Call `POST /api/v1/auth/logout` with current refresh token.
2. Regardless of server 200 from valid/already-invalid token:
- clear access token and refresh token in client storage
- clear in-memory auth/user state
- redirect to login screen
3. If server returns 500:
- FE still clears local tokens for safety
- show brief message: "Da dang xuat tren thiet bi nay. Neu can, vui long thu lai."

### UI Notes
- Usually no dedicated logout screen.
- Optional confirmation modal: "Ban co chac chan muon dang xuat?"

## 9. Acceptance Criteria
- Active session logout sets status to `LOGGED_OUT` and returns 200.
- Repeated logout with same token still returns 200.
- Missing/invalid payload returns 400.
- No sensitive token leaks in logs.

## 10. Prompt for Stitch (UI only)
```text
Generate logout interaction UI behavior for 2Hands:
- Add logout action in account menu
- Optional confirmation modal before logout
- On confirm: call logout API and clear local auth state
- Redirect to Login screen after logout
- Handle fallback message if server error occurs
- Responsive and accessible UI
```
