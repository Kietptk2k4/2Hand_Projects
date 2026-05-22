# Track Order Status – API & Behavior

## 1. Business Goal

Buyer theo doi trang thai order, payment, fulfillment (order items + shipments) va timeline lich su. Read-only; khong thay doi trang thai.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/orders/{orderId}/status`
- **Auth:** Bearer JWT (required)

Khong co request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay trang thai don hang thanh cong.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "orderStatus": "PROCESSING",
    "orderPaymentStatus": "PAID",
    "paymentMethod": "PAYOS",
    "totalAmount": 1000000,
    "finalAmount": 1000000,
    "createdAt": "2026-05-21T10:00:00Z",
    "updatedAt": "2026-05-21T14:00:00Z",
    "completedAt": null,
    "orderCompleted": false,
    "paymentPaid": true,
    "allItemsCompleted": false,
    "anyShipmentDelivered": true,
    "anyItemDelivered": true,
    "payment": {
      "paymentId": "...",
      "status": "PAID",
      "paymentMethod": "PAYOS",
      "paidAt": "2026-05-21T10:05:00Z",
      "expiredAt": null,
      "timeline": [
        { "oldStatus": "PENDING", "newStatus": "PAID", "occurredAt": "..." }
      ]
    },
    "items": [
      {
        "orderItemId": "...",
        "productId": "...",
        "sellerId": "...",
        "productName": "iPhone 15",
        "quantity": 1,
        "status": "DELIVERED",
        "shipmentId": "...",
        "completedAt": null
      }
    ],
    "shipments": [
      {
        "shipmentId": "...",
        "sellerId": "...",
        "status": "DELIVERED",
        "carrier": "GHN",
        "trackingNumber": "TRACK-001",
        "shippedAt": "...",
        "deliveredAt": "...",
        "timeline": []
      }
    ],
    "orderTimeline": [
      {
        "oldStatus": "AWAITING_PAYMENT",
        "newStatus": "PROCESSING",
        "changedBy": "SYSTEM",
        "note": null,
        "occurredAt": "..."
      }
    ]
  }
}
```

## 4. Derived flags (FE)

| Field | Y nghia |
|-------|---------|
| `orderCompleted` | `orderStatus == COMPLETED` |
| `paymentPaid` | `payment.status == PAID` |
| `allItemsCompleted` | Moi `items[].status == COMPLETED` |
| `anyShipmentDelivered` | Co shipment `DELIVERED` |
| `anyItemDelivered` | Co order item `DELIVERED` |

**Phan biet quan trong:**

- Shipment `DELIVERED` **khong** dong nghia order `COMPLETED`.
- Order `COMPLETED` khi tat ca items `COMPLETED` **va** payment `PAID` (confirm-received / auto-complete flow).

## 5. FE Behavior

- Chi goi voi order cua buyer (JWT `user_id`).
- Dung `orderTimeline`, `payment.timeline`, `shipments[].timeline` de ve timeline.
- Nut "Da nhan hang" khi `anyItemDelivered` va order chua `orderCompleted` — goi `POST .../confirm-received`.
- Refresh dinh ky khi order dang `PROCESSING`.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-ORDER` | Order khong ton tai hoac khong thuoc buyer |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_TrackOrderStatus.md`
- Confirm received: `ConfirmOrderReceived-api-and-behavior.md`
- Track shipment (seller/buyer per shipment): `FR_TrackShipment.md`
