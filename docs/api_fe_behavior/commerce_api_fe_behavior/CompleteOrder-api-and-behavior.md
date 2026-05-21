# Complete Order – API & Behavior

## 1. Business Goal

Hoan tat order khi **tat ca** `order_items` da `COMPLETED` va `payment_status = PAID`. Use case noi bo, duoc goi tu auto-complete job, confirm-received (future), hoac admin API tuy chon.

## 2. API Contract (optional – admin)

- **Method:** POST
- **URL:** `/commerce/api/v1/orders/{orderId}/complete`
- **Auth:** Bearer JWT voi role **ADMIN**

### Request body (optional)

| Field    | Type   | Required | Mo ta        |
|----------|--------|----------|--------------|
| `reason` | string | no       | Ly do audit  |

Buyer **khong** dung API nay; dung `POST .../confirm-received` (FR rieng).

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Hoan tat don hang thanh cong.",
  "data": {
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "order_status": "COMPLETED",
    "completed_at": "2026-05-21T15:00:00.123Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T15:00:00.123Z"
}
```

Idempotent: order da `COMPLETED` → 200, message `Don hang da hoan tat truoc do.`

## 4. Response – Error

| HTTP | Code string                          | Mo ta                                                |
|------|--------------------------------------|------------------------------------------------------|
| 401  | `COMMERCE-401`                       | Khong co JWT                                         |
| 403  | `COMMERCE-403`                       | Khong co role ADMIN                                  |
| 404  | `COMMERCE-404-ORDER`                 | Order khong ton tai                                  |
| 409  | `COMMERCE-409-ORDER-NOT-COMPLETABLE` | Chua du dieu kien (items/payment)                    |

## 5. Business Rules

Order duoc complete **chi khi**:

- Moi `order_items.status IN (COMPLETED, CANCELLED)` (khong con PENDING/PROCESSING/DELIVERED/...)
- `orders.payment_status = PAID` (hoac `payments.status = PAID`)

Khong du:

- Shipment `DELIVERED` thoi **chua du** — can item `COMPLETED` + payment `PAID`.
- Order `AWAITING_PAYMENT` — chi complete tu `PROCESSING`.

Khi transition:

- `orders.status` → `COMPLETED`
- `orders.completed_at` set mot lan
- `order_status_history` + outbox `COMMERCE_ORDER_COMPLETED`

## 6. Internal Usage

| Caller                    | Cach goi                                      |
|---------------------------|-----------------------------------------------|
| Auto-complete job         | `OrderCompletionRepository` sau khi complete items + COD paid |
| `CompleteOrderUseCase.tryComplete` | Khong throw — tra `CompleteOrderOutcome`      |
| Admin API                 | `CompleteOrderUseCase.execute`                |
| Confirm received (future) | `tryComplete` sau khi buyer confirm items     |

## 7. Edge Cases

- Goi complete 2 lan → idempotent, khong duplicate outbox neu da completed.
- Mot so item van `DELIVERED` → `NOT_ELIGIBLE`.
- PayOS chua paid → `NOT_ELIGIBLE`.

## 8. Data Dependencies

- Read: `orders`, `order_items`, `payments`
- Write: `orders`, `order_status_history`, `outbox_events`

## 9. FE / Ops Notes

- Buyer flow: khong goi `/complete`; cho confirm-received UI.
- Admin/support: chi dung khi can force-check completion sau khi data da dung.
