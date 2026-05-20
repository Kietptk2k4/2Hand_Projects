# UC - Background Jobs

## 1. Overview

Use case nay mo ta cac system jobs cua Commerce Service: sync inventory/cart, expire payment, auto cancel unpaid order, auto complete delivered order, sync shipment tracking, retry webhook va outbox publishing. Jobs phai idempotent va an toan khi retry.

## 2. Actors

- **System Scheduler/Worker:** Chay jobs.
- **External Providers:** GHN/payOS khi job can sync.
- **Message Broker:** Nhan outbox events.

## 3. Related Data

- `product_inventories`
- `products`
- `cart_items`
- `orders`
- `order_items`
- `payments`
- `shipments`
- `payment_webhook_logs`
- `ghn_webhook_logs`
- `outbox_events`

## 4. Business Rules

- Jobs must be idempotent.
- Use small batches and row locking/claiming.
- Do not replace synchronous validation in checkout.
- COD payment must not expire only by age.
- Auto complete order only when payment paid condition is satisfied.
- Outbox worker is responsible for broker publish.

## 5. Sub-Use Cases

### 5.1. Inventory And Cart Sync

**Main Flow:**

1. Job finds products/inventory needing sync.
2. If stock 0, set product `OUT_OF_STOCK`.
3. If stock restored, product can return `ACTIVE` if valid.
4. Job updates cart item status to `OUT_OF_STOCK`, `INVALID_PRODUCT`, or `ACTIVE`.

### 5.2. Expire Pending payOS Payment

**Main Flow:**

1. Job finds `PAYOS` payments `PENDING` past `expired_at`.
2. Job locks payment/order.
3. Job marks payment `EXPIRED`.
4. Job cancels awaiting order.
5. Job releases reserved inventory.
6. Job writes histories/outbox events.

### 5.3. Auto Cancel Unpaid Order

**Main Flow:**

1. Job finds stale orders `CREATED/AWAITING_PAYMENT` with pending payment.
2. Job validates no shipment started.
3. Job cancels order/payment.
4. Job releases inventory.

### 5.4. Auto Complete Delivered Order

**Main Flow:**

1. Job finds order items `DELIVERED` older than configured window, default 7 days.
2. Job marks items `COMPLETED`.
3. For COD, job marks payment paid if MVP policy allows.
4. Job completes order if all items completed and payment paid.

### 5.5. Shipment Tracking Sync

**Main Flow:**

1. Job finds active GHN shipments.
2. Job calls GHN tracking API.
3. Job maps raw status to domain shipment status.
4. Job updates shipment/order items idempotently.

### 5.6. Webhook Retry

**Main Flow:**

1. Job finds unprocessed payment/GHN webhook logs.
2. Job reprocesses valid logs.
3. Job marks processed or keeps for retry according to failure.

### 5.7. Outbox Publishing

**Main Flow:**

1. Worker claims pending/failed outbox events.
2. Worker publishes to broker.
3. Worker marks published or failed.

## 6. Acceptance Criteria

- Jobs are safe under retry.
- Payment expiration releases inventory exactly once.
- Auto complete does not violate payment/order completion invariant.
- Shipment sync and webhook use same status mapping.
- Outbox failed events can retry.

