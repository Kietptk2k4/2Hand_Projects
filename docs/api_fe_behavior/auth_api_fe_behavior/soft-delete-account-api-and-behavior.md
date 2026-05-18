# Soft Delete Account - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for soft delete account feature in Auth Service.

In scope:
- Confirm user password before account deletion.
- Soft delete current account (`status = DELETED`, set `deleted_at`).
- Revoke all ACTIVE refresh token sessions.
- Write outbox event `USER_DELETED` in the same transaction.

Out of scope:
- Hard delete physical records.
- Immediate synchronous deletion of avatar object in MinIO.
- Cross-service business precheck not implemented in current auth-service code.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_SoftDeleteAccount.md`
- `docs/use-cases/uc-user-profile-management.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/api_fe_behavior/ProfileAccount-api-and-behavior.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/database/auth_schema.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `POST`
- Path: `/api/v1/users/me/soft-delete`
- Auth: Required JWT (`Authorization: Bearer <access_token>`)

### 3.2 Request Body
```json
{
  "password": "CurrentPassword123!"
}
```

### 3.3 Validation Rules (BE Authoritative)
- `password`:
  - Required, non-blank.
  - Must match current user password hash.

### 3.4 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Xoa tai khoan thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 4. Error Handling
- `400 Bad Request`:
  - Missing/blank password.
  - Wrong password.
  - Invalid request payload (malformed JSON).
- `401 Unauthorized`:
  - Missing/invalid JWT.
  - Invalid principal format.
  - User not found.
- `409 Conflict`:
  - Account already in `DELETED` status.
- `500 Internal Server Error`:
  - Unexpected backend/runtime errors.

Example wrong password response:
```json
{
  "code": 400,
  "success": false,
  "message": "Mat khau khong chinh xac.",
  "data": null,
  "errors": [
    {
      "field": "password",
      "reason": "INVALID_CREDENTIAL"
    }
  ],
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Backend Behavior (Authoritative)

### 5.1 Main Flow
1. Extract current user id from JWT context.
2. Validate request payload (`password` required).
3. Load user from `USERS`.
4. If user is already `DELETED`, return `409`.
5. Verify `password` against stored `password_hash`.
6. Mark user as soft deleted at domain level.
7. In one transaction:
   - update user status to `DELETED` and set `deleted_at`,
   - revoke all ACTIVE refresh token sessions of user,
   - insert outbox event `USER_DELETED`.
8. Return HTTP 200.

### 5.2 Transaction Rule
- User status update + session revoke + outbox insert run in one ACID transaction.
- Any failure rolls back all write operations.

## 6. Database and Session Impact
- `USERS`:
  - Update: `status = DELETED`, `deleted_at`, `updated_at`.
- `REFRESH_TOKEN_SESSIONS`:
  - Update all rows where `user_id = currentUser` and `status = ACTIVE` to `REVOKED`.
- `OUTBOX_EVENTS`:
  - Insert event:
    - `event_type = USER_DELETED`
    - `status = PENDING`
    - payload includes `user_id`, `email`, `deleted_at`.

## 7. Post-Delete Behavioral Notes
- Deleted account is blocked from login flow by domain login rule.
- Existing active refresh sessions are invalidated after soft delete.
- Avatar object cleanup (if required) should be handled asynchronously by downstream worker/consumer.

## 8. FE Behavior

### 8.1 UX Flow
- Place this action in "Danger Zone".
- Require password input and explicit confirmation dialog before submit.
- Call `POST /api/v1/users/me/soft-delete` after confirmation.

### 8.2 UX Rules
- Disable submit while request is pending.
- On success (`200`):
  - clear auth state/cache immediately,
  - redirect user to login or account-deleted landing screen.
- On `400`:
  - show inline password error if available in `errors[]`,
  - keep user on same form.
- On `401`:
  - treat as unauthenticated session and redirect login.
- On `409`:
  - show account already deleted state and force logout locally.
- On `500`:
  - show generic retry message.

## 9. Security Notes
- Endpoint is protected under `/api/v1/users/me/**`.
- Ownership is implicit via JWT principal (current user only).
- Never log plaintext password or tokens.
- Use HTTPS/TLS for transport.

## 10. Acceptance Criteria
- Valid password soft-deletes account and returns `200`.
- All ACTIVE refresh sessions are revoked in same transaction.
- Outbox event `USER_DELETED` is created.
- Wrong password returns `400` with `field=password`.
- Already deleted account returns `409`.
- Unauthorized request returns `401`.
- API response follows standard envelope `code/success/message/data/errors/timestamp`.

## 11. Prompt for Stitch (UI only)
```text
Create a Delete Account (Soft Delete) UI for 2Hands.

API:
- POST /api/v1/users/me/soft-delete
- Auth: Bearer token
- Request: { "password": "..." }

Requirements:
- Danger zone section
- Password confirmation field
- Confirmation modal before final submit
- Loading state while submitting
- Handle:
  - 400 wrong/missing password
  - 401 unauthorized -> redirect login
  - 409 already deleted
  - 500 retry
- On success: clear local session and redirect to login/account-deleted page
- Responsive and accessible design
```
