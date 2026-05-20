# Functional Requirement - View Shipment

## 1. Feature Overview

Cho phep buyer hoac seller xem thong tin shipment, bao gom carrier, tracking, status, fee, ETA, attached order items va shipping address snapshot theo quyen truy cap.

## 2. Actors

- **Buyer:** Xem shipment cua order minh.
- **Seller:** Xem shipment cua shop minh.
- **System:** Load shipment detail.

## 3. Scope

**In Scope:**

- View shipment detail by id.
- Return tracking/status/address snapshot.
- Scope response by buyer/seller ownership.

**Out of Scope:**

- Update shipment.
- Process webhook.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/shipments/{shipmentId}`

**Auth:** Required (JWT)

## 5. Business Rules

- Buyer can view shipment only if shipment belongs to buyer order.
- Seller can view shipment only if `shipments.seller_id` belongs to their shop.
- Seller response can include fulfillment address snapshot.
- Do not expose unrelated payment provider details.

## 6. Database Impact

- Read `shipments`.
- Read `orders`, `order_items`.
- Read `shipping_address_snapshots`.
- Optional read `shipment_status_history`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Ownership check by buyer order or seller id.

## 9. Failure Cases

- Shipment not found/not owned -> 404.

## 10. Acceptance Criteria

- Buyer sees shipment of own order.
- Seller sees shipment of own shop.
- Tracking/status/address snapshot are included.
- Unrelated user cannot access shipment.

