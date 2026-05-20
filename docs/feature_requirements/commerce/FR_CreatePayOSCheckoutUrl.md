# Functional Requirement - Create PayOS Checkout URL

## 1. Feature Overview

Cho phep buyer tao hoac lay lai checkout URL payOS cho payment pending cua order. Redirect URL chi la huong dan thanh toan; payment paid phai den tu webhook hop le.

## 2. Actors

- **Buyer:** Yeu cau URL thanh toan payOS.
- **System:** Validate payment va call payOS.
- **payOS:** Provider tao payment link.

## 3. Scope

**In Scope:**

- Create payOS payment link.
- Reuse existing non-expired checkout URL.
- Store provider response.

**Out of Scope:**

- Webhook processing.
- Mark payment paid from redirect.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/payments/{paymentId}/payos-checkout-url`

**Auth:** Required (JWT)

**Response data:**

- `payment_id`
- `order_id`
- `payos_order_code`
- `payos_checkout_url`
- `checkout_url_expired_at`

## 5. Business Rules

- Buyer can request only own payment.
- Payment method must be `PAYOS`.
- Payment status must be `PENDING`.
- Order should be `AWAITING_PAYMENT`.
- Existing valid checkout URL should be reused.
- payOS failure keeps payment pending.

## 6. Database Impact

- Read `payments`, `orders`.
- Update payOS fields in `payments`.
- Store `provider_response`.

## 7. Transaction

- Short write transaction for saving provider response.
- Avoid long DB transaction while waiting external provider if possible.

## 8. Security

- JWT required.
- Ownership check through order buyer.
- Do not log provider secrets.

## 9. Failure Cases

- Payment not found/not owned -> 404.
- Payment not pending -> 409.
- Payment method not PAYOS -> 409.
- payOS unavailable -> 502/503.

## 10. Acceptance Criteria

- Pending payOS payment returns checkout URL.
- Existing valid URL is reused.
- Redirect does not mark payment paid.
- Provider response is stored for debug.

