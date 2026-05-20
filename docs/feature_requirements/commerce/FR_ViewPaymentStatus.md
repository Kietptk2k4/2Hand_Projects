# Functional Requirement - View Payment Status

## 1. Feature Overview

Cho phep buyer xem trang thai payment cua order minh, bao gom method, amount, status, paid/expired time va checkout URL neu payOS payment con pending.

## 2. Actors

- **Buyer:** Xem payment status.
- **System:** Load payment/order status.

## 3. Scope

**In Scope:**

- View payment status by payment id/order id.
- Include order payment status.
- Include valid checkout URL for pending payOS payment if available.

**Out of Scope:**

- Create payment.
- Process webhook.
- Refund.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/payments/{paymentId}/status`

**Auth:** Required (JWT)

**Response data:**

- `payment_id`
- `order_id`
- `payment_method`
- `amount`
- `currency`
- `status`
- `paid_at`
- `expired_at`
- `payos_checkout_url` nullable
- `order_status`
- `order_payment_status`

## 5. Business Rules

- Buyer can view only own payment.
- Seller cannot see provider payment details.
- Checkout URL only returned if payment pending and URL not expired.
- Redirect success/cancel should call this endpoint to display final/current state.

## 6. Database Impact

- Read `payments`.
- Read `orders`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Ownership check by order buyer.
- Do not expose provider response/secrets.

## 9. Failure Cases

- Payment not found/not owned -> 404.

## 10. Acceptance Criteria

- Buyer sees accurate payment status.
- Other buyer payment inaccessible.
- Expired checkout URL is not returned as usable.
- Provider response is not exposed.

