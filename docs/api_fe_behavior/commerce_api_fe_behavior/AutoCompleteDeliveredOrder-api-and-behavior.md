# Auto Complete Delivered Order – API & Behavior

## 1. Business Goal

System job tu dong chuyen order items `DELIVERED` sang `COMPLETED` sau cua so cho (MVP mac dinh **7 ngay** ke tu `shipments.delivered_at` hoac `order_items.updated_at`). Neu du dieu kien, danh dau COD `PAID` va complete order. **Khong co public HTTP API.**

## 2. API Contract

Khong ap dung — **scheduled/internal job**.

| Muc          | Gia tri |
|--------------|---------|
| Trigger      | Spring `@Scheduled` (mac dinh hourly) |
| Component    | `AutoCompleteDeliveredOrderScheduler` |
| Use case     | `AutoCompleteDeliveredOrdersUseCase` |

## 3. Response – Success

Job log:

- `candidates` — so order item tim thay
- `itemsCompleted` — so item chuyen `COMPLETED`
- `ordersCompleted` — so order chuyen `COMPLETED`
- `ordersProcessed` — so order duoc xu ly trong batch
- `failed` — loi theo tung order
- `elapsedMs`

## 4. Response – Error

Loi theo tung order duoc log + dem `failed`; batch tiep tuc.

| Tinh huong | Hanh vi |
|------------|---------|
| Order da `COMPLETED` | Skip (no-op) |
| Payment chua `PAID` (PayOS chua thanh toan) | Complete items nhung **khong** complete order |
| Item khong con `DELIVERED` | Skip item (conditional update) |

## 5. Business Rules

- Chi item `DELIVERED` va `COALESCE(shipments.delivered_at, order_items.updated_at) < now - windowDays`.
- Item: `DELIVERED → COMPLETED`, set `completed_at`.
- COD payment `PENDING → PAID` khi job auto-complete items (MVP policy).
- Order `COMPLETED` chi khi:
  - Tat ca items `COMPLETED` hoac `CANCELLED` (khong con item open).
  - Payment `PAID` (PayOS da paid truoc, hoac COD vua mark).
  - Order dang `PROCESSING`.
- Shipment `DELIVERED` **khong** tu dong complete order ngay.
- Idempotent: conditional updates, event keys deterministic.

## 6. Edge Cases

- Nhieu item cung order trong 1 batch → xu ly 1 transaction/order.
- Mot phan item da duoc buyer confirm → chi complete item con `DELIVERED` qua han.
- PayOS: payment thuong da `PAID` truoc shipment → order complete sau khi tat ca items `COMPLETED`.
- Retry job: item da `COMPLETED` → update 0 rows, an toan.

## 7. Data Dependencies

| Table                    | Action      |
|--------------------------|-------------|
| `order_items`            | read/update |
| `shipments`              | read        |
| `orders`                 | read/update |
| `payments`               | read/update |
| `order_status_history`   | insert      |
| `payment_status_history` | insert      |
| `outbox_events`          | insert      |

Outbox: `COMMERCE_PAYMENT_PAID` (COD), `COMMERCE_ORDER_COMPLETED`.

## 8. Configuration

```env
COMMERCE_AUTO_COMPLETE_DELIVERED_ORDER_ENABLED=false
COMMERCE_AUTO_COMPLETE_DELIVERED_ORDER_CRON=0 0 * * * *
COMMERCE_AUTO_COMPLETE_DELIVERED_ORDER_BATCH_SIZE=50
COMMERCE_AUTO_COMPLETE_DELIVERED_ORDER_WINDOW_DAYS=7
```

## 9. FE Integration Notes

FE khong goi job. Buyer co the dung `POST /commerce/api/v1/orders/{orderId}/confirm-received` truoc khi het han 7 ngay.
