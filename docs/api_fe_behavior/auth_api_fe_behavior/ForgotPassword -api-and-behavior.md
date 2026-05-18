# ForgotPassword - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for `ForgotPassword` in Auth Service.

In scope:
- Accept forgot-password request by email
- Apply anti-enumeration response policy
- Create password reset token
- Write outbox event for email notification

Out of scope:
- Reset password endpoint (token + new password)
- Login/logout/refresh logic

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ForgotPassword.md`
- `docs/use-cases/uc-password-recovery.md`
- `docs/business-flow/password-recovery-flow.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. API Contract
### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/forgot-password`
- Auth: Public

### Request Body
```json
{
  "email": "user@example.com"
}
```

### Validation
- `email`: required, valid format, max length 255

### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Neu email hop le, chung toi da gui huong dan dat lai mat khau.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

### Error Responses
- `400` invalid payload
- `429` too many attempts
- `500` internal error

Note:
- For anti-enumeration, when email does not exist, API still returns the same `200` success message.

## 4. Backend Behavior (Authoritative)
### Main Flow
1. Validate request payload.
2. Normalize email (`email_normalized`).
3. Lookup user by normalized email.
4. If user exists:
- generate reset token/OTP
- store hashed token into `verification_tokens` with:
  - `type = PASSWORD_RESET`
  - `expires_at`
  - `used_at = null`
- insert outbox event `PASSWORD_RESET_REQUESTED` with `status = PENDING`
5. If user does not exist:
- skip token and outbox write
6. Return 200 generic success message in both cases.

## 5. Database Impact
- If user exists:
- insert into `verification_tokens`
- insert into `outbox_events`

Transaction rule:
- Token insert + outbox insert must be in one transaction.

## 6. Security Rules
- Never log plaintext token/password.
- Prefer storing `token_hash` only.
- Apply rate limit by IP/email for abuse prevention.
- Use HTTPS/TLS only.
- Keep response generic to prevent account enumeration.

## 7. FE Behavior (for Stitch/UI generation)
### Form
- Single field: `email`
- Submit button: `Send reset link`

### UX States
- Inline email validation
- Disable submit while loading
- On 200:
- show neutral success message:
  - "If your email is valid, we sent reset instructions."
- optionally navigate to `Check your email` screen
- On 400:
- show field validation error
- On 429:
- show "Ban thao tac qua nhanh, vui long thu lai sau."
- On 500:
- show generic retry message

### UI Notes
- Keep wording privacy-safe (do not reveal account existence)
- Provide link back to Login
- Responsive and accessible design

## 8. Acceptance Criteria
- Valid existing email creates `verification_tokens` + `outbox_events` and returns 200.
- Non-existing email still returns the same 200 message.
- Invalid email format returns 400.
- Sensitive token values are not logged.

## 9. Prompt for Stitch (UI only)
```text
Generate Forgot Password UI for 2Hands:
- Email input form with inline validation
- Submit button with loading state
- Privacy-safe success message (do not reveal if email exists)
- Error handling for 400/429/500
- Link back to Login
- Responsive and accessible layout
```
