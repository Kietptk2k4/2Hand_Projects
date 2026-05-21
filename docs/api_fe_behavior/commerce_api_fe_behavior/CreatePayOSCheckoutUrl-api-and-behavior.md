# Create PayOS Checkout URL – API & Behavior

## 1. Business Goal

Buyer lay link thanh toan payOS cho payment `PENDING` cua order `AWAITING_PAYMENT`. Redirect URL **khong** danh dau paid — chi webhook hop le moi cap nhat payment.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/payments/{paymentId}/payos-checkout-url`
- **Auth:** Bearer JWT (required)
- **Request body:** Khong

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao link thanh toan payOS thanh cong.",
  "data": {
    "payment_id": "660e8400-e29b-41d4-a716-446655440001",
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "payos_order_code": "1747812345678",
    "payos_checkout_url": "https://pay.payos.vn/web/...",
    "checkout_url_expired_at": "2026-05-21T10:30:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

Neu URL con han, `message` la `"Lay lai link thanh toan payOS thanh cong."`.

## 4. FE Behavior

1. Checkout `PAYOS` → `payos_checkout_url` co the `null` — goi API nay de lay URL.
2. Redirect buyer toi `payos_checkout_url`.
3. Sau redirect, **khong** gia dinh da thanh toan — poll order/payment status hoac cho webhook.
4. Retry API khi URL het han hoac loi provider.

## 5. Business Rules

- Chi buyer so huu order (join `orders.buyer_id`).
- `payment_method = PAYOS`, `payment.status = PENDING`, `order.status = AWAITING_PAYMENT`.
- URL con han (`checkout_url_expired_at > now`) → tra lai URL cu, khong goi payOS lai.
- Luu `payos_order_code`, `payos_checkout_url`, `provider_response`.
- Local/dev: `COMMERCE_PAYOS_MOCK_FALLBACK_ENABLED=true` tao URL mock khi PayOS chua cau hinh.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `UNAUTHORIZED` | Thieu JWT |
| 404 | `COMMERCE-404-PAYMENT` | Payment khong ton tai / khong thuoc buyer |
| 409 | `COMMERCE-409-PAYMENT-STATE` | Payment khong `PENDING` |
| 409 | `COMMERCE-409-ORDER-AWAITING-PAYMENT` | Order khong `AWAITING_PAYMENT` |
| 409 | `COMMERCE-400-PAYMENT-METHOD` | Khong phai PAYOS |
| 503 | `COMMERCE-503-PAYOS` | PayOS loi va mock fallback tat |

## 7. Env (PayOS live)

| Bien | Mo ta |
|------|--------|
| `COMMERCE_PAYOS_ENABLED` | Bat tich hop |
| `COMMERCE_PAYOS_CLIENT_ID` | x-client-id |
| `COMMERCE_PAYOS_API_KEY` | x-api-key |
| `COMMERCE_PAYOS_CHECKSUM_KEY` | HMAC signature |
| `COMMERCE_PAYOS_RETURN_URL` | returnUrl |
| `COMMERCE_PAYOS_CANCEL_URL` | cancelUrl |
| `COMMERCE_PAYOS_MOCK_FALLBACK_ENABLED` | Mock khi API fail / chua config |

## 8. Related

- Checkout: `CheckoutFromCart-api-and-behavior.md`
- FR: `docs/feature_requirements/commerce/FR_CreatePayOSCheckoutUrl.md`
