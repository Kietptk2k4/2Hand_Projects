# View Order Detail – API & Behavior

## 1. Business Goal

Buyer xem chi tiet don hang: header, item **snapshots** (gia/ten/anh tai thoi diem mua), payment, shipments, dia chi giao snapshot va timeline. Read-only.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/orders/{orderId}`
- **Auth:** Bearer JWT (buyer)

Khong co request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay chi tiet don hang thanh cong.",
  "data": {
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "buyer_id": "660e8400-e29b-41d4-a716-446655440001",
    "order_status": "PROCESSING",
    "order_payment_status": "PAID",
    "payment_method": "PAYOS",
    "total_amount": 1050000,
    "final_amount": 1050000,
    "created_at": "2026-05-21T10:00:00Z",
    "updated_at": "2026-05-21T14:00:00Z",
    "completed_at": null,
    "payment": {
      "payment_id": "...",
      "status": "PAID",
      "payment_method": "PAYOS",
      "amount": 1050000,
      "currency": "VND",
      "paid_at": "2026-05-21T10:05:00Z",
      "expired_at": null,
      "checkout_url_expired_at": null,
      "timeline": []
    },
    "items": [
      {
        "order_item_id": "...",
        "product_id": "...",
        "seller_id": "...",
        "shipment_id": "...",
        "quantity": 1,
        "status": "DELIVERED",
        "unit_price_snapshot": 1000000,
        "final_price": 1000000,
        "sku_snapshot": "SKU-001",
        "product_name_snapshot": "iPhone 15",
        "image_snapshot": "http://localhost:9000/2hands-commerce-product/p1.jpg",
        "attributes_snapshot": "{\"color\":\"black\"}",
        "shop_name_snapshot": "Tech Shop",
        "shipping_fee_allocated": 50000,
        "completed_at": null
      }
    ],
    "shipments": [
      {
        "shipment_id": "...",
        "seller_id": "...",
        "status": "DELIVERED",
        "carrier": "GHN",
        "tracking_number": "TRACK-001",
        "shipping_fee": 50000,
        "shipment_type": "STANDARD",
        "estimated_delivery_date": null,
        "shipped_at": "...",
        "delivered_at": "...",
        "shipping_address": {
          "receiver_name": "Nguyen Van A",
          "phone": "0901234567",
          "province_code": "79",
          "district_code": "760",
          "ward_code": "26734",
          "address_detail": "123 Nguyen Van Linh",
          "full_address": "123 Nguyen Van Linh, Q.7, TP.HCM"
        },
        "timeline": []
      }
    ],
    "order_timeline": []
  },
  "errors": null,
  "timestamp": "2026-05-21T14:00:01Z"
}
```

## 4. Server behavior

1. JWT `user_id` = `orders.buyer_id`.
2. Load order, `order_items` (snapshot columns), `payments`, `shipments`, `shipping_address_snapshots`, status histories.
3. **Khong** doc gia/ten san pham hien tai tu `products` — chi snapshot.
4. Khong thay doi inventory hay order state.

## 5. FE Behavior

- Man order detail: `GET /orders/{orderId}`.
- Tracking timeline: co the dung them `GET /orders/{orderId}/status` neu can flags `paymentPaid`, `anyShipmentDelivered`, ...
- Hien anh tu `image_snapshot` (URL immutable luc checkout).

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-ORDER` | Order khong ton tai hoac khong thuoc buyer |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewOrderDetail.md`
- Track status: `TrackOrderStatus-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-checkout-order.md` § View Order Detail
