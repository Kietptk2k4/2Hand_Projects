# View Payment Status – API & Behavior

## 1. Business Goal

Buyer xem trang thai thanh toan cua don hang (theo `paymentId`), gom method, amount, status, thoi gian paid/expired, checkout PayOS neu con hop le, va trang thai order lien quan. Read-only.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/payments/{paymentId}/status`
- **Auth:** Bearer JWT (buyer)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `paymentId` | UUID | ID ban ghi `payments` |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay trang thai thanh toan thanh cong.",
  "data": {
    "payment_id": "550e8400-e29b-41d4-a716-446655440000",
    "order_id": "660e8400-e29b-41d4-a716-446655440001",
    "payment_method": "PAYOS",
    "amount": 1050000,
    "currency": "VND",
    "status": "PENDING",
    "paid_at": null,
    "expired_at": "2026-05-21T11:00:00Z",
    "payos_checkout_url": "https://pay.payos.vn/web/...",
    "order_status": "AWAITING_PAYMENT",
    "order_payment_status": "PENDING"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

### Field notes

| Field | Mo ta |
|-------|-------|
| `status` | Trang thai `payments.status` |
| `paid_at` | Thoi diem thanh toan thanh cong (null neu chua paid) |
| `expired_at` | Han payment (tu `payments.expired_at`) |
| `payos_checkout_url` | Chi tra ve khi `status` = `PENDING`, `payment_method` = `PAYOS`, URL ton tai va `checkout_url_expired_at` > now; neu het han hoac da paid -> `null` |
| `order_status` | `orders.status` |
| `order_payment_status` | `orders.payment_status` |

**Khong** tra ve `provider_response` hay thong tin secret PayOS.

## 4. Server behavior

1. JWT `user_id` = `orders.buyer_id` (join `payments` + `orders`).
2. Load payment + order status read-only.
3. Loc checkout URL theo rule tren (khong expose URL het han).

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 404 | `COMMERCE-404-PAYMENT` | Payment khong ton tai hoac khong thuoc buyer |
| 401 | — | Thieu/invalid JWT |

## 6. FE guidance

- Sau redirect success/cancel tu PayOS, goi endpoint nay de hien thi trang thai hien tai.
- Neu `payos_checkout_url` = `null` va `status` = `PENDING`, co the goi `POST /payments/{paymentId}/payos-checkout-url` de tao URL moi.
- Dung `order_status` + `order_payment_status` de dieu huong man hinh order/payment.
