# Functional Requirement - Manage Seller Shipment

## 1. Feature Overview

Cho phep seller tao va quan ly shipment cua order items thuoc shop minh, bao gom tao shipment va cap nhat tracking/status cho manual/self-delivery.

## 2. Actors

- **Seller:** Tao/cap nhat shipment cua shop minh.
- **System:** Validate ownership and shipment transition.
- **GHN:** Provider neu carrier GHN.

## 3. Scope

**In Scope:**

- Create seller shipment for processing items.
- Update manual/self-delivery tracking/status.
- View shipment summary.

**Out of Scope:**

- Buyer confirm received.
- GHN webhook processing.
- Refund/dispute.

## 4. API Contract

**Endpoints:**

- `POST /commerce/api/v1/seller/shipments`
- `PATCH /commerce/api/v1/seller/shipments/{shipmentId}`

**Auth:** Required (JWT seller)

## 5. Business Rules

- Seller manages only own shipments.
- Shipment cannot be created unless order `PROCESSING`.
- Selected order items must be seller-owned and not already shipped.
- Manual/self-delivery status transitions must be valid.
- Seller cannot mark order item `COMPLETED`; buyer/system controls completion.

## 6. Database Impact

- Read/update `order_items`.
- Read/update/insert `shipments`.
- Insert `shipping_address_snapshots` on create.
- Insert `shipment_status_history`.
- Insert outbox events.

## 7. Transaction

- Write transaction required for create/update local shipment.
- External GHN calls should avoid long DB transaction if possible.

## 8. Security

- JWT required.
- Seller ownership check mandatory.

## 9. Failure Cases

- Shipment/item not owned -> 404.
- Invalid order/item status -> 409.
- Duplicate shipment for item -> 409.
- GHN failure -> 502/503 or pending provider state.

## 10. Acceptance Criteria

- Seller creates shipment only for own processing items.
- Seller can manage manual/self-delivery tracking.
- Seller cannot complete order item directly.
- Duplicate shipment is prevented.

