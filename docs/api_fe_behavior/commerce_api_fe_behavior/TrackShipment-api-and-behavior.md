# Track Shipment – API & Behavior

## 1. Business Goal

Buyer hoac seller theo doi trang thai van chuyen: status hien tai, tracking number, carrier va timeline. Read-only; khong poll provider.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/shipments/{shipmentId}/tracking`
- **Auth:** Bearer JWT (required)

Khong co request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay thong tin tracking shipment thanh cong.",
  "data": {
    "shipmentId": "...",
    "orderId": "...",
    "sellerId": "...",
    "accessedAs": "BUYER",
    "status": "DELIVERED",
    "carrier": "GHN",
    "shipmentType": "STANDARD",
    "trackingNumber": "TRACK-001",
    "ghnOrderCode": "GHN-123",
    "shippedAt": "...",
    "deliveredAt": "...",
    "estimatedDeliveryDate": "2026-05-25",
    "orderStatus": "PROCESSING",
    "shipmentDelivered": true,
    "orderCompleted": false,
    "timeline": [
      {
        "oldStatus": "SHIPPED",
        "newStatus": "DELIVERED",
        "rawStatus": "delivered",
        "occurredAt": "..."
      }
    ]
  }
}
```

## 4. Access control

User duoc phep neu **mot trong hai**:

- JWT `user_id` = `orders.buyer_id` (buyer cua order chua shipment)
- JWT `user_id` = `shipments.seller_id` (seller tao shipment)

Nguoi khac → **404** `COMMERCE-404-SHIPMENT` (khong lo ton tai shipment).

`accessedAs`: `BUYER` hoac `SELLER` — role duoc resolve tu ownership.

## 5. Derived flags

| Field | Y nghia |
|-------|---------|
| `shipmentDelivered` | `status == DELIVERED` |
| `orderCompleted` | `orderStatus == COMPLETED` |

**Khong gop hai khai niem:** shipment delivered ≠ order completed. Buyer confirm nhan hang qua `POST /orders/{orderId}/confirm-received`.

## 6. Timeline

- Doc tu `shipment_status_history`, sap xep `created_at` ASC.
- `rawStatus`: ma trang thai provider (GHN) khi co — **khong** tra `external_provider_response` JSON.

## 7. FE Behavior

- Buyer: link tu order detail / track order.
- Seller: link tu seller shipment detail (`GET /seller/shipments/{id}` cho full detail; endpoint nay cho tracking timeline).
- Hien tracking number + carrier; copy tracking cho GHN/MANUAL.

## 8. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-SHIPMENT` | Shipment khong ton tai hoac user khong co quyen |

## 9. Related

- FR: `docs/feature_requirements/commerce/FR_TrackShipment.md`
- Track order: `TrackOrderStatus-api-and-behavior.md`
- Seller view: `GET /commerce/api/v1/seller/shipments/{shipmentId}`
