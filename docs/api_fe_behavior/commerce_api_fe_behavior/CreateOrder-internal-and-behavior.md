# Create Order – Internal Use Case & Behavior

## 1. Business Goal

Tao order aggregate (`orders`, `order_items`, `order_status_history`), payment ban dau va outbox `COMMERCE_ORDER_CREATED`. Day la core cua checkout; **khong** co public API rieng trong MVP — buyer goi qua `POST /commerce/api/v1/checkout`.

## 2. Invocation

| Caller | Khi nao |
|--------|---------|
| `CheckoutFromCartUseCase` | Sau khi validate cart, reserve inventory trong cung transaction checkout |

Flow:

1. `CheckoutFromCartRepository.prepareCheckout` — validate + reserve stock
2. `CreateOrderUseCase.execute` — order + payment + order outbox
3. Ghi `COMMERCE_INVENTORY_RESERVED` outbox (checkout use case)

## 3. CreateOrderUseCase – Input

`CreateOrderCommand`:

- `buyer_id` (tu JWT / checkout context)
- `total_amount`, `final_amount` (server-calculated)
- `payment_method` (`COD` | `PAYOS`)
- `idempotency_key` (optional, forwarded to payment)
- `lines[]` — snapshot server-side, **khong** tin client snapshot
- `occurred_at`

Moi line (`CreateOrderLineRequest`):

- `product_id`, `seller_id`, `quantity`
- `unit_price`, `line_total`
- `product_name`, `shop_name`, `sku`, `image_url`, `attributes_json`
- `shipping_fee_allocated`

## 4. CreateOrderUseCase – Output

`CreateOrderResult`:

| Field | Mo ta |
|-------|--------|
| `order_id` | UUID order |
| `payment_id` | UUID payment 1:1 |
| `status` | `AWAITING_PAYMENT` (PAYOS) hoac `PROCESSING` (COD) |
| `payment_status` | `PENDING` |
| `payment_method` | `COD` / `PAYOS` |
| `total_amount` | Tong hang |
| `final_amount` | Tong + ship |
| `order_items[]` | `order_item_id`, `product_id`, `seller_id`, `quantity`, prices |

## 5. CreatePaymentUseCase

Duoc goi ngay sau khi tao order. Chi tiet: `CreatePayment-internal-and-behavior.md`.

## 6. Business Rules

- `total_amount >= 0`, `final_amount >= 0`
- Snapshot bat buoc: product name, shop name, unit/line price
- Multi-seller: moi line giu `seller_id`
- Khong reserve stock trong CreateOrder (checkout da reserve truoc)
- Outbox `COMMERCE_ORDER_CREATED` cung transaction

## 7. Errors

| Code | Khi nao |
|------|---------|
| `COMMERCE-400-VALIDATION` | Amount am, khong co line |
| `COMMERCE-409-ORDER-SNAPSHOT` | Thieu ten/gia snapshot |
| `COMMERCE-400-PAYMENT-METHOD` | Method khong hop le |

## 8. Related

- Public: `POST /commerce/api/v1/checkout` — xem `CheckoutFromCart-api-and-behavior.md`
- FR: `docs/feature_requirements/commerce/FR_CreateOrder.md`
