# RetryFailedOutboxEvents - API and Behavior Spec

## 1. Scope
This document defines backend behavior for retrying failed or stuck outbox events in Auth Service.

In scope:
- Internal scheduled retry flow for outbox events
- Candidate selection from `FAILED` and timed-out `PENDING`
- Status transitions and retry metadata updates

Out of scope:
- Client-facing API endpoint for retry in MVP
- Broker-specific delivery guarantees beyond at-least-once pattern
- Consumer-side deduplication implementation details

## 2. Source Docs
- `docs/feature-requirements/auth/FR_RetryFailedOutboxEvents.md`
- `docs/use-cases/uc-event-publishing.md`
- `docs/business-flow/outbox-event-flow.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`

## 3. Backend Contract / Behavior
This feature is an internal backend scheduled job, not a user-facing API endpoint.

Scheduler trigger:
- Cron-based job reads config:
  - `auth.outbox.retry.enabled`
  - `auth.outbox.retry.cron`
  - `auth.outbox.retry.max-retries`
  - `auth.outbox.retry.pending-timeout-seconds`
  - `auth.outbox.retry.batch-size`

Candidate rules:
- `FAILED` with `retry_count < max_retries`
- `PENDING` older than `pending-timeout-seconds` with `retry_count < max_retries`

Processing rules:
- Claim batch and mark claimed events to `PROCESSING`
- Publish event through `OutboxEventPublisher` abstraction
- On success: set `PUBLISHED`, set `published_at`, clear `last_error`
- On failure: increment `retry_count`, set `FAILED`, update `last_error`
- If retry hits max retries: remain `FAILED` and emit warning/error logs

## 4. Standard Response / Log Semantics
Because this is internal job behavior, operation is observed via logs and metrics-style counters (if added later), not HTTP response payloads.

Log fields (minimum):
- `outboxEventId`
- `eventType`
- `retryCount`
- status transition result (`PUBLISHED` / `FAILED`)
- sanitized error message (no sensitive payload data)

## 5. Backend Behavior Summary (State Machine)
- Retry lane:
  - `FAILED` or timeout `PENDING` -> `PROCESSING` -> `PUBLISHED` (publish success)
  - `FAILED` or timeout `PENDING` -> `PROCESSING` -> `FAILED` (publish failure, retry_count++)
- Max-retry policy:
  - `retry_count >= max_retries` stays `FAILED` and is skipped by auto retry
- Delivery model:
  - At-least-once delivery; consumer side should be idempotent

## 6. FE Behavior
This feature has no mandatory FE user flow in MVP.

If an operator/admin dashboard is added:
- FE should display outbox health summaries (counts by status)
- Show empty state when no retry candidates
- Show failure state with safe error excerpts (no raw payload secrets)
- Optional manual retry action should be permission-gated and out of current scope

## 7. Acceptance Criteria
- `FAILED` events below max retries are retried by scheduler/use case.
- Timed-out `PENDING` events are picked for retry.
- Success path updates `status = PUBLISHED` and `published_at`.
- Failure path increments `retry_count`, sets `status = FAILED`, updates `last_error`.
- Events at/over max retries are not retried automatically.
- When scheduler is disabled, retry job does not execute.

## 8. Prompt for Stitch (UI only)
```text
Create an Outbox Retry Operations dashboard for 2Hands admin:
- Show counters by outbox status: PENDING, PROCESSING, PUBLISHED, FAILED
- Highlight failed events approaching max retry threshold
- Include empty state when no retry candidates
- Include error-state UI for publish failures with safe, non-sensitive error messages
- Provide filters by event_type and status
- Do not show raw sensitive payload data
- Design responsive and accessible layout
```
