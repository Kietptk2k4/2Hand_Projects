# Functional Requirement - View Order Support Detail

## 1. Feature Overview

Cho phep admin support xem chi tiet order de ho tro tranh chap, van chuyen va thanh toan. Admin Service lay du lieu tu Commerce Service thong qua internal API/read integration.

## 2. Actors

- **Support Admin:** View order support detail.
- **Commerce Service:** Owns order data.

## 3. Scope

**In Scope:**

- View order status, buyer, shop, items, totals and timestamps.
- View support-relevant snapshots.
- Log access if policy requires.

**Out of Scope:**

- Direct order mutation.
- Direct Commerce DB access.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/support/orders/{orderId}`

**Auth:** Required, permission `ORDER_SUPPORT_VIEW`.

## 5. Business Rules

- Admin Service must not own order state.
- Response may aggregate Commerce internal API result.
- PII should be minimized and masked where possible.

## 6. Database Impact

- Optional insert `admin_action_logs` for sensitive access.
- No Commerce DB mutation.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Mask buyer contact fields unless permission allows full view.

## 9. Failure Cases

- Order not found -> 404.
- Commerce unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Support admin can view order details.
- Admin Service does not modify order.
- Access is permission-gated and auditable.

