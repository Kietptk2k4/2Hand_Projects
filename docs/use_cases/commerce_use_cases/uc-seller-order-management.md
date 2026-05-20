# UC - Seller Order Management

## 1. Overview

Use case nay mo ta nghiep vu seller xem va xu ly phan order thuoc shop minh. Trong multi-seller order, seller chi quan ly `order_items` va `shipments` cua seller do, khong quan ly toan bo order cua buyer.

## 2. Actors

- **Seller:** Xem order items, chuan bi hang, tao shipment, cap nhat tracking.
- **System:** Cap nhat status tu payment/shipment.
- **GHN:** Provider shipment khi carrier GHN.

## 3. Related Data

- `orders`
- `order_items`
- `shipments`
- `shipping_address_snapshots`
- `payments`
- `seller_shops`
- `seller_shipping_profiles`
- `outbox_events`

## 4. Business Rules

- Seller chi xem/thao tac order items co `seller_id` cua minh.
- Seller khong duoc xem order items cua seller khac trong cung order.
- Seller chi xu ly order khi order `PROCESSING`.
- payOS order phai paid truoc shipment.
- COD order co the shipment khi payment pending.
- Seller khong duoc set order item `COMPLETED`; buyer confirm/system auto-complete lam viec do.

## 5. Sub-Use Cases

### 5.1. View Seller Orders

**Main Flow:**

1. Seller request order list.
2. System resolve seller shop.
3. System query `order_items` by `seller_id`.
4. System include order/payment/shipment summary can thiet.
5. System tra paginated result.

**Exception Flow:** Seller has no shop -> 404/409.

### 5.2. View Seller Order Detail

**Main Flow:**

1. Seller request detail by order id.
2. System load only seller-owned order items.
3. System load shipment and shipping snapshot if fulfillment allowed.
4. System tra seller-scoped detail.

**Exception Flow:** No item belongs to seller -> 404.

### 5.3. Mark Items Processing

**Preconditions:** Order `PROCESSING`; order items `PENDING`; seller owns items.

**Main Flow:**

1. Seller confirms preparing items.
2. System validates ownership and order state.
3. System sets items `PROCESSING`.
4. System writes outbox event.

**Exception Flow:** Order not processing -> 409; item already terminal -> 409.

### 5.4. Create Shipment For Seller Items

**Preconditions:** Items belong to same seller/order and are `PROCESSING`.

**Main Flow:**

1. Seller selects items and carrier.
2. System validates ownership, status, payment/order readiness.
3. System creates shipment through shipping use case.
4. System attaches items to shipment.
5. System returns shipment detail.

**Exception Flow:** Item already has shipment -> 409; missing shipping profile -> 409.

### 5.5. Update Manual Shipment

**Preconditions:** Shipment carrier `MANUAL` or `SELF_DELIVERY`.

**Main Flow:**

1. Seller updates tracking/status.
2. System validates seller ownership and allowed transition.
3. System updates shipment and attached item status.
4. System writes history/outbox.

**Exception Flow:** Invalid transition -> 409.

## 6. Acceptance Criteria

- Seller APIs are scoped by seller/shop ownership.
- Seller cannot see unrelated seller items.
- Seller cannot create shipment before order `PROCESSING`.
- Seller cannot complete order item directly.
- Duplicate shipment for same order item is prevented.

