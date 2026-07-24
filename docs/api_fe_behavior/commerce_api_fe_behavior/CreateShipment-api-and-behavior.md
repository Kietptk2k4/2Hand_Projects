# Create Shipment – API & Behavior

## 1. Business Goal

Seller tao shipment cho order items cua shop sau khi order o trang thai `PROCESSING`. Ho tro GHN, manual va self-delivery.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/shipments`
- **Auth:** Bearer JWT (seller — `user_id` = `shipments.seller_id`)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `order_id` | UUID | yes | Don hang |
| `order_item_ids` | UUID[] | yes | Items cua seller, chua co shipment |
| `carrier` | string | yes | `GHN`, `MANUAL`, `SELF_DELIVERY` |
| `shipment_type` | string | yes | `STANDARD`, `EXPRESS`, `SAME_DAY` |
| `weight_gram` | integer | no | Neu bo trong, he thong tinh tu product |
| `tracking_number` | string | no | Manual/self-delivery (GHN lay tu provider) |

```json
{
  "order_id": "550e8400-e29b-41d4-a716-446655440000",
  "order_item_ids": ["660e8400-e29b-41d4-a716-446655440001"],
  "carrier": "GHN",
  "shipment_type": "STANDARD",
  "weight_gram": 500
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao shipment thanh cong.",
  "data": {
    "shipment_id": "770e8400-e29b-41d4-a716-446655440002",
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "880e8400-e29b-41d4-a716-446655440003",
    "carrier": "GHN",
    "shipment_type": "STANDARD",
    "status": "PENDING",
    "ghn_order_code": "GHN-MOCK-770E8400",
    "tracking_number": "TRK-ABC123",
    "shipping_fee": 20000,
    "cod_amount": 0,
    "weight_gram": 500,
    "estimated_delivery_date": "2026-05-24",
    "order_item_ids": ["660e8400-e29b-41d4-a716-446655440001"],
    "created_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. FE Behavior

- Chi goi sau khi order `PROCESSING` va (PayOS da `PAID` hoac COD cho phep `PENDING`).
- `seller_id` khong gui tu client — lay tu JWT.
- GHN: `ghn_order_code` / `tracking_number` co sau khi provider tra ve (mock khi `COMMERCE_GHN_MOCK_FALLBACK_ENABLED=true`).
- GHN: `estimated_delivery_date` uu tien parse tu Create Order `expected_delivery_time`; neu thieu/loi parse giu heuristic luc insert.
- Dia chi giao: buyer default `user_addresses` (MVP; chua snapshot tai checkout).

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Payload khong hop le |
| 400 | `COMMERCE-400-SHIPMENT-CARRIER` | Carrier khong hop le |
| 400 | `COMMERCE-400-SHIPMENT-TYPE` | Shipment type khong hop le |
| 403 | `COMMERCE-403-ORDER-ITEM` | Item khong thuoc seller |
| 404 | `COMMERCE-404-ORDER` | Order khong ton tai |
| 404 | `COMMERCE-404-ORDER-ITEM` | Item khong ton tai trong order |
| 404 | `COMMERCE-404-BUYER-ADDRESS` | Buyer chua co dia chi |
| 409 | `COMMERCE-409-ORDER-PROCESSING` | Order khong o `PROCESSING` |
| 409 | `COMMERCE-409-ORDER-ITEM-SHIPPED` | Item da co shipment |
| 409 | `COMMERCE-409-PAYMENT-STATE` | PayOS chua `PAID` |
| 409 | `COMMERCE-409-SHIPPING-PROFILE` | Thieu seller shipping profile |
| 503 | `COMMERCE-503-GHN` | GHN khong kha dung (khong mock) |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_CreateShipment.md`
- Outbox: `COMMERCE_SHIPMENT_CREATED` → `commerce.shipment.created`
