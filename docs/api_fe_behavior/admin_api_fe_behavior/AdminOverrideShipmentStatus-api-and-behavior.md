# Admin Override Shipment Status - API & Behavior

## 1. Business Goal

Cho phep admin/support **cap nhat trang thai shipment noi bo** khi GHN webhook/sync cham hoac khong kha dung. Chi mutate Commerce DB qua Admin gateway - **khong** goi GHN API.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| PATCH | `/admin/api/v1/support/shipments/{shipmentId}/status` | Bearer + `SHIPMENT_SUPPORT_WRITE` |

**Path params:**

| Param | Type | Mo ta |
|-------|------|-------|
| `shipmentId` | UUID | ID shipment tren Commerce |

**Request body:**

```json
{
  "status": "DELIVERED",
  "reason": "GHN webhook khong ve sau 48h, xac nhan giao hang qua hotline GHN",
  "force": false
}
```

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `status` | string | Yes | ShipmentStatus enum (UPPER_SNAKE) |
| `reason` | string | Yes | Ly do audit, 10-500 ky tu |
| `force` | boolean | No | Default `false`. Can `SHIPMENT_SUPPORT_FORCE_WRITE` khi override tu terminal status |

## 3. Response - Success

**HTTP 200** - envelope chuan Admin:

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
  },
  "errors": null,
  "timestamp": "2026-06-10T10:00:00.123Z"
}
```

**No-op:** `previous_status === current_status` -> HTTP 200, message `Shipment status unchanged`.

## 4. Response - Error

| HTTP | code | Mo ta |
|------|------|-------|
| 400 | `ADMIN-400-VALIDATION` | `reason` / `status` khong hop le |
| 401 | `ADMIN-401` | Thieu hoac het han JWT |
| 403 | `ADMIN-403` | Thieu write hoac force permission |
| 404 | `ADMIN-404` | Khong tim thay shipment |
| 409 | `ADMIN-409-SHIPMENT-STATUS` | Transition khong hop le |
| 503 | `ADMIN-503` | Commerce unavailable |

## 5. Business Rules

1. Chi noi Commerce DB - khong goi GHN API.
2. `reason` bat buoc, min 10 ky tu.
3. Transition theo carrier policy (GHN / MANUAL).
4. Terminal status block khi `force=false`.
5. `DELIVERED` khong auto-complete order.
6. `shipment_status_history.raw_status = admin_override`.
7. Audit `SHIPMENT_STATUS_OVERRIDE` tren Admin Service.

## 6. Edge Cases

| Case | Hanh vi |
|------|---------|
| `status` = status hien tai | 200 no-op |
| Terminal + `force=false` | 409 |
| Commerce integration disabled | 503 |
| Override `DELIVERED` COD | Khong chung minh GHN da thu tien |

## 7. FE Integration Notes

- UI: `ShipmentSupportDetailTab` + `ShipmentStatusOverrideCard`
- API: `orderSupportApi.overrideShipmentStatus`
- Dropdown loc transition theo carrier; checkbox force cho Super Admin
- Confirm dialog truoc submit; refresh GET detail sau success
- QA: `frontend/src/fe-module/features/auth/admin/orderSupport/CHECKLIST.md`

## 8. Related

- FR: `docs/feature_requirements/admin/FR_AdminOverrideShipmentStatus.md`
- Flow: `docs/business_flow/admin_business_flow/order-support-flow.md`
- Commerce: `docs/api_fe_behavior/commerce_api_fe_behavior/AdminOverrideShipmentStatus-api-and-behavior.md`
- Read: `docs/api_fe_behavior/admin_api_fe_behavior/ViewShipmentSupportDetail-api-and-behavior.md`
