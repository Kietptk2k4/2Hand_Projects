# Register - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for Register screen in Auth Service, including:
- Register by email/password
- OAuth login/register entry (Google/Facebook) from the same screen

In scope:
- Register with email/password
- Create default profile/settings
- Create verification token
- Write outbox event for email verification
- OAuth start redirect endpoints and FE behavior

Out of scope:
- Resend OTP
- Verify OTP
- OAuth callback internals (handled by Spring Security)

## 2. Source Docs
- `docs/feature-requirements/auth/FR_Register_Email.md`
- `docs/feature-requirements/auth/FR_Login_OAuth.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/backend-convention.md`

## 3. API Contract (Email Register)
### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/register`
- Auth: Public

### Request Body
```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "confirm_password": "Password123!"
}
```

### Validation
- `email`: required, valid format, max 255 chars, normalized lowercase
- `password`: required, 8-32 chars, includes at least 1 uppercase, 1 lowercase, 1 number
- `confirm_password`: must match `password`

### Success Response (201)
```json
{
  "code": 201,
  "success": true,
  "message": "Dang ky thanh cong. Vui long kiem tra email de xac thuc.",
  "data": {
    "user_id": "uuid-1234-5678",
    "email": "user@example.com",
    "status": "PENDING_VERIFICATION"
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

### Error Responses
- `400` invalid input
- `409` duplicate email
- `429` too many attempts
- `500` internal error

## 4. API Contract (OAuth Entry)
### OAuth Start Endpoints
- `GET /oauth2/authorization/google`
- `GET /oauth2/authorization/facebook`

### OAuth Callback Endpoints (internal)
- `GET /login/oauth2/code/google`
- `GET /login/oauth2/code/facebook`

Notes:
- FE only redirects browser to OAuth start endpoints.
- Callback endpoints are handled by Spring Security.
- On OAuth success/failure, backend redirects back to frontend route per auth config.

## 5. Backend Behavior (Authoritative)
### Email Register Flow
1. Validate request.
2. Normalize email (`email_normalized`).
3. Check duplicate email.
4. Hash password (bcrypt/argon2), never store plaintext.
5. Open one DB transaction.
6. Insert `USERS` with `status = PENDING_VERIFICATION`, `email_verified = false`.
7. Insert default `USER_PROFILES`.
8. Insert default `USER_SETTINGS` (`appearance_mode = SYSTEM`).
9. Insert `VERIFICATION_TOKENS` (`type = EMAIL_VERIFY`, `expires_at`).
10. Insert `OUTBOX_EVENTS` (`event_type = EMAIL_VERIFICATION_REQUESTED`, `status = PENDING`).
11. Commit transaction.
12. Return `201`.

### OAuth Entry Behavior
- Clicking OAuth endpoints initiates provider auth flow.
- On provider callback:
- if user email not exists: create ACTIVE + email_verified=true user
- if user exists: login that account (no duplicate)
- issue access/refresh token and create login/session logs
- redirect to frontend success route

## 6. FE Behavior (for Stitch/UI generation)
### Register Form
- Fields: `email`, `password`, `confirm_password`
- Main CTA: `Dang ky`
- Social CTAs:
- `Continue with Google`
- `Continue with Facebook`

### UX States
- Inline validation on blur/type
- Disable register button until form valid
- Show loading state while submitting register
- Register success (201): navigate to Verify Email / OTP
- Register errors:
- 400 field errors
- 409 duplicate email
- 429 too many attempts
- 500 generic error

### OAuth Button Behavior
- On click Google: `window.location.href = "http://localhost:3001/oauth2/authorization/google"`
- On click Facebook: `window.location.href = "http://localhost:3001/oauth2/authorization/facebook"`
- Show loading overlay while redirecting
- On backend OAuth failure redirect, show retry toast

## 7. Acceptance Criteria
- Register success creates USERS/PROFILES/SETTINGS/VERIFICATION_TOKENS/OUTBOX_EVENTS.
- Duplicate email returns 409 without partial writes.
- Register screen includes Google and Facebook OAuth buttons.
- OAuth button click starts provider flow through auth-service endpoints.

## 8. Prompt for Stitch (UI only)
```text
Create a Register screen for 2Hands:
- Email, password, confirm password fields
- Inline validation and loading states
- Register button disabled until valid
- Add two social buttons: Continue with Google, Continue with Facebook
- On Google/Facebook click, redirect browser to auth-service OAuth start endpoints
- On register success (201), navigate to Verify Email/OTP
- Show clear error messages for 400/409/429/500
- Responsive and accessible layout
```
