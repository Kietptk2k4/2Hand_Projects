# Functional Requirement - View Seller Orders

## 1. Feature Overview

Cho phep seller xem danh sach order items va shipment summary thuoc shop minh. Trong multi-seller order, seller chi thay phan order cua minh.

## 2. Actors

- **Seller:** Xem order items can fulfillment.
- **System:** Query seller-scoped order data.

## 3. Scope

**In Scope:**

- List seller-owned order items.
- Filter by item status, shipment status, date.
- Include order/payment/shipment summary safe for seller.

**Out of Scope:**

- Buyer full order detail.
- Admin support search.
- Payment provider detail.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/seller/orders`

**Auth:** Required (JWT seller)

**Query params:**

- `status`
- `shipment_status`
- `page` / `cursor`
- `limit`

## 5. Business Rules

- Seller sees only `order_items.seller_id` of own shop/user.
- Seller cannot see items of other sellers in same order.
- Include only payment status summary, not provider details.
- Sort newest first by default.

## 6. Database Impact

- Read `seller_shops`.
- Read `order_items`.
- Read `orders`.
- Read `payments` summary.
- Read `shipments` summary.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- Seller ownership scope mandatory.

## 9. Failure Cases

- Seller has no shop -> 404/409.
- Invalid filter/pagination -> 400.

## 10. Acceptance Criteria

- Seller sees only own order items.
- Other seller items are never returned.
- Response includes fulfillment-relevant summary.

