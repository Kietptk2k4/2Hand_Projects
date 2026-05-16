# Login - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for Login in Auth Service, including:
- Login by email/password
- Login by OAuth (Google/Facebook)

## 2. Source Docs
- `docs/feature-requirements/auth/FR_Login_Email.md`
- `docs/feature-requirements/auth/FR_Login_OAuth.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/use-cases/uc-oauth-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/backend-convention.md`

## 3. API Contract (Email Login)
### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/login`
- Auth: Public

### Request Body
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

### Validation
- `email`: required, valid email format, normalize lowercase for lookup
- `password`: required, non-empty

### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Dang nhap thanh cong.",
  "data": {
    "access_token": "eyJhbGciOi...",
    "refresh_token": "rft_abc123...",
    "expires_in": 1800,
    "user": {
      "id": "uuid-123",
      "email": "user@example.com",
      "status": "ACTIVE"
    }
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

### Error Responses
- `400` invalid request format
- `401` wrong email/password (generic message)
- `403` account suspended
- `429` too many login attempts
- `500` internal error

## 4. API Contract (OAuth Login)
### OAuth Start Endpoints
- `GET /oauth2/authorization/google`
- `GET /oauth2/authorization/facebook`

### OAuth Callback Endpoints (internal)
- `GET /login/oauth2/code/google`
- `GET /login/oauth2/code/facebook`

Notes:
- FE redirects browser to start endpoint.
- Callback is handled by Spring Security OAuth2 Client.
- Backend issues internal JWT access/refresh token then redirects to frontend success route.

## 5. Backend Behavior (Authoritative)
### Email Login Flow
1. Validate request.
2. Normalize email and query user.
3. If user not found -> 401 generic.
4. Verify password hash.
5. If mismatch:
- write `LOGIN_LOGS` failure
- return 401 generic.
6. Check status:
- `SUSPENDED` -> 403
- `DELETED` -> 401 generic
- `PENDING_VERIFICATION` -> allow login and return status for FE redirect
7. Issue access + refresh token.
8. Save `REFRESH_TOKEN_SESSION` ACTIVE.
9. Update `USERS.last_login_at`.
10. Save `LOGIN_LOGS` success.
11. Return 200.

### OAuth Login Flow
1. FE redirects to `/oauth2/authorization/{provider}`.
2. Provider authenticates and redirects to `/login/oauth2/code/{provider}`.
3. Backend reads provider profile (email, name, avatar).
4. If email missing -> fail (400/401 by handler strategy).
5. If user not exists:
- create `USERS` with `status=ACTIVE`, `email_verified=true`
- create `USER_PROFILES`, `USER_SETTINGS`
- write outbox `USER_CREATED`
6. If user exists:
- map and login existing account, no duplicate create
7. If status `SUSPENDED` or `DELETED` -> deny (403).
8. Issue access + refresh token, save session/logins, redirect FE success route.

## 6. Database Impact
- Email login:
- `USERS.last_login_at`
- `LOGIN_LOGS`
- `REFRESH_TOKEN_SESSION`
- OAuth login:
- all above plus conditional create:
- `USERS`, `USER_PROFILES`, `USER_SETTINGS`, `OUTBOX_EVENTS (USER_CREATED)`

## 7. Security Rules
- Never log plaintext password/raw tokens.
- Use generic 401 for invalid credentials.
- Apply brute-force protection/rate limit.
- OAuth flow uses state validation and CSRF protection by Spring Security.
- HTTPS required.

## 8. FE Behavior (for Stitch/UI generation)
### Login Form
- Fields: `email`, `password`
- Optional: remember_me, forgot-password link
- Social buttons:
- `Continue with Google`
- `Continue with Facebook`

### UX States
- Inline validation for email/password
- Disable submit while loading
- On email login success:
- if `status = PENDING_VERIFICATION` -> redirect Verify Email
- else -> redirect Home/original redirectUrl
- On email login errors 401/403/429/500 -> show clear message

### OAuth UX
- On Google click:
- `window.location.href = "http://localhost:3001/oauth2/authorization/google"`
- On Facebook click:
- `window.location.href = "http://localhost:3001/oauth2/authorization/facebook"`
- Show loading overlay during redirect
- On oauth success callback route: finalize session and route user
- On oauth failure callback route: show retry toast

## 9. Acceptance Criteria
- Email login works with correct state handling and logging.
- Login screen has Google/Facebook buttons.
- OAuth can create new account if email not exists.
- OAuth does not create duplicate account if email already exists.
- Suspended/deleted users are blocked in both email and OAuth login.

## 10. Prompt for Stitch (UI only)
```text
Create a Login screen for 2Hands:
- Email + password fields with show/hide password
- Inline validation and loading states
- Add social buttons: Continue with Google, Continue with Facebook
- On social click, redirect browser to auth-service OAuth start endpoints
- Handle email login success:
  - PENDING_VERIFICATION -> Verify Email screen
  - ACTIVE -> Home
- Handle 401/403/429/500 errors with clear messaging
- Responsive and accessible layout
```
