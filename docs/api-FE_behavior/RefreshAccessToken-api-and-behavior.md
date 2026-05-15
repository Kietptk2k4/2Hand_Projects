# RefreshAccessToken - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for `RefreshAccessToken` in Auth Service.

In scope:
- Validate refresh token
- Check refresh token session state
- Issue new access token
- Return 401 when session invalid/expired/revoked

Out of scope:
- Login endpoint
- Logout endpoint
- Forgot/reset password

## 2. Source Docs
- `docs/feature-requirements/auth/FR_RefreshAccessToken_Email.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/business-flow/authentication-lifecycle-flow.md`
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. API Contract
### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/refresh`
- Auth: Public (token-based validation in request body)

### Request Body
```json
{
  "refresh_token": "rft_abc123..."
}
```

### Validation
- `refresh_token`: required, non-empty, valid format

### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lam moi access token thanh cong.",
  "data": {
    "access_token": "eyJhbGciOi...",
    "expires_in": 1800
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

### Error Responses
- `400` invalid payload
- `401` invalid/expired/revoked/logged_out refresh session
- `500` internal error

Example 401:
```json
{
  "code": 401,
  "success": false,
  "message": "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 4. Backend Behavior (Authoritative)
### Main Flow
1. Validate request.
2. Hash `refresh_token` (sha256).
3. Find `REFRESH_TOKEN_SESSION` by `token_hash`.
4. Validate session:
- exists
- `status = ACTIVE`
- `expires_at > now()`
5. Load user and validate status:
- deny if `SUSPENDED` or `DELETED`
6. Issue new access token (short TTL).
7. Return 200.

## 5. Session Rules
- If session status is `REVOKED`, `LOGGED_OUT`, or `EXPIRED` => return 401.
- If refresh token not found => return 401.
- If refresh session expired by time => return 401.
- Optional hardening:
- rotate refresh token and update session atomically
- detect refresh token reuse and revoke all sessions

## 6. Database Impact
- Read `refresh_token_sessions` by token hash.
- Read `users` status.
- Base flow: no mandatory write.

Optional write path (if rotate enabled):
- revoke old session
- create new active refresh session

## 7. Security Rules
- Never log raw refresh token.
- Store only `token_hash`, never plaintext token.
- Use HTTPS/TLS only.
- Add rate limit for refresh endpoint.

## 8. FE Behavior (for Stitch/UI generation)
### Technical UX
- FE triggers refresh silently when:
- access token expired
- or request receives 401 due to expired access token
- On refresh success:
- replace access token in memory/storage
- retry original request once
- On refresh 401:
- clear auth state
- redirect user to login
- show message: "Phien dang nhap da het han, vui long dang nhap lai."

### UI Notes
- Usually no dedicated user-facing screen for refresh.
- If needed for demo/test, provide minimal "Session Expired" fallback modal/page.

## 9. Acceptance Criteria
- Valid active refresh session returns new access token.
- Invalid/expired/revoked refresh session returns 401.
- Suspended/deleted user cannot refresh token.
- No sensitive token value appears in logs.

## 10. Prompt for Stitch (UI only)
```text
Generate auth session handling UI behavior for 2Hands:
- Silent refresh flow (no visible screen by default)
- If refresh fails with 401: clear session and redirect to Login
- Show user-friendly message: "Session expired, please login again"
- Include optional fallback modal/page design for session expiration
- Ensure responsive and accessible UI
```
