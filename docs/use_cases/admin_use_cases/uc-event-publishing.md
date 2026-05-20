# UC - Event Publishing

## 1. Overview

Use case nay mo ta Admin Service publish events bang Outbox Pattern. Local admin decision va outbox event phai commit atomic, sau do worker publish async.

## 2. Actors

- **Admin Use Cases:** Insert outbox event with local decision.
- **Outbox Worker:** Publish events.
- **Message Broker:** Transport events.
- **Consumer Services:** Auth/Social/Commerce/Notification.

## 3. Related Data

- `outbox_events`

## 4. Business Rules

- Do not publish directly before DB commit.
- Initial event status `PENDING`.
- Publish semantics at-least-once.
- Consumers deduplicate.
- Failed events retry.

## 5. Sub-Use Cases

### 5.1. Write Outbox Event

**Main Flow:** Domain/admin use case writes local record and outbox event in same transaction.

### 5.2. Publish Pending Events

**Main Flow:** Worker claims `PENDING`, marks `PROCESSING`, publishes, then marks `PUBLISHED`.

### 5.3. Retry Failed Events

**Main Flow:** Worker retries `FAILED` events with retry count.

## 6. Event Catalog

- `USER_SUSPENDED`
- `USER_BANNED`
- `USER_RESTRICTED`
- `USER_ENFORCEMENT_REVOKED`
- `USER_ENFORCEMENT_EXPIRED`
- `PRODUCT_REMOVED`
- `REVIEW_HIDDEN`
- `SHOP_SUSPENDED`
- `POST_MODERATED`
- `COMMENT_MODERATED`
- `SYSTEM_CONFIG_UPDATED`
- `SYSTEM_ANNOUNCEMENT_PUBLISHED`

## 7. Acceptance Criteria

- Outbox insert is atomic with local decision.
- Worker publishes events.
- Failed events retry.
- No event is lost after local transaction commits.

