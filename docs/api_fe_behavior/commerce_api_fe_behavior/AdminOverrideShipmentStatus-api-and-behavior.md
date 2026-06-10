# Admin Override Shipment Status - API & Behavior (Commerce)

## 1. Business Goal

Cho phep admin cap nhat **trang thai shipment noi bo** khi GHN webhook/sync khong kha dung. Chi mutate Commerce DB; khong goi GHN API.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/admin/support/shipments/{shipmentId}/status`
- **Auth:** Bearer JWT — permission `SHIPMENT_SUPPORT_WRITE` (`SHIPMENT_SUPPORT_FORCE_WRITE` khi `force=true`)

### Request body

```json
{
  "status": "DELIVERED",
  "reason": "GHN webhook khong ve sau 48h, xac nhan giao hang qua hotline GHN",
  "force": false
}
```

## 3. Response - Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Shipment status overridden successfully",
  "data": {
    "shipment_id": "uuid",
    "order_id": "uuid",
    "carrier": "GHN",
    "previous_status": "SHIPPED",
    "current_status": "DELIVERED",
    "override_source": "ADMIN",
    "raw_status": "admin_override",
    "order_items_updated": 1,
    "occurred_at": "2026-06-10T10:00:00Z"
  }
}
```

## 4. Response - Error

| HTTP | code | Mo ta |
|------|------|-------|
| 400 | `COMMERCE-400-VALIDATION` | `reason` thieu / qua ngan |
| 400 | `COMMERCE-400` | `status` khong hop le |
| 403 | `COMMERCE-403` | Thieu permission |
| 404 | `COMMERCE-404-SHIPMENT` | Khong tim thay shipment |
| 409 | `COMMERCE-409-SHIPMENT-STATUS` | Transition khong hop le |

## 5. Business Rules

- `reason` bat buoc (10-500 ky tu).
- Transition theo `AdminShipmentStatusOverridePolicy` (GHN / MANUAL carrier policy).
- Ghi `shipment_status_history.raw_status = admin_override`.
- Cap nhat `order_items` + outbox `COMMERCE_SHIPMENT_STATUS_CHANGED`.
- `DELIVERED` khong auto-complete order.
- Khong goi GHN API.

## 6. Edge Cases

- Cung `status` hien tai -> 200 no-op.
- Terminal status + `force=false` -> 409.
- `force=true` -> can `SHIPMENT_SUPPORT_FORCE_WRITE`.

## 7. Data Dependencies

- `shipments`, `order_items`, `shipment_status_history`, `outbox_events`

## 8. FE Integration Notes

- Admin FE goi Admin gateway: `PATCH /admin/api/v1/support/shipments/{shipmentId}/status` (khong goi Commerce truc tiep tu browser).
- UI: `ShipmentStatusOverrideCard` tren tab Shipment Support Detail.
- Manual QA: `frontend/src/fe-module/features/auth/admin/orderSupport/CHECKLIST.md`.

## 9. Related

- FR: `docs/feature_requirements/admin/FR_AdminOverrideShipmentStatus.md`
- Admin gateway doc: `docs/api_fe_behavior/admin_api_fe_behavior/AdminOverrideShipmentStatus-api-and-behavior.md`