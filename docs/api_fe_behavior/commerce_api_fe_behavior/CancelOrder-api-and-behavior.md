# Cancel Order – API & Behavior

## 1. Business Goal

Cho phep buyer huy don hang khi chua bat dau fulfillment. He thong release reserved inventory neu payment chua thanh cong.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/orders/{orderId}/cancel`
- **Auth:** Bearer JWT (required)

### Path params

| Field      | Type | Required | Mo ta        |
|------------|------|----------|--------------|
| `orderId`  | UUID | yes      | ID don hang  |

### Request body (optional)

| Field    | Type   | Required | Mo ta                |
|----------|--------|----------|----------------------|
| `reason` | string | no       | Ly do huy (audit log) |

```json
{
  "reason": "Khong con nhu cau mua"
}
```

Body co the bo trong `{}` hoac khong gui body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Huy don hang thanh cong.",
  "data": {
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "CANCELLED",
    "cancelled_at": "2026-05-21T12:00:00.123Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T12:30:00.123Z"
}
```

Idempotent: neu order da `CANCELLED`, van tra **200** voi message `Don hang da duoc huy truoc do.`

## 4. Response – Error

| HTTP | Code string                          | Mo ta                                              |
|------|--------------------------------------|----------------------------------------------------|
| 401  | `COMMERCE-401`                       | Khong co JWT                                       |
| 404  | `COMMERCE-404-ORDER`                 | Order khong ton tai hoac khong thuoc buyer         |
| 409  | `COMMERCE-409-ORDER-NOT-CANCELLABLE` | Order/shipment/payment khong cho phep huy          |
| 500  | `COMMERCE-500`                       | Loi server (vd: inventory release conflict)        |

## 5. Business Rules

- Buyer chi huy order cua minh (`buyer_id` = JWT `user_id`).
- Cho phep khi `orders.status IN (CREATED, AWAITING_PAYMENT)` va `payment_status = PENDING`.
- Shipment khong ton tai hoac tat ca shipment `PENDING` / `CANCELLED`.
- Khong cho huy neu shipment da `PICKING_UP`, `READY_TO_SHIP`, `SHIPPED`, `DELIVERED`, ...
- Payment `PENDING` → `CANCELLED`; order `payment_status` → `CANCELLED`.
- Order items `PENDING` → `CANCELLED`.
- Release inventory: `reserved_quantity` giam, `stock_quantity` tang (moi item pending).
- Ghi `order_status_history`, `payment_status_history`.
- Outbox: `COMMERCE_ORDER_CANCELLED`, `COMMERCE_INVENTORY_RELEASED` (neu co items pending).
- Khong ho tro refund sau khi da thanh toan / da ship.

## 6. Edge Cases

- `reason` null/blank → luu note mac dinh `BUYER_CANCELLED`.
- Huy lan 2 → idempotent 200.
- Order `PROCESSING` / payment `PAID` → 409.
- Inventory release fail (race) → 500.

## 7. Data Dependencies

- Write: `orders`, `order_items`, `payments`, `product_inventories`, `order_status_history`, `payment_status_history`, `outbox_events`.
- Read: `shipments` (check blocking status).

## 8. FE Integration Notes

- Chi hien nut "Huy don" khi order con `CREATED` / `AWAITING_PAYMENT` va chua co shipment active.
- Sau khi huy thanh cong, refresh order detail / list.
- Khong dung API nay cho refund sau thanh toan.
