# Manage Seller Shipment – API & Behavior

## 1. Business Goal

Seller tao va quan ly shipment cho order items thuoc shop: tao shipment (POST), xem summary (GET), cap nhat tracking/status cho `MANUAL` / `SELF_DELIVERY` (PATCH).

## 2. API Contract

### 2.1 Create shipment

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/shipments`
- **Auth:** Bearer JWT (`user_id` = `shipments.seller_id`)

Chi tiet: [CreateShipment-api-and-behavior.md](./CreateShipment-api-and-behavior.md)

### 2.2 View shipment summary

- **Method:** GET
- **URL:** `/commerce/api/v1/seller/shipments/{shipmentId}`
- **Auth:** Bearer JWT (seller)

### 2.3 Update manual/self-delivery shipment

- **Method:** PATCH
- **URL:** `/commerce/api/v1/seller/shipments/{shipmentId}`
- **Auth:** Bearer JWT (seller)

#### Request body (it nhat mot field)

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `status` | string | no* | `READY_TO_SHIP`, `SHIPPED`, `DELIVERED`, `FAILED` (theo transition hop le) |
| `tracking_number` | string | no* | Cap nhat ma van don truoc `DELIVERED` |

\* Can it nhat `status` hoac `tracking_number`.

**Transition hop le (MANUAL / SELF_DELIVERY):**

- `PENDING` → `READY_TO_SHIP`
- `READY_TO_SHIP` → `SHIPPED`
- `SHIPPED` → `DELIVERED` | `FAILED`

Seller **khong** duoc dat order item `COMPLETED` (buyer confirm / auto-complete xu ly).

## 3. Response – Success (GET / PATCH)

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat shipment thanh cong.",
  "data": {
    "shipment_id": "770e8400-e29b-41d4-a716-446655440002",
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "880e8400-e29b-41d4-a716-446655440003",
    "carrier": "MANUAL",
    "shipment_type": "STANDARD",
    "status": "SHIPPED",
    "ghn_order_code": null,
    "tracking_number": "TRK-ABC123",
    "shipping_fee": 20000,
    "cod_amount": 0,
    "weight_gram": 500,
    "estimated_delivery_date": "2026-05-24",
    "shipped_at": "2026-05-21T12:00:00Z",
    "delivered_at": null,
    "created_at": "2026-05-21T10:00:00Z",
    "updated_at": "2026-05-21T12:00:00Z",
    "shipping_address": {
      "receiver_name": "Nguyen Van A",
      "phone": "0900000000",
      "province_code": "79",
      "district_code": "760",
      "ward_code": "26734",
      "address_detail": "123 Street",
      "full_address": "123 Street, Ward, District, HCMC"
    },
    "order_items": [
      {
        "order_item_id": "660e8400-e29b-41d4-a716-446655440001",
        "product_name_snapshot": "Ao thun",
        "quantity": 1,
        "status": "SHIPPED"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-21T12:00:01Z"
}
```

## 4. FE Behavior

- PATCH chi dung cho shipment `carrier` = `MANUAL` hoac `SELF_DELIVERY`; GHN cap nhat qua webhook/provider.
- Sau `SHIPPED` / `DELIVERED`, order items duoc cap nhat tuong ung (`SHIPPED`, `DELIVERED`); khong bao gio `COMPLETED`.
- GET dung de hien thi dia chi snapshot + danh sach item trong shipment.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400` | Khong co `status` va `tracking_number` |
| 404 | `COMMERCE-404-SHIPMENT` | Shipment khong ton tai hoac khong thuoc seller |
| 409 | `COMMERCE-409-SHIPMENT-STATUS` | Transition khong hop le / shipment terminal |
| 409 | `COMMERCE-409-SHIPMENT-CARRIER` | Carrier GHN — seller khong PATCH |
| 409 | `COMMERCE-409-TRACKING` | `tracking_number` trung |

## 6. Database & Events

- Write (PATCH): `shipments`, `order_items`, `shipment_status_history`, `outbox_events`
- Read (GET): `shipments`, `shipping_address_snapshots`, `order_items`
- Outbox: `COMMERCE_SHIPMENT_STATUS_CHANGED` → `commerce.shipment.status_changed`

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ManageSellerShipment.md`
- Flow: `docs/business_flow/commerce_business_flow/seller-order-management-flow.md` §10
