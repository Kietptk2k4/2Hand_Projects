# Functional Requirement - Consume Domain Event

## 1. Feature Overview

Notification Service consume domain events tu Auth, Social, Commerce va Admin qua broker/outbox de bat dau notification pipeline.

## 2. Actors

- **Producer Service:** Publish domain event.
- **Message Broker:** Deliver event at-least-once.
- **Notification Consumer:** Consume event.

## 3. Scope

**In Scope:**

- Receive broker message.
- Parse and validate event envelope.
- Route message to event storage flow.

**Out of Scope:**

- Business mutation in producer service.
- Creating user notification directly before event is durably stored.

## 4. Input Contract

Required envelope fields:

- `event_id`
- `event_type`
- `source_service`
- `event_key` recommended
- `aggregate_type` optional
- `aggregate_id` optional
- `actor_id` optional
- `recipient_user_ids` optional by event type
- `payload`
- `occurred_at`

## 5. Business Rules

- `source_service` must be allowlisted: `AUTH`, `SOCIAL`, `COMMERCE`, `ADMIN`, `SYSTEM`.
- `event_type` must use canonical `UPPER_SNAKE_CASE` or configured alias.
- Consumer must not acknowledge broker message before durable insert/dedup outcome.
- Payload must be sanitized before logging.

## 6. Database Impact

- Delegates persistence to `notification_events`.

## 7. Transaction

- Consumer should wrap validation and insert/dedup decision in a short transaction.

## 8. Security

- Internal broker/service credentials required.
- Do not log passwords, tokens, OTPs, provider credentials or raw authorization headers.

## 9. Failure Cases

- Malformed JSON -> reject/ack by broker policy with sanitized error.
- DB unavailable -> do not ack; allow redelivery.
- Unsupported source/event type -> store failed or reject by allowlist policy.

## 10. Acceptance Criteria

- Valid broker message enters Notification pipeline.
- Duplicate broker delivery is handled idempotently downstream.
- Invalid message does not crash consumer loop.

