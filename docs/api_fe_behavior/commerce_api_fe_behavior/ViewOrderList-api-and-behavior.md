# View Order List – API & Behavior

## 1. Business Goal

Buyer xem lich su don hang voi tom tat status, payment, amount, so luong item, preview san pham va shipment. Read-only, phan trang.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/orders`
- **Auth:** Bearer JWT (buyer)

### Query params

| Param | Type | Required | Default | Mo ta |
|-------|------|----------|---------|-------|
| `page` | int | no | 1 | Trang (>= 1) |
| `limit` | int | no | 20 | So ban ghi/trang (1–50) |
| `status` | string | no | — | Loc `orders.status`: `CREATED`, `AWAITING_PAYMENT`, `PROCESSING`, `COMPLETED`, `CANCELLED` |

Vi du: `GET /commerce/api/v1/orders?page=1&limit=10&status=PROCESSING`

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach don hang thanh cong.",
  "data": {
    "orders": [
      {
        "order_id": "550e8400-e29b-41d4-a716-446655440000",
        "order_status": "PROCESSING",
        "order_payment_status": "PAID",
        "payment_method": "PAYOS",
        "total_amount": 1050000,
        "final_amount": 1050000,
        "created_at": "2026-05-21T10:00:00Z",
        "updated_at": "2026-05-21T14:00:00Z",
        "completed_at": null,
        "item_count": 2,
        "preview_product_name": "iPhone 15",
        "preview_image_url": "http://localhost:9000/2hands-commerce-product/p1.jpg",
        "payment": {
          "payment_id": "...",
          "status": "PAID",
          "payment_method": "PAYOS",
          "amount": 1050000,
          "currency": "VND"
        },
        "shipment_summary": {
          "shipment_count": 1,
          "statuses": ["DELIVERED"]
        }
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 1,
      "total_pages": 1,
      "has_next": false
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T14:00:01Z"
}
```

- Sap xep mac dinh: **`created_at` DESC** (moi nhat truoc).
- `preview_*`: item dau tien theo `order_items.created_at` (snapshot luc mua).
- Khong co order -> `orders: []`, `total_items: 0`.

## 4. Server behavior

1. JWT `user_id` = `orders.buyer_id`.
2. Dem + query orders (optional filter `status`).
3. Batch load: item count/preview, `payments`, `shipments` aggregate.
4. Khong doc gia/ten san pham hien tai tu `products`.

Chi tiet day du: `GET /orders/{orderId}`. Tracking timeline: `GET /orders/{orderId}/status`.

## 5. FE Behavior

- Man danh sach don: goi GET khi load / pull-to-refresh.
- Order card: hien `preview_image_url`, `preview_product_name`, `order_status`, `final_amount`.
- Phan trang: dung `pagination.has_next` / `total_pages`.
- Tap card -> `GET /orders/{orderId}`.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-PAGINATION` | `page` hoac `limit` khong hop le |
| 400 | `COMMERCE-400-VALIDATION` | `status` khong hop le |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewOrderList.md`
- Detail: `ViewOrderDetail-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-checkout-order.md` § View Order List
