# Functional Requirement - Process Seller Order Item

## 1. Feature Overview

Cho phep seller chuyen order items cua shop minh sang trang thai dang xu ly/chuan bi hang khi order da san sang fulfillment.

## 2. Actors

- **Seller:** Xac nhan chuan bi hang.
- **System:** Validate order/payment readiness and update item status.

## 3. Scope

**In Scope:**

- Mark seller-owned order items `PROCESSING`.
- Write outbox event/status update.

**Out of Scope:**

- Create shipment.
- Cancel/refund order.
- Complete order item.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/order-items/process`

**Auth:** Required (JWT seller)

**Request body:**

- `order_item_ids`

## 5. Business Rules

- Seller owns all selected order items.
- Parent order must be `PROCESSING`.
- For payOS, order payment must be `PAID`.
- Items must be `PENDING`.
- Seller cannot process cancelled/failed/completed items.

## 6. Database Impact

- Read `seller_shops`.
- Read/update `order_items`.
- Read `orders`, `payments`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Seller ownership check mandatory.

## 9. Failure Cases

- Item not found/not owned -> 404.
- Order not processing -> 409.
- Invalid item status -> 409.

## 10. Acceptance Criteria

- Seller can mark own pending items as processing.
- Items from other sellers rejected.
- Completed/cancelled items cannot be processed.

