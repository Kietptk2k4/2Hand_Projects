# Reserve Inventory – API & Behavior

## 1. Business Goal

Reserve inventory là **internal use case** trong checkout: chuyển stock từ `stock_quantity` sang `reserved_quantity` một cách atomic. Cart **không** reserve stock.

## 2. API Contract

Không có HTTP endpoint public. Được gọi từ `CheckoutFromCartUseCase` trong cùng transaction checkout.

### Input (`ReserveInventoryCommand`)

- `lines`: danh sách `{ product_id, quantity }` (đã gộp theo product, sort `product_id`)
- `occurred_at`: timestamp checkout

### Output (`ReserveInventoryResult`)

- `reserved_items`: `{ product_id, quantity }` dùng cho outbox `COMMERCE_INVENTORY_RESERVED`
- `reserved_at`

## 3. Business Rules

| Rule | Mô tả |
|------|-------|
| Atomic batch | Reserve tất cả product trong một transaction; fail một item → rollback toàn bộ |
| Stock check | `stock_quantity >= quantity` trước khi update |
| Row lock | `SELECT ... FOR UPDATE` theo `product_id` ASC |
| Missing inventory | Không có row `product_inventories` → `COMMERCE-409-INVENTORY` |
| Insufficient stock | Update 0 rows → `COMMERCE-409-STOCK` |
| Inventory update | `stock_quantity -= q`, `reserved_quantity += q` |

## 4. Checkout integration

```text
prepareCheckout (validate cart, price, stock preview)
  → ReserveInventoryUseCase
  → CreateOrderUseCase
  → outbox COMMERCE_INVENTORY_RESERVED (key: inventory:{order_id}:reserved)
```

## 5. Events

- `COMMERCE_INVENTORY_RELEASED` / settle: xử lý ở payment failure/success flows (không thuộc FR này).

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ReserveInventory.md`
- FR: `docs/feature_requirements/commerce/FR_CheckoutFromCart.md`
- Flow: `docs/business_flow/commerce_business_flow/checkout-order-flow.md`
