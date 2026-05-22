# Handle Payment Failure – API & Behavior

## 1. Business Goal

Xu ly payment **FAILED**, **CANCELLED**, **EXPIRED** khi order dang cho thanh toan: cap nhat payment/order, **release reserved inventory** mot lan, ghi history va outbox.

## 2. Triggers (System)

| Trigger | Terminal status | `changed_by` |
|---------|-----------------|--------------|
| PayOS webhook (non-success) | `FAILED` / `CANCELLED` | `SYSTEM:PAYOS_WEBHOOK` |
| Auto-cancel unpaid job | `EXPIRED` | `SYSTEM:AUTO_CANCEL_UNPAID_ORDER` |

**PayOS webhook:** `POST /commerce/api/v1/payments/webhooks/payos` (public, verify signature).

## 3. Business rules

- Chi payment **`PENDING`** moi chuyen sang terminal failure.
- Payment da **`PAID`** → skip (log warn), **khong ghi de**.
- Order **`CREATED` / `AWAITING_PAYMENT`** + `payment_status = PENDING` → **`CANCELLED`**.
- Release inventory: `reserved_quantity -= qty`, `stock_quantity += qty` (order items `PENDING`).
- COD payment → skip failure handler.
- Shipment da bat dau (ngoai `PENDING`/`CANCELLED`) → skip.
- Outbox: `COMMERCE_PAYMENT_FAILED` | `COMMERCE_PAYMENT_CANCELLED` | `COMMERCE_PAYMENT_EXPIRED`, `COMMERCE_ORDER_CANCELLED`, `COMMERCE_INVENTORY_RELEASED`.

## 4. PayOS webhook (failure path)

**Request body (example):**

```json
{
  "code": "01",
  "desc": "Payment cancelled",
  "data": { "orderCode": "123456789", "amount": 100000 },
  "signature": "..."
}
```

- `code = "00"` → success webhook: **ack only** (success handler la FR rieng).
- Signature invalid → log, **khong** cap nhat domain.
- Duplicate `(provider, payos_order_code, event_type)` → idempotent no-op.

## 5. Response – Webhook ack

**HTTP 200 OK** (luon tra 200 khi da nhan payload de PayOS khong retry vo han)

```json
{
  "code": 200,
  "success": true,
  "message": "Da nhan PayOS webhook.",
  "data": {
    "event_type": "PAYOS_01",
    "payos_order_code": "123456789",
    "signature_valid": true,
    "processed": true,
    "terminal_status": "CANCELLED",
    "failure_outcome": "PROCESSED",
    "success_webhook": false
  }
}
```

## 6. Failure outcomes (`failure_outcome`)

| Value | Meaning |
|-------|---------|
| `PROCESSED` | Da cap nhat payment/order + release stock |
| `SKIPPED_ALREADY_PAID` | Payment da PAID — conflict log |
| `SKIPPED_PAYMENT_NOT_PENDING` | Payment khong con PENDING |
| `SKIPPED_ALREADY_TERMINAL` | Order/payment da terminal |
| `SKIPPED_COD` | COD — khong auto-fail |
| `SKIPPED_SHIPMENT_STARTED` | Da co shipment active |
| `SKIPPED_ORDER_NOT_CANCELLABLE` | Order khong con cancellable |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_HandlePaymentFailure.md`
- Webhook: `docs/feature_requirements/commerce/FR_ProcessPayOSWebhook.md`
- Job: `AutoCancelUnpaidOrders` (EXPIRED)
