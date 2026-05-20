# Functional Requirement - View Order List

## 1. Feature Overview

Cho phep buyer xem danh sach order cua minh, gom thong tin tom tat ve status, payment, amount va shipment.

## 2. Actors

- **Buyer:** Xem lich su order.
- **System:** Query order summaries.

## 3. Scope

**In Scope:**

- List orders by buyer.
- Pagination.
- Include payment/order/shipment summary.

**Out of Scope:**

- Seller order list.
- Admin order search.
- Order mutation.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/orders`

**Auth:** Required (JWT)

**Query params:**

- `page` / `cursor`
- `limit`
- optional `status`

## 5. Business Rules

- Buyer sees only own orders.
- Sort newest first by default.
- Include enough summary for order card.

## 6. Database Impact

- Read `orders`.
- Read `order_items` summary.
- Read `payments` summary.
- Read `shipments` summary.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Filter by `buyer_id` from JWT.

## 9. Failure Cases

- Invalid pagination/status -> 400.
- No orders -> empty list.

## 10. Acceptance Criteria

- Buyer sees only own orders.
- Order list is paginated.
- Each order has status/payment/amount summary.

