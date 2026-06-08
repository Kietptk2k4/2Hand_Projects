# View Shipment Support Detail - API & Behavior (Commerce)

## 1. Business Goal

API Commerce-side tra cuu **chi tiet van don** cho support: tracking, carrier status, shipping snapshot, timeline va webhook GHN summary. Read-only.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/admin/support/shipments/{shipmentId}`
- **Auth:** Bearer JWT — permission `SHIPMENT_SUPPORT_READ`

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `shipmentId` | UUID | ID shipment |

## 3. Response - Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Shipment support detail retrieved successfully",
  "data": {
    "shipment_id": "uuid",
    "order_id": "uuid",
    "seller_id": "uuid",
    "buyer_id": "uuid",
    "order_status": "PROCESSING",
    "carrier": "GHN",
    "internal_status": "SHIPPED",
    "carrier_status": "transporting",
    "ghn_order_code": "GHN-123",
    "tracking_number": "TRACK-9",
    "shipping_address": {},
    "status_history": [],
    "carrier_webhook_events": []
  }
}
```

- `internal_status`: `ShipmentStatus` noi bo Commerce.
- `carrier_status`: trang thai carrier (GHN raw / webhook moi nhat).
- Khong tra webhook payload, `external_provider_response`.

## 4. FE Behavior

- **Out of scope Commerce FE.**
- Consumer: Admin Service — `admin_api_fe_behavior/ViewShipmentSupportDetail-api-and-behavior.md`.

## 5. Business Rules

- `ViewShipmentSupportDetailUseCase` — read-only.
- Admin Service co the mask shipping contact truoc khi hien UI admin.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 403 | `COMMERCE-403` | Thieu `SHIPMENT_SUPPORT_READ` |
| 404 | `COMMERCE-404-SHIPMENT` | Shipment khong ton tai |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewShipmentSupportDetail.md`
- Admin proxy: `docs/api_fe_behavior/admin_api_fe_behavior/ViewShipmentSupportDetail-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-commerce-support-read.md`