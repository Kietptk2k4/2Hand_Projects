# Functional Requirement - Publish Commerce Events

## 1. Feature Overview

Commerce Service publish domain events bang Outbox Pattern de cac service khac nhu Notification/Admin/Analytics co the consume ma khong lam mat event khi transaction domain da commit.

## 2. Actors

- **Application Use Case:** Insert outbox event with domain change.
- **Outbox Worker:** Publish event to broker.
- **Message Broker:** Nhan event.
- **Consumer Services:** Consume events.

## 3. Scope

**In Scope:**

- Insert outbox event in same transaction as domain mutation.
- Publish `PENDING` events to broker.
- Use topic naming `{service}.{domain}.{action}`.
- Mark published status.

**Out of Scope:**

- Consumer business logic.
- Broker infrastructure provisioning.

## 4. Event Contract

Event envelope:

- `event_id`
- `event_type`
- `event_key`
- `aggregate_id`
- `source`
- `occurred_at`
- `payload`

Example topics:

- `commerce.order.created`
- `commerce.payment.paid`
- `commerce.shipment.status_changed`
- `commerce.inventory.released`
- `commerce.product.removed`

## 5. Business Rules

- Domain change and outbox insert must be atomic.
- Do not publish directly before DB commit.
- `event_key` should be deterministic.
- Outbox event initial status `PENDING`.
- Worker publishes events at-least-once.

## 6. Database Impact

- Insert `outbox_events`.
- Update outbox status after publish.

## 7. Transaction

- Domain use case transaction includes outbox insert.
- Worker transaction claims and updates event status.

## 8. Security

- Do not include secrets/tokens/provider credentials in event payload.
- Payload should contain only data consumers need.

## 9. Failure Cases

- Domain transaction rollback -> no event persisted.
- Broker publish failure -> event remains retryable.
- Duplicate event key -> prevent duplicate event insert if unique enforced.

## 10. Acceptance Criteria

- Every external commerce event is written through outbox.
- Committed domain changes have corresponding pending events.
- Worker publishes and marks events published.
- Failed publish can retry without losing event.

