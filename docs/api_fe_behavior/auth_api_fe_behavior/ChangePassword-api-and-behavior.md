# ChangePassword - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for `ChangePassword` in Auth Service.

In scope:
- Validate current/new password payload
- Verify current password
- Update password hash and password_changed_at
- Revoke all active refresh sessions
- Write outbox event PASSWORD_CHANGED

Out of scope:
- Forgot/Reset password flow
- Login/logout endpoint details

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ChangePassword.md`
- `docs/use-cases/uc-password-recovery.md`
- `docs/business-flow/password-recovery-flow.md`
- `docs/business-flow/session-management-flow.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. API Contract
### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/change-password`
- Auth: Required (JWT)

### Request Body
```json
{
  "current_password": "OldPassword123!",
  "new_password": "NewPassword456!",
  "confirm_new_password": "NewPassword456!"
}
```

### Validation
- `current_password`: required, non-empty
- `new_password`: required, 8-32 chars, at least 1 uppercase + 1 lowercase + 1 number
- `confirm_new_password`: must equal `new_password`
- `new_password` must be different from `current_password`

### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Doi mat khau thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

### Error Responses
- `400` invalid payload / wrong current password / weak new password
- `401` missing/invalid JWT
- `500` internal error

## 4. Backend Behavior (Authoritative)
### Main Flow
1. Authenticate user from JWT context.
2. Validate request payload.
3. Load current user record.
4. Verify `current_password` against stored `password_hash`.
5. Ensure `new_password` != `current_password`.
6. Hash `new_password`.
7. In one transaction:
- update `users.password_hash`
- update `users.password_changed_at = now()`
- update all `refresh_token_sessions` for user where `status = ACTIVE` -> `REVOKED`
- insert `outbox_events` with `event_type = PASSWORD_CHANGED`, `status = PENDING`
8. Return 200.

## 5. Database Impact
- `users`:
- update `password_hash`, `password_changed_at`, `updated_at`
- `refresh_token_sessions`:
- set all ACTIVE sessions to REVOKED
- `outbox_events`:
- insert PASSWORD_CHANGED event

## 6. Transaction Rules
- User update + session revoke + outbox insert must be in one ACID transaction.
- Any failure should rollback all writes.

## 7. Security Rules
- Never log plaintext password or tokens.
- Password must be hashed (BCrypt/Argon2).
- Force session invalidation after password change.
- HTTPS/TLS required.
- Optional: rate limit endpoint to reduce abuse.

## 8. FE Behavior (for Stitch/UI generation)
### Form
- `current_password`
- `new_password`
- `confirm_new_password`
- show/hide password toggles

### UX States
- Inline validation for strength + confirm mismatch
- Disable submit while loading
- On 200:
- show success message
- force logout UX: clear local auth and redirect to Login
- On 400:
- show field-level or global error
- On 401:
- redirect to Login
- On 500:
- show generic retry message

### UI Notes
- Display password requirements helper text
- Emphasize security message: user will be logged out on other devices
- Responsive and accessible layout

## 9. Acceptance Criteria
- Correct current password + valid new password -> 200 and password updated.
- All ACTIVE refresh sessions are REVOKED after success.
- Outbox event PASSWORD_CHANGED is created.
- Wrong current password -> 400 and no DB updates.
- No sensitive values appear in logs.

## 10. Prompt for Stitch (UI only)
```text
Generate Change Password UI for 2Hands:
- Fields: current password, new password, confirm new password
- Password visibility toggle
- Inline strength + confirm validation
- Loading state on submit
- Success: show confirmation and redirect to Login
- Error handling for 400/401/500
- Responsive and accessible design
```
