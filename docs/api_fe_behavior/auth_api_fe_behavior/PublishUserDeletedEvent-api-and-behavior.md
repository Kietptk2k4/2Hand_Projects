# PublishUserDeletedEvent - API and Behavior Spec

## 1. Scope
This document defines backend integration behavior for publishing `USER_DELETED` in Auth Service.

In scope:
- Transactional outbox write for `USER_DELETED` when soft-delete succeeds
- Trigger point from account soft-delete endpoint
- Outbox payload/record contract and rollback behavior

Out of scope:
- Outbox worker publish/retry implementation details
- Consumer-side handling in downstream services
- Any direct frontend endpoint for outbox access

## 2. Source Docs
- `docs/feature-requirements/auth/FR_PublishUserDeletedEvent.md`
- `docs/feature-requirements/auth/FR_SoftDeleteAccount.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. Trigger Endpoint
`USER_DELETED` is produced by:
- `POST /api/v1/users/me/soft-delete`

Notes:
- Event creation is internal integration behavior.
- Endpoint response does not expose outbox payload.

## 4. Outbox Contract
For each `USER_DELETED` outbox record:
- `event_type`: `USER_DELETED`
- `source`: `auth-service`
- `status`: `PENDING`
- `retry_count`: `0`
- `payload` minimum fields:
  - `user_id`
  - `email`
  - `deleted_at` (ISO-8601 timestamp by current implementation)

Security:
- Payload must not include sensitive data (`password_hash`, raw tokens, secrets).

## 5. Backend Behavior Summary (Transactional Outbox)
- Use case validates auth context and password confirmation.
- On success, soft-delete flow performs in one ACID transaction:
  - update user to `DELETED` + set `deleted_at`,
  - revoke all active refresh sessions,
  - insert `USER_DELETED` into `OUTBOX_EVENTS`.
- If outbox payload serialization fails or outbox persistence fails, transaction rolls back fully.
- No direct dual-write from request flow to both DB and broker.

## 6. FE Behavior
- FE does not call outbox APIs and does not render raw outbox data.
- FE only calls `POST /api/v1/users/me/soft-delete` and handles its response.
- On success:
  - clear local auth/session state,
  - redirect user to login or account-deleted landing.
- On failure:
  - show message from endpoint error response (`400/401/409/500`).

## 7. Acceptance Criteria
- Soft-delete success creates one `USER_DELETED` outbox record.
- Created outbox record has `status = PENDING` and `retry_count = 0`.
- Event payload contains at least `user_id`, `email`, `deleted_at`.
- Soft-delete update + session revoke + outbox insert are atomic in one transaction.
- Wrong password fails without creating outbox event.
- Serialization/outbox persistence failure causes full rollback.

## 8. Prompt for Stitch (UI only)
```text
Create a Soft Delete Account confirmation flow for 2Hands:
- Show a Danger Zone section with delete-account action
- Require password confirmation before submit
- Show confirmation modal before final action
- Handle states:
  - loading while submitting
  - success: clear auth state and redirect to login/account-deleted page
  - failure: show clear messages for 400/401/409/500
- Do not display internal outbox/event data in UI
- Keep layout responsive and accessible
```
