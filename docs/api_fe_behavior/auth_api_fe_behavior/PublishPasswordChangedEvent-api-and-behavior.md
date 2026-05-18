# PublishPasswordChangedEvent - API and Behavior Spec

## 1. Scope
This document defines backend integration behavior for publishing `PASSWORD_CHANGED` in Auth Service.

In scope:
- Transactional outbox write for `PASSWORD_CHANGED` when password change succeeds
- Trigger point from change-password endpoint
- Outbox payload/record contract and rollback behavior

Out of scope:
- Outbox worker publish/retry implementation details
- Consumer-side handling in downstream services
- Any direct frontend endpoint for outbox records

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ChangePassword.md`
- `docs/feature-requirements/auth/FR_PublishPasswordChangedEvent.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. Trigger Endpoint
`PASSWORD_CHANGED` is produced by:
- `POST /api/v1/auth/change-password`

Notes:
- Event creation is internal integration behavior.
- Endpoint response does not expose outbox payload.

## 4. Outbox Contract
For each `PASSWORD_CHANGED` outbox record:
- `event_type`: `PASSWORD_CHANGED`
- `source`: `auth-service`
- `status`: `PENDING`
- `retry_count`: `0`
- `payload` minimum fields:
  - `user_id`
  - `email`
  - `changed_at` (ISO-8601 timestamp by current implementation)

Security:
- Payload must not include sensitive data (`password_hash`, plaintext password, raw tokens, secrets).

## 5. Backend Behavior Summary (Transactional Outbox)
- Use case validates auth context and request payload.
- Current password must be valid before update.
- On success, change-password flow performs in one ACID transaction:
  - update password hash and `password_changed_at`,
  - revoke all active refresh sessions,
  - insert `PASSWORD_CHANGED` into `OUTBOX_EVENTS`.
- If outbox payload serialization fails or outbox persistence fails, transaction rolls back fully.
- No direct dual-write from request flow to both DB and broker.

## 6. FE Behavior
- FE does not call outbox APIs and does not render raw outbox data.
- FE only calls `POST /api/v1/auth/change-password` and handles its response.
- On success:
  - show success feedback,
  - enforce re-login expectation by clearing local auth/session and navigating to login.
- On failure:
  - show message based on endpoint response (`400/401/500`).

## 7. Acceptance Criteria
- Change-password success creates one `PASSWORD_CHANGED` outbox record.
- Created outbox record has `status = PENDING` and `retry_count = 0`.
- Event payload contains at least `user_id`, `email`, `changed_at`.
- Password update + session revoke + outbox insert are atomic in one transaction.
- Wrong current password fails without creating outbox event.
- Serialization/outbox persistence failure causes full rollback.

## 8. Prompt for Stitch (UI only)
```text
Create a Change Password flow for 2Hands:
- Form fields: current password, new password, confirm new password
- Show inline validation and password requirement hints
- Handle states:
  - loading while submitting
  - success: show confirmation, clear local auth state, redirect to login
  - failure: show clear messages for 400/401/500
- Explain re-login expectation after password change
- Do not display internal outbox/event data in UI
- Keep layout responsive and accessible
```
