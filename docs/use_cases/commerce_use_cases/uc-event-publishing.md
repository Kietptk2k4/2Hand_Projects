# UC - Event Publishing

## 1. Overview

Use case nay mo ta cach Commerce Service publish domain events bang Outbox Pattern. Tat ca event publish ra broker phai duoc ghi vao `outbox_events` trong cung transaction voi domain change, sau do outbox worker publish bat dong bo.

## 2. Actors

- **Application Use Case:** Tao domain change va insert outbox event.
- **Outbox Worker:** Poll va publish events.
- **Message Broker:** Nhan event.
- **Consumer Services:** Notification/Admin/Analytics/future services.

## 3. Related Data

- `outbox_events`
- Domain tables thay doi theo event: `orders`, `payments`, `shipments`, `products`, `reviews`, `product_inventories`.

## 4. Business Rules

- Domain change va outbox insert phai atomic.
- Khong publish event truc tiep tu controller/use case truoc DB commit.
- Outbox worker la component publish broker.
- Event payload phai co `event_id`, `event_type`, `event_key`, `aggregate_id`, `source`, `occurred_at`, `payload`.
- Publish semantics la at-least-once; consumers phai idempotent.
- `event_key` nen deterministic de tranh duplicate event.

## 5. Sub-Use Cases

### 5.1. Write Outbox Event With Domain Change

**Preconditions:** Domain use case co state change can publish.

**Main Flow:**

1. Application use case begin transaction.
2. Use case apply domain change.
3. Use case insert `outbox_events` status `PENDING`.
4. Transaction commit.

**Exception Flow:** Transaction rollback -> domain change va event deu rollback.

**Postconditions:** Event pending exists only if domain change committed.

### 5.2. Publish Pending Events

**Main Flow:**

1. Worker claims batch events `PENDING` or retryable `FAILED`.
2. Worker marks events `PROCESSING`.
3. Worker publishes event payload to broker topic.
4. On success, mark `PUBLISHED` and set `published_at`.
5. On failure, mark `FAILED`, increment `retry_count`, store `last_error`.

**Exception Flow:** Broker down -> events remain failed/retryable.

### 5.3. Retry Failed Events

**Main Flow:**

1. Worker selects `FAILED` events according to backoff policy.
2. Worker marks `PROCESSING`.
3. Worker republishes.
4. Worker marks `PUBLISHED` or `FAILED`.

### 5.4. Recover Stale Processing Events

**Main Flow:**

1. Worker finds events `PROCESSING` older than timeout.
2. Worker marks them `FAILED` or `PENDING`.
3. Normal retry flow handles them.

## 6. Event Catalog

Order:

- `COMMERCE_ORDER_CREATED`
- `COMMERCE_ORDER_CANCELLED`
- `COMMERCE_ORDER_COMPLETED`
- `COMMERCE_ORDER_READY_FOR_PROCESSING`

Payment:

- `COMMERCE_PAYMENT_CREATED`
- `COMMERCE_PAYMENT_PAID`
- `COMMERCE_PAYMENT_FAILED`
- `COMMERCE_PAYMENT_CANCELLED`
- `COMMERCE_PAYMENT_EXPIRED`

Shipment:

- `COMMERCE_SHIPMENT_CREATED`
- `COMMERCE_SHIPMENT_STATUS_CHANGED`
- `COMMERCE_SHIPMENT_DELIVERED`
- `COMMERCE_SHIPMENT_FAILED`

Inventory:

- `COMMERCE_INVENTORY_RESERVED`
- `COMMERCE_INVENTORY_SETTLED`
- `COMMERCE_INVENTORY_RELEASED`
- `COMMERCE_INVENTORY_LOW_STOCK`

Catalog/review/moderation:

- `COMMERCE_PRODUCT_PUBLISHED`
- `COMMERCE_PRODUCT_REMOVED`
- `COMMERCE_SHOP_SUSPENDED`
- `COMMERCE_REVIEW_CREATED`
- `COMMERCE_REVIEW_HIDDEN`

## 7. Topic Naming

Use topic style `{service}.{domain}.{action}`:

- `commerce.order.created`
- `commerce.payment.paid`
- `commerce.shipment.status_changed`
- `commerce.inventory.released`
- `commerce.product.removed`
- `commerce.review.created`

## 8. Acceptance Criteria

- Every external event is written through `outbox_events`.
- Domain mutation and event insert are atomic.
- Worker publishes pending events and marks status.
- Failed events retry without data loss.
- Duplicate publish is tolerated by event id/key.
- Stale `PROCESSING` events can recover.

