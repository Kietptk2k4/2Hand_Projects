# Functional Requirement - Track Shipment

## 1. Feature Overview

Cho phep buyer/seller theo doi tracking status cua shipment. Feature tra current shipment status, tracking number, carrier va timeline neu co.

## 2. Actors

- **Buyer:** Theo doi shipment cua order minh.
- **Seller:** Theo doi shipment cua shop minh.
- **System:** Return status and history.

## 3. Scope

**In Scope:**

- View current shipment status.
- View tracking number and carrier.
- View status history/timeline.

**Out of Scope:**

- Create shipment.
- Manual provider sync trigger.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/shipments/{shipmentId}/tracking`

**Auth:** Required (JWT)

## 5. Business Rules

- Shipment access scoped by buyer/seller ownership.
- `DELIVERED` shipment means delivered, not order completed.
- Status timeline should include raw provider status when safe/useful.

## 6. Database Impact

- Read `shipments`.
- Read `shipment_status_history`.
- Read order/order item ownership context.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Do not expose provider secret/raw sensitive payload.

## 9. Failure Cases

- Shipment not found/not owned -> 404.

## 10. Acceptance Criteria

- Tracking endpoint returns current status and timeline.
- Buyer/seller ownership enforced.
- Delivered and completed concepts are not conflated.

