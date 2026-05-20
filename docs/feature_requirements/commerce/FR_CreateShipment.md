# Functional Requirement - Create Shipment

## 1. Feature Overview

Cho phep seller tao shipment cho order items cua shop minh sau khi order du dieu kien `PROCESSING`. Shipment co the dung GHN, manual hoac self-delivery.

## 2. Actors

- **Seller:** Tao shipment cho order items cua shop minh.
- **System:** Validate fulfillment readiness, create shipment and snapshot.
- **GHN:** Provider tao shipment neu carrier GHN.

## 3. Scope

**In Scope:**

- Create local shipment.
- Attach order items to shipment.
- Create shipping address snapshot.
- Call GHN create order when carrier is `GHN`.
- Store tracking/provider response.

**Out of Scope:**

- GHN webhook processing.
- Refund/dispute.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/shipments`

**Auth:** Required (JWT seller)

**Request body:**

- `order_id`
- `order_item_ids`
- `carrier`
- `shipment_type`
- `weight_gram` optional
- `tracking_number` optional for manual/self-delivery

## 5. Business Rules

- Order must be `PROCESSING`.
- Seller must own all selected order items.
- Selected items must not already have shipment.
- For payOS, payment must be `PAID`.
- For COD, payment can remain `PENDING` and shipment has COD amount.
- Each shipment must have one shipping address snapshot.

## 6. Database Impact

- Read `orders`, `order_items`, `payments`, `seller_shipping_profiles`.
- Insert `shipments`.
- Insert `shipping_address_snapshots`.
- Update `order_items.shipment_id`.
- Insert shipment history/outbox events.

## 7. Transaction

- Required for local shipment creation and item attach.
- Avoid long DB transaction around slow GHN call if possible.

## 8. Security

- JWT required.
- Seller ownership check by `seller_id`/shop.

## 9. Failure Cases

- Order not processing -> 409.
- Item not owned/already shipped -> 403/409.
- Missing seller shipping profile -> 409.
- GHN create order fails -> 502/503 or pending provider state.

## 10. Acceptance Criteria

- Seller can create shipment only for own processing items.
- Shipment cannot be duplicated for same item.
- Shipment includes address snapshot.
- GHN shipment stores GHN order code/tracking.

