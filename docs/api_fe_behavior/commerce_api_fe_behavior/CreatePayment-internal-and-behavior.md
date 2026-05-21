# Create Payment – Internal Use Case & Behavior

## 1. Business Goal

Tao mot payment record `PENDING` cho order trong checkout (MVP: `COD`, `PAYOS`). Ghi `payment_status_history` va outbox `COMMERCE_PAYMENT_CREATED`. Khong tao payOS checkout URL tai buoc nay.

## 2. Invocation

| Caller | Khi nao |
|--------|---------|
| `CreateOrderUseCase` | Ngay sau khi insert order, trong cung transaction checkout |

## 3. CreatePaymentUseCase – Input

`CreatePaymentCommand` (server-side, khong tu client):

| Field | Mo ta |
|-------|--------|
| `payment_id` | UUID pre-generated |
| `order_id` | Order vua tao |
| `payer_id` | = `buyer_id` (tu JWT/checkout) |
| `buyer_id` | Buyer context |
| `amount` | Phai bang `order_final_amount` |
| `order_final_amount` | `orders.final_amount` |
| `payment_method` | Phai khop `orders.payment_method` |
| `order_payment_method` | Method tren order |
| `currency` | Mac dinh `VND` neu trong |
| `idempotency_key` | Optional (forward tu checkout) |
| `occurred_at` | Timestamp transaction |

## 4. Output

`CreatePaymentResult`:

- `payment_id`, `order_id`
- `status` = `PENDING`
- `payment_method`, `amount`, `currency`

## 5. Business Rules

- Mot order toi da mot payment (`payments.order_id` UNIQUE)
- `amount > 0` va `amount == orders.final_amount`
- `payer_id == buyer_id`
- PAYOS: set `expired_at` / `checkout_url_expired_at` theo TTL job config
- payOS provider fields (`payos_checkout_url`, ...) nullable luc tao

## 6. Outbox

- Event: `COMMERCE_PAYMENT_CREATED`
- Key: `payment:{payment_id}:created`

## 7. Errors

| Code | Khi nao |
|------|---------|
| `COMMERCE-400-PAYMENT-AMOUNT` | Amount <= 0 hoac khac final_amount |
| `COMMERCE-400-PAYMENT-METHOD` | Method khong hop le / khac order |
| `COMMERCE-409-PAYMENT-EXISTS` | Da co payment cho order |
| `COMMERCE-400-VALIDATION` | payer_id khong khop buyer |

## 8. Related

- Checkout: `CheckoutFromCart-api-and-behavior.md`
- Create order: `CreateOrder-internal-and-behavior.md`
- FR: `docs/feature_requirements/commerce/FR_CreatePayment.md`
