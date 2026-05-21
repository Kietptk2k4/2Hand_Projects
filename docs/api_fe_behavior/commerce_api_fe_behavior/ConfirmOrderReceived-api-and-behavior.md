# Confirm Order Received – API & Behavior

## 1. Business Goal

Buyer xac nhan da nhan hang. He thong chuyen cac `order_items` dang `DELIVERED` sang `COMPLETED`, voi **COD** danh dau payment `PAID`, va complete order neu du dieu kien (tat ca items completed + payment paid).

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/orders/{orderId}/confirm-received`
- **Auth:** Bearer JWT (required)
- **Body:** khong bat buoc

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Xac nhan da nhan hang thanh cong.",
  "data": {
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "order_status": "COMPLETED",
    "payment_status": "PAID",
    "items_completed": 2,
    "payment_marked_paid": true,
    "order_completed": true
  },
  "errors": null,
  "timestamp": "2026-05-21T16:00:00.123Z"
}
```

Idempotent: order da `COMPLETED` → 200, message `Don hang da duoc xac nhan nhan hang truoc do.`

## 4. Response – Error

| HTTP | Code string                     | Mo ta                                              |
|------|---------------------------------|----------------------------------------------------|
| 401  | `COMMERCE-401`                  | Khong co JWT                                       |
| 404  | `COMMERCE-404-ORDER`            | Order khong ton tai hoac khong thuoc buyer         |
| 409  | `COMMERCE-409-ORDER-ITEMS`      | Khong co order item nao dang `DELIVERED`           |
| 409  | `COMMERCE-409-PAYMENT-STATE`    | PayOS chua paid / payment state khong hop le      |
| 409  | `COMMERCE-409-ORDER-NOT-CANCELLABLE` | Order da `CANCELLED`                          |

## 5. Business Rules

- Chi buyer (`orders.buyer_id` = JWT `user_id`) moi confirm duoc.
- Confirm tat ca `order_items` dang `DELIVERED` cua order (multi-seller: confirm het delivered items hien co).
- **COD:** `payments.status` `PENDING` → `PAID`, `orders.payment_status` → `PAID`.
- **PayOS:** payment phai da `PAID` truoc khi confirm (da thanh toan truoc khi ship).
- Sau khi items completed + payment paid → goi `CompleteOrder` (order `PROCESSING` → `COMPLETED`).
- Outbox: `COMMERCE_PAYMENT_PAID` (COD), `COMMERCE_ORDER_COMPLETED` (neu order complete).
- Shipment `DELIVERED` **khong du** — item phai `DELIVERED` truoc.

## 6. Edge Cases

- Mot phan items `DELIVERED`, phan con `SHIPPED` → chi complete delivered; order complete khi **tat ca** items completed.
- Confirm 2 lan khi order da completed → idempotent 200.
- Khong co item `DELIVERED` → 409.

## 7. Data Dependencies

- Read/update: `orders`, `order_items`, `payments`, `order_status_history`, `payment_status_history`, `outbox_events`.

## 8. FE Integration Notes

- Hien nut "Da nhan hang" khi co item `DELIVERED` (hoac shipment delivered).
- Sau success refresh order detail; `order_status` co the la `COMPLETED`.
- Khong dung endpoint admin `/complete` — day la flow buyer.
