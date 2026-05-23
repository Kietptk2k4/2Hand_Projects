# Apply User Enforcement - API and Behavior Spec

## 1. Scope
Central apply logic in Auth Service for admin enforcement decisions (suspend, ban, restrict, revoke, expire).

In scope:
- Shared `ApplyUserEnforcementUseCase` with idempotent tracking via `user_enforcement_snapshots`.
- Sync facades: suspend, ban, restrict, revoke admin APIs.
- Event consumer use case (`ConsumeUserEnforcementEventUseCase`) for broker integration.

Out of scope:
- Owning `user_enforcements` master records (Admin Service).
- Kafka wiring (use case ready; broker adapter can call consumer later).

## 2. Source Docs
- `docs/feature_requirements/auth/FR_ApplyUserEnforcement.md`
- `FR_SuspendUserByAdmin.md`, ban/restrict/revoke FRs

## 3. Action Mapping

| Action | User status | Sessions | Snapshot |
|--------|-------------|----------|----------|
| SUSPEND / BAN | SUSPENDED | Revoke all ACTIVE | APPLIED |
| RESTRICT | ACTIVE (unchanged) | No revoke | APPLIED |
| REVOKE / EXPIRE | ACTIVE if no other blocking enforcement | No revoke | REVOKED / EXPIRED |

## 4. Idempotency
- Same `enforcement_id` for apply actions → replay revokes sessions (suspend/ban) without duplicate snapshot.
- Same `event_id` → skip duplicate event processing.

## 5. Sync APIs (facades)
- `POST /api/v1/admin/users/{userId}/suspend`
- `POST /api/v1/admin/users/{userId}/ban`
- `POST /api/v1/admin/users/{userId}/restrict`
- `POST /api/v1/admin/user-enforcements/{enforcementId}/revoke`

All delegate to `ApplyUserEnforcementUseCase` after permission checks.

## 6. Event Consumer
`ConsumeUserEnforcementEventUseCase` accepts payload fields:
`event_id`, `event_type`, `enforcement_id`, `user_id`, `action_type`, `reason_code`, `description`, `expires_at`.

Maps `USER_SUSPENDED`, `USER_BANNED`, `USER_RESTRICTED`, `USER_ENFORCEMENT_REVOKED`, `USER_ENFORCEMENT_EXPIRED`.

Missing user on event path → log and skip (no 404).

## 7. Database
- `user_enforcement_snapshots` (Flyway `V2__user_enforcement_snapshots.sql`)
- Updates `users`, `refresh_token_sessions` per action rules

## 8. Acceptance Criteria
- Suspend/ban revokes sessions and sets SUSPENDED.
- Revoke reactivates user when no APPLIED SUSPEND/BAN remains.
- Idempotent replay is safe.
- Sync API and event apply share the same core use case.
