# Suspend User By Admin - API and Behavior Spec (Auth Service)

## 1. Scope
Internal sync API for Admin Service to suspend a user account: set `SUSPENDED` and revoke all active refresh sessions.

Facade over `ApplyUserEnforcementUseCase` with action `SUSPEND`.

## 2. Source Docs
- `docs/feature_requirements/auth/FR_SuspendUserByAdmin.md`
- `docs/feature_requirements/auth/FR_ApplyUserEnforcement.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `POST`
- Path: `/api/v1/admin/users/{userId}/suspend`
- Auth: Bearer JWT (`USER_SUSPEND`, or role `ADMIN` / `SUPER_ADMIN`)

### 3.2 Request Body
```json
{
  "enforcement_id": "uuid-from-admin-service",
  "reason_code": "ABUSE_SPAM",
  "description": "Spam reviews and fake orders",
  "expires_at": "2026-06-01T00:00:00Z"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `enforcement_id` | Yes | Idempotency key (Auth snapshot table) |
| `reason_code` | Yes | Non-blank |
| `description` | Yes | Non-blank |
| `expires_at` | No | Must be future instant if provided |

`userId` comes from path only, not body.

### 3.3 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Suspend user thanh cong.",
  "data": {
    "user_id": "uuid",
    "status": "SUSPENDED",
    "revoked_session_count": 3
  },
  "errors": null,
  "timestamp": "..."
}
```

## 4. Error Handling
- `400`: validation (missing fields, invalid `expires_at`)
- `401`: missing/invalid JWT
- `403`: missing `USER_SUSPEND` (and not admin role)
- `404`: user not found or `DELETED`

## 5. Backend Behavior
1. Authorize actor.
2. Delegate to `ApplyUserEnforcementUseCase` (`SUSPEND`).
3. Set user `SUSPENDED` if not already.
4. Revoke all `ACTIVE` refresh token sessions (also on idempotent replay).
5. Persist `user_enforcement_snapshots` with `APPLIED`.

## 6. Orchestration
Admin Service: create enforcement → outbox `USER_SUSPENDED` → call this Auth endpoint.

## 7. Acceptance Criteria
- Suspended user cannot log in (existing login checks on `SUSPENDED`).
- All active sessions revoked; count returned in response.
- Idempotent for same `enforcement_id`.
- 403 / 404 / 400 per spec.
