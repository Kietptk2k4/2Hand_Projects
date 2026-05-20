# UC - Shipping

## 1. Overview

Use case nay mo ta nghiep vu shipping trong Commerce Service: tinh phi ship, tao shipment, luu snapshot dia chi, tich hop GHN, cap nhat tracking va xu ly webhook giao hang. Shipment la fulfillment unit theo seller; mot order multi-seller co the co nhieu shipment.

## 2. Actors

- **Buyer:** Xem shipment, tracking number, estimated delivery date va shipping address snapshot.
- **Seller:** Tao shipment, nhap weight/package info, cap nhat tracking voi manual/self-delivery.
- **System:** Xu ly GHN webhook va sync tracking.
- **GHN:** Shipping provider.

## 3. Related Data

- `orders`
- `order_items`
- `shipments`
- `shipping_address_snapshots`
- `seller_shipping_profiles`
- `shipment_status_history`
- `ghn_webhook_logs`
- `outbox_events`

## 4. Business Rules

- Khong tao shipment neu order chua `PROCESSING`.
- Shipment thuoc mot order va mot seller.
- Shipment phai co one shipping address snapshot.
- Seller chi tao/xem shipment cua shop minh.
- For GHN, `tracking_number = ghn_order_code`.
- Shipment `DELIVERED` khong auto complete order item; can buyer confirm hoac auto complete job.
- COD shipment co `cod_amount` theo final amount/order allocation policy.

## 5. Sub-Use Cases

### 5.1. Calculate Shipping Fee

**Preconditions:** Buyer co destination address; selected items hop le.

**Main Flow:**

1. System group selected items by seller.
2. System load seller pickup profile.
3. System load buyer destination address.
4. System tinh weight theo product quantity.
5. Neu GHN enabled, call GHN fee API; neu chua, dung mock calculator.
6. System tra shipping fee va estimated delivery date theo seller group.

**Exception Flow:** Missing seller shipping profile -> 409; provider timeout -> fallback mock neu configured, nguoc lai 503.

### 5.2. Create Shipment

**Preconditions:** Order `PROCESSING`; seller owns selected order items; items chua co shipment.

**Main Flow:**

1. Seller request create shipment.
2. System validate seller ownership va order status.
3. System validate selected order items `PENDING/PROCESSING`.
4. System tao shipment `PENDING`.
5. System tao shipping address snapshot.
6. Neu carrier `GHN`, call GHN create order va luu `ghn_order_code`, `ghn_shop_id`, `tracking_number`, provider response.
7. System gan `shipment_id` vao order items.
8. System ghi shipment history va outbox event.

**Exception Flow:** Order not processing -> 409; item already shipped -> 409; GHN failure -> keep local pending/failure state theo policy.

### 5.3. Track Shipment

**Preconditions:** Buyer owns order or seller owns shipment.

**Main Flow:**

1. Actor request shipment detail.
2. System validate ownership.
3. System load shipment, attached order items, snapshot va status history.
4. System tra carrier, tracking number, status va ETA.

**Exception Flow:** Shipment not found/not owned -> 404.

### 5.4. Process GHN Webhook

**Preconditions:** GHN sends webhook payload.

**Main Flow:**

1. System ghi `ghn_webhook_logs`.
2. System resolve shipment by `ghn_order_code`.
3. System map raw status sang domain shipment status.
4. System update shipment status, timestamps va history.
5. System update attached order item statuses.
6. System ghi outbox event va mark webhook processed.

**Exception Flow:** Shipment not found -> log unprocessed; duplicate status -> idempotent no-op.

### 5.5. Manual/Self Delivery Update

**Preconditions:** Shipment carrier `MANUAL` hoac `SELF_DELIVERY`; seller owns shipment.

**Main Flow:**

1. Seller update tracking/status.
2. System validate allowed transition.
3. System update shipment va attached order items.
4. System ghi history va outbox event.

**Exception Flow:** Invalid transition -> 409; seller not owner -> 403/404.

## 6. Acceptance Criteria

- Shipment chi duoc tao khi order `PROCESSING`.
- Seller chi thao tac shipment cua minh.
- Buyer xem duoc tracking cua order minh.
- GHN webhook idempotent.
- Delivered shipment set order items `DELIVERED`, khong set `COMPLETED`.

