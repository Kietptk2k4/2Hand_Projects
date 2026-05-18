# PublishUserCreatedEvent - API and Behavior Spec

## 1. Scope
This document defines backend integration behavior for publishing `USER_CREATED` in Auth Service.

In scope:
- Transactional outbox write for `USER_CREATED` when a new user is created
- Trigger points from register and OAuth new-user flows
- Outbox payload and record structure

Out of scope:
- Outbox worker polling/publishing/retry execution details
- Consumer-side handling in other services
- Any frontend endpoint for outbox access

## 2. Source Docs
- `docs/feature-requirements/auth/FR_PublishUserCreatedEvent.md`
- `docs/feature-requirements/auth/FR_Register_Email.md`
- `docs/feature-requirements/auth/FR_Login_OAuth.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`

## 3. Contract Type
This feature is backend internal integration behavior, not a dedicated public endpoint.

`USER_CREATED` is produced as an outbox record inside the same transaction as user creation data.

## 4. Trigger Points and Current Status
- `POST /api/v1/auth/register`
  - New user is created with `status = PENDING_VERIFICATION`
  - Backend writes both:
    - `USER_CREATED` outbox
    - `EMAIL_VERIFICATION_REQUESTED` outbox
- OAuth callback flow (`/login/oauth2/code/{provider}` via Spring Security)
  - Only when OAuth creates a brand-new user
  - Backend writes `USER_CREATED` outbox
  - Existing-user OAuth login does not create `USER_CREATED`

## 5. Outbox Record Structure
For each `USER_CREATED` record in `outbox_events`:
- `event_type`: `USER_CREATED`
- `source`: `auth-service`
- `status`: `PENDING`
- `retry_count`: `0`
- `payload` (minimum):
  - `user_id`
  - `email`
  - `status` (included by current implementation)

Security rule:
- Payload must not include sensitive fields (`password_hash`, raw tokens, secrets).

## 6. Backend Behavior Summary
- Auth use case opens a single DB transaction.
- Business writes are persisted (`users`, `user_profiles`, `user_settings`, and register-only `verification_tokens`).
- `USER_CREATED` outbox is inserted in the same transaction.
- If payload serialization fails or outbox save fails, transaction is rolled back (no partial user data).
- No dual-write from request flow to DB and broker directly.

## 7. FE Behavior
- FE does not call an outbox endpoint and does not render raw outbox data.
- FE interacts only with original auth endpoints:
  - Register: handles normal register response and verify-email navigation
  - OAuth login/register: handles success/failure redirect and session flow
- Outbox publishing is transparent backend integration.

## 8. Acceptance Criteria
- New OAuth user creation writes exactly one `USER_CREATED` outbox record (`PENDING`, `retry_count = 0`).
- New register user creation writes `USER_CREATED` in the same transaction as user data.
- Register flow still writes `EMAIL_VERIFICATION_REQUESTED` and preserves verify-email behavior.
- `USER_CREATED` payload contains at least `user_id` and `email` (plus `status` in current implementation).
- Any serialization/outbox write error causes full transaction rollback.

## 9. Prompt for Stitch (UI only)
```text
Create UI behavior specs for Register and OAuth success/failure states in 2Hands:
- Do not expose outbox/internal events in UI
- Register success: show success message and navigate to Verify Email screen
- Register errors: show clear messages for 400/409/429/500
- OAuth new/existing user success: continue normal post-login routing
- OAuth failure: show retry toast/message and allow retry
- Include loading/disabled states for submit and OAuth buttons
- Keep responsive and accessible interactions
```
