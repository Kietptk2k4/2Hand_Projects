# Functional Requirement - View Shipment Support Detail (Commerce)

## 1. Feature Overview

Commerce Service expose chi tiet shipment read-only cho support admin: tracking, carrier status, address snapshot, timeline va GHN webhook summary.

## 2. Actors

- **Support Admin:** Tra cuu shipment (qua Admin Service).
- **Commerce Service:** Own shipment data.

## 3. Scope

**In Scope:**

- Shipment detail, status history, carrier webhook events metadata.
- Permission `SHIPMENT_SUPPORT_READ`.

**Out of Scope:**

- Shipment mutation.
- Commerce buyer/seller tracking UI (separate FR).

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/admin/support/shipments/{shipmentId}`

**Auth:** Required — `SHIPMENT_SUPPORT_READ`

## 5. Business Rules

- Read-only.
- Distinguish internal vs carrier status.
- No raw webhook payload.

## 6. Database Impact

- Read `shipments`, histories, webhook logs, address snapshots.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- PII may be masked by Admin Service layer.

## 9. Failure Cases

- Shipment not found -> 404.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Support can inspect shipment with permission.
- Carrier investigation data available without secrets.

## 11. Related

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewShipmentSupportDetail-api-and-behavior.md`
- Admin FR: `docs/feature_requirements/admin/FR_ViewShipmentSupportDetail.md`