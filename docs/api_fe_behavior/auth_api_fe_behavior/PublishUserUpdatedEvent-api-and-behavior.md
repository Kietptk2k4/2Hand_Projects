# PublishUserUpdatedEvent - API and Behavior Spec

## 1. Scope
This document defines backend integration behavior for publishing `USER_UPDATED` in Auth Service.

In scope:
- Transactional outbox write for `USER_UPDATED` after user account update flows
- Trigger points from profile/avatar/privacy endpoints
- Outbox payload and record contract for integration consistency

Out of scope:
- Outbox worker publish/retry implementation details
- Consumer-side processing in downstream services
- Any direct FE API for outbox records

## 2. Source Docs
- `docs/feature-requirements/auth/FR_PublishUserUpdatedEvent.md`
- `docs/feature-requirements/auth/FR_UpdateProfile.md`
- `docs/feature-requirements/auth/FR_UpdateAvatar.md`
- `docs/feature-requirements/auth/FR_TogglePrivateProfile.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. Trigger Endpoints
`USER_UPDATED` is produced after successful business update in:
- `PUT /api/v1/users/me/profile`
- `PATCH /api/v1/users/me/avatar`
- `PATCH /api/v1/users/me/privacy`

Notes:
- Event is internal integration behavior; endpoint response does not expose outbox data.
- Existing auth/validation behavior of each endpoint remains unchanged.

## 4. Outbox Contract
For each new `USER_UPDATED` outbox record:
- `event_type`: `USER_UPDATED`
- `source`: `auth-service`
- `status`: `PENDING`
- `retry_count`: `0`
- `payload` minimum fields:
  - `user_id`
  - `email`
  - `updated_at` (ISO-8601 timestamp by current implementation)

Security:
- Payload must not contain sensitive data (`password_hash`, raw token, secret values).

## 5. Backend Behavior Summary (Transactional Outbox)
- Endpoint use case validates auth context and request payload.
- Business data update is persisted in `USER_PROFILES`.
- `USER_UPDATED` outbox event is inserted in `OUTBOX_EVENTS`.
- Both actions happen in the same ACID transaction.
- If payload serialization fails or outbox save fails, the transaction rolls back (no partial profile update, no orphan outbox).
- No direct dual-write from request flow to DB and broker.

## 6. FE Behavior
- FE does not call outbox directly and does not render raw outbox payload.
- FE only calls original endpoints (`profile`, `avatar`, `privacy`) and handles their API responses.
- On success: FE updates UI state/refetches account data.
- On failure: FE shows error based on endpoint response contract.

## 7. Acceptance Criteria
- Update profile success creates one `USER_UPDATED` outbox record.
- Update avatar success creates one `USER_UPDATED` outbox record.
- Toggle privacy success creates one `USER_UPDATED` outbox record.
- Created outbox record has `status = PENDING`, `retry_count = 0`.
- `payload` contains at least `user_id`, `email`, `updated_at`.
- Serialization/outbox persistence failure causes full transaction rollback.

## 8. Prompt for Stitch (UI only)
```text
Create UI behavior specs for Profile update features in 2Hands:
- Screens/features: Edit Profile, Update Avatar, Privacy Toggle
- Do not expose outbox/internal event data in UI
- For each feature: show loading, success, and error states
- Edit Profile errors: field-level validation and generic retry
- Update Avatar errors: invalid URL/upload flow fallback and retry
- Privacy Toggle errors: revert toggle UI state on failure
- Keep interactions responsive and accessible
```
