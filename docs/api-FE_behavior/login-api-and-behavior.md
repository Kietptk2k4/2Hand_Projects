# Login by Email - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for `Login by Email/Password` in Auth Service.

In scope:
- Validate email/password
- Authenticate user
- Issue access token + refresh token
- Create refresh session
- Update last login timestamp
- Write login logs (success/failure)

Out of scope:
- OAuth login
- Forgot/reset password
- 2FA

## 2. Source Docs
- `docs/feature-requirements/auth/FR_Login_Email.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/backend-convention.md`

## 3. API Contract
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
- `401` wrong email/password (use generic message)
- `403` account suspended
- `429` too many login attempts
- `500` internal error

Example 401:
```json
{
  "code": 401,
  "success": false,
  "message": "Email hoac mat khau khong chinh xac.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 4. Backend Behavior (Authoritative)
### Main Flow
1. Validate request.
2. Normalize email (`email_normalized`) and query `USERS`.
3. If user not found -> return 401 generic message.
4. Verify password hash.
5. If mismatch:
- insert `LOGIN_LOGS` with `success = false`
- return 401 generic message.
6. Check user status:
- `SUSPENDED` -> return 403.
- `DELETED` -> return 401 generic message (avoid enumeration).
- `PENDING_VERIFICATION` -> allow login; return tokens + `status = PENDING_VERIFICATION` for FE redirect to verify flow.
7. Generate access token (short TTL) and refresh token (long TTL).
8. Persist `REFRESH_TOKEN_SESSION` with:
- `token_hash`
- `status = ACTIVE`
- `device_id`, `ip_address`, `user_agent`
- `expires_at`
9. Update `USERS.last_login_at = now()`.
10. Insert `LOGIN_LOGS` with `success = true`.
11. Return 200 with token payload.

## 5. Database Impact
- `USERS`: update `last_login_at` on successful login.
- `LOGIN_LOGS`: insert record for both success and failure cases.
- `REFRESH_TOKEN_SESSION`: insert active session when login succeeds.

## 6. Security Rules
- Never log plaintext password or raw tokens.
- Store refresh token as hash (`token_hash`) in DB.
- Use generic 401 message to reduce account enumeration risk.
- Apply brute-force protection / rate limit by IP and/or email (e.g. 5 failed attempts per 15 minutes).
- Use HTTPS-only transport.

## 7. FE Behavior (for Stitch/UI generation)
### Form
- Fields: `email`, `password`
- Optional checkbox: `remember_me`
- Optional link: `forgot password`
- Optional password visibility toggle

### UX States
- Inline validation for email format and required password
- Disable submit while request is in-flight
- Show loading state on login button
- On 200:
- Store tokens securely (prefer HttpOnly cookie strategy if available)
- If `user.status = PENDING_VERIFICATION`: redirect to Verify Email flow
- Else redirect to Home or original `redirectUrl`
- On 401/403/429/500:
- Show clear error toast/message from API

## 8. Acceptance Criteria
- Correct email/password for active user -> 200 + tokens + login log success + last_login_at updated.
- Wrong credential -> 401 + login log failure.
- Suspended user -> 403.
- Login success creates `REFRESH_TOKEN_SESSION` with `ACTIVE` status.
- No sensitive data leak in logs.

## 9. Prompt for Stitch (UI only)
```text
Create a Login screen for 2Hands with:
- Email + password fields
- Password show/hide toggle
- Inline validation
- Disabled submit while loading
- Clear error states for 401/403/429/500
- On login success:
  - redirect to Verify Email if status is PENDING_VERIFICATION
  - otherwise redirect to Home
- Responsive and accessible layout
```
