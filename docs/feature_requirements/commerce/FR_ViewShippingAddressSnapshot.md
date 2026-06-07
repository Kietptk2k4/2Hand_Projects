# Functional Requirement - View Shipping Address Snapshot

## 1. Feature Overview

Cho phep buyer/seller xem shipping address snapshot cua shipment theo quyen truy cap. Snapshot la dia chi immutable tai thoi diem tao shipment/order.

## 2. Actors

- **Buyer:** Xem dia chi giao hang cua order minh.
- **Seller:** Xem dia chi de fulfill shipment cua shop minh.
- **System:** Load snapshot.

## 3. Scope

**In Scope:**

- View snapshot by shipment id.
- Return receiver, phone, location codes, detail and full address.

**Out of Scope:**

- Update snapshot.
- Update user address book.
- Dedicated Commerce FE client cho endpoint nay — UI lay dia chi qua `FR_ViewShipment` (`shipping_address` trong response shipment detail).

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/shipments/{shipmentId}/address-snapshot`

**Auth:** Required (JWT)

## 5. Business Rules

- Snapshot is immutable.
- Buyer can view if shipment belongs to their order.
- Seller can view if shipment belongs to their shop.
- Updating/deleting user address does not change snapshot.

## 6. Database Impact

- Read `shipping_address_snapshots`.
- Read `shipments`, `orders` for ownership.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Address data is PII; ownership check is mandatory.

## 9. Failure Cases

- Shipment/snapshot not found -> 404.
- Not owned -> 404/403.

## 10. Acceptance Criteria

- Authorized buyer/seller can view snapshot.
- Snapshot data remains unchanged after address book updates.
- Unauthorized users cannot access address PII.

## 11. Related

- Commerce FE hien thi dia chi: `FR_ViewShipment.md` (buyer/seller shipment detail, field `shipping_address`).

