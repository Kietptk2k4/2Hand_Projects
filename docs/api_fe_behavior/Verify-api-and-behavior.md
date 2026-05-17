# Verify Email - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for `Verify Email` in Auth Service.

In scope:
- Accept verification token from user
- Validate token by hash, type, expiry, and usage status
- Activate user account
- Mark verification token as used
- Write outbox event after successful activation

Out of scope:
- Resend verification token
- Register/login/refresh/logout endpoints

## 2. Source Docs
- `docs/feature-requirements/auth/FR_Verify_Email.md`
- `docs/use-cases/uc-user-authentication.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/business-flow/authentication-lifecycle-flow.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. Current Code Alignment
- Register flow already creates `verification_tokens` with `type = EMAIL_VERIFY` and stores `token_hash`.
- Register flow already emits `EMAIL_VERIFICATION_REQUESTED` outbox event.
- Domain already has `VerificationToken` model and expiry/used rules.
- Current codebase does not yet expose `POST /api/v1/auth/verify-email` endpoint.

This spec is the target behavior that fits current architecture and existing data model.

## 4. API Contract
### Endpoint
- Method: `POST`
- Path: `/api/v1/auth/verify-email`
- Auth: Public

### Request Body
```json
{
  "token": "verify_token_or_otp"
}
```

### Validation
- `token`: required, non-empty

### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Xac thuc email thanh cong.",
  "data": {
    "user_id": "uuid-1234-5678",
    "email_verified": true,
    "status": "ACTIVE"
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

### Error Responses
- `400` invalid/expired/used token
- `500` internal error

Example 400:
```json
{
  "code": 400,
  "success": false,
  "message": "Token xac thuc khong hop le hoac da het han.",
  "data": null,
  "errors": [
    {
      "field": "token",
      "reason": "INVALID_OR_EXPIRED"
    }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 5. Backend Behavior (Authoritative)
### Main Flow
1. Validate request payload.
2. Hash input `token` using same strategy as register flow (BCrypt hash service used in project for verification token compare flow).
3. Find verification token record by:
- `type = EMAIL_VERIFY`
- token match strategy consistent with current hashing approach
4. Validate token business conditions:
- not used (`used_at is null`)
- not expired (`expires_at > now`)
5. Load user by `user_id` from token.
6. Validate user status:
- expected `PENDING_VERIFICATION`
7. In one transaction:
- mark token `used_at = now`
- update user:
  - `email_verified = true`
  - `status = ACTIVE`
  - `updated_at = now`
- create outbox event (`USER_CREATED` or `USER_UPDATED` per project convention)
8. Return 200 success.

### Idempotency Recommendation
- If user already ACTIVE and email_verified=true, may return 200 with safe message instead of hard failure.
- Keep behavior consistent across FE for better UX.

## 6. Database Impact
- `verification_tokens`:
- update `used_at`
- `users`:
- update `email_verified`, `status`, `updated_at`
- `outbox_events`:
- insert user activation event with `status = PENDING`

## 7. Transaction and Consistency
- Token update + user update + outbox insert must be in one ACID transaction.
- Any failure rolls back all writes.
- Prevent concurrent verify race:
- lock token row or use conditional update (`used_at IS NULL`) and verify affected rows.

## 8. Security Rules
- Never log raw token value.
- Keep generic 400 error for invalid/expired/used token.
- Apply rate limiting for verify endpoint to reduce brute-force attempts.
- Use TLS/HTTPS only.

## 9. FE Behavior (for Stitch/UI generation)
### Verify Screen
- Input: token/OTP
- CTA: `Verify email`
- Optional link: `Resend code`

### UX States
- Inline validation for empty/invalid token format
- Loading state on submit
- On 200:
- show success toast/message
- redirect to Login (or Home by product decision)
- On 400:
- show "Token khong hop le hoac da het han."
- keep user on verify screen
- On 500:
- show generic retry message

### UI Notes
- Keep flow simple and mobile-friendly
- Accessibility: labeled input, error text, keyboard submit

## 10. Acceptance Criteria
- Valid token + pending user -> user activated and token marked used.
- Invalid/expired/used token -> 400 and no user state change.
- Successful verify writes outbox event in same transaction.
- Sensitive token is never written to logs.

## 11. Prompt for Stitch (UI only)
```text
Generate a Verify Email screen for 2Hands:
- Token input + Verify button
- Optional Resend Code link
- Inline validation and loading state
- Success path: show success message and redirect to Login
- Error path: invalid/expired token message
- Responsive and accessible layout
```
