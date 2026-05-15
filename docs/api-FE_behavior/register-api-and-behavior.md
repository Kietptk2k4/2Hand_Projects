# Register by Email - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for `Register by Email` in Auth Service.

In scope:
- Register with email/password
- Create default profile/settings
- Create verification token
- Write outbox event for email verification

Out of scope:
- Resend OTP
- Verify OTP
- OAuth register

## 2. Source Docs
- `docs/feature-requirements/auth/FR_Register_Email.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/backend-convention.md`

## 3. API Contract
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

Example:
```json
{
  "code": 409,
  "success": false,
  "message": "Email da duoc su dung.",
  "data": null,
  "errors": [
    {
      "field": "email",
      "reason": "DUPLICATE"
    }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 4. Backend Behavior (Authoritative)
### Main Flow
1. Validate request.
2. Normalize email (`email_normalized`).
3. Check duplicate email.
4. Hash password (bcrypt/argon2), never store plaintext.
5. Open one DB transaction.
6. Insert into `USERS`:
- `status = PENDING_VERIFICATION`
- `email_verified = false`
7. Insert default `USER_PROFILES`.
8. Insert default `USER_SETTINGS` (`appearance_mode = SYSTEM`).
9. Insert `VERIFICATION_TOKENS` with `type = EMAIL_VERIFY` and `expires_at`.
10. Insert `OUTBOX_EVENTS` with `event_type = EMAIL_VERIFICATION_REQUESTED`, `status = PENDING`.
11. Commit transaction.
12. Return `201`.

### Consistency Rules
- Steps 6-10 must be in the same transaction.
- Any failure -> rollback all writes.
- DB unique constraint on `email_normalized` must be treated as `409`.

### Security Rules
- Apply rate limit per IP (example: 5 requests/hour/IP).
- Never log password/token/secret.
- Use parameterized queries / ORM to prevent SQL injection.

## 5. FE Behavior (for Stitch/UI generation)
### Form
- Fields: `email`, `password`, `confirm_password`
- Register button disabled until form valid

### UX States
- Inline validation on blur/type
- Submit loading state on button
- Success (201): show success toast + navigate to `Verify Email / OTP` screen
- Error:
- 400: show field-level errors
- 409: show "Email da duoc su dung."
- 429: show "Ban thao tac qua nhanh, vui long thu lai sau."
- 500: show generic retry message

### UI Hints
- Show password rule helper text
- Accessible labels and error messages
- Responsive mobile + desktop layout

## 6. Acceptance Criteria
- Register success creates `USERS`, `USER_PROFILES`, `USER_SETTINGS`, `VERIFICATION_TOKENS`, `OUTBOX_EVENTS`.
- New user status is `PENDING_VERIFICATION`.
- Duplicate email returns `409` and does not create partial data.
- FE handles loading/success/error paths correctly.

## 7. Prompt for Stitch (UI only)
```text
Create a Register screen for 2Hands based on this behavior:
- Fields: email, password, confirm password
- Inline validation
- Disabled submit until valid
- Loading state on submit
- On 201: success message and navigate to Verify Email/OTP
- On 400/409/429/500: show clear error messages
- Responsive and accessible UI
```
