# Auto Cancel Unpaid Order – API & Behavior

## 1. Business Goal

System job tu dong huy order payOS chua thanh toan sau khi het han (`expired_at`, `checkout_url_expired_at`, hoac order TTL). Job release reserved inventory, ghi status histories va outbox events. **Khong co public HTTP API.**

## 2. API Contract

Khong ap dung — day la **scheduled/internal job**.

| Muc          | Gia tri |
|--------------|---------|
| Trigger      | Spring `@Scheduled` |
| Component    | `AutoCancelUnpaidOrderScheduler` |
| Use case     | `AutoCancelUnpaidOrdersUseCase` |

## 3. Response – Success

Khong co HTTP response. Job log metrics:

- `candidates` — so order tim thay trong batch
- `cancelled` — so order da cancel thanh cong
- `skipped` — no-op (da paid, shipment started, ...)
- `failed` — exception khi xu ly tung order
- `elapsedMs` — thoi gian chay job

## 4. Response – Error

Loi xu ly tung order duoc log va dem vao `failed`; job khong dung toan batch.

| Tinh huong | Hanh vi |
|------------|---------|
| Payment da `PAID` | Skip (no-op) |
| Order da terminal | Skip |
| Shipment da bat dau | Skip + log |
| Inventory release conflict | Rollback transaction order do + log error |

## 5. Business Rules

- Chi xu ly `payments.payment_method = PAYOS`.
- **Khong** auto-cancel COD chi vi qua han.
- Order: `status IN (CREATED, AWAITING_PAYMENT)`, `payment_status = PENDING`.
- Payment: `status = PENDING`.
- Het han khi: `expired_at < now` OR `checkout_url_expired_at < now` OR `orders.created_at < now - orderTtlMinutes`.
- Khong cancel neu shipment co status ngoai `PENDING`, `CANCELLED`.
- Transition: payment `PENDING → EXPIRED`; order `→ CANCELLED`, `payment_status → EXPIRED`; order items `PENDING → CANCELLED`.
- Release inventory: `reserved_quantity -= q`, `stock_quantity += q` (mot lan, idempotent qua conditional update).
- Outbox: `COMMERCE_PAYMENT_EXPIRED`, `COMMERCE_ORDER_CANCELLED`, `COMMERCE_INVENTORY_RELEASED` (neu co items).

## 6. Edge Cases

- Webhook payOS paid truoc job → payment khong con `PENDING` → skip.
- Job retry cung order → conditional update order/payment → no-op.
- Order khong co pending items → van cancel payment/order, khong emit inventory event.
- Partial inventory release → throw, rollback toan bo order transaction.

## 7. Data Dependencies

| Table                    | Action        |
|--------------------------|---------------|
| `orders`                 | read/update   |
| `payments`               | read/update   |
| `order_items`            | update        |
| `product_inventories`    | update        |
| `shipments`              | read (guard)  |
| `order_status_history`   | insert        |
| `payment_status_history` | insert        |
| `outbox_events`          | insert        |

## 8. Configuration (local / ops)

```env
COMMERCE_AUTO_CANCEL_UNPAID_ORDER_ENABLED=false
COMMERCE_AUTO_CANCEL_UNPAID_ORDER_CRON=0 */10 * * * *
COMMERCE_AUTO_CANCEL_UNPAID_ORDER_BATCH_SIZE=50
COMMERCE_AUTO_CANCEL_UNPAID_ORDER_TTL_MINUTES=30
```

Mac dinh **disabled** giong outbox schedulers. Bat khi can test/staging.

## 9. FE Integration Notes

FE khong goi truc tiep. Buyer thay order `CANCELLED` / payment `EXPIRED` qua order detail sau khi job chay.
