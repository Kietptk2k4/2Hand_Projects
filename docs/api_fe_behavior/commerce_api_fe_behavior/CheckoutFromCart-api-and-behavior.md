# Checkout From Cart – API & Behavior

## 1. Business Goal

Buyer checkout cac cart items da chon de tao order, order items (snapshots), payment va reserve inventory trong mot transaction. Day la diem duy nhat reserve stock trong MVP.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/checkout`
- **Auth:** Bearer JWT (required)

### Request body

| Field              | Type          | Required | Mo ta                                |
|--------------------|---------------|----------|--------------------------------------|
| `cart_item_ids`    | UUID[]        | yes      | Cart items can checkout              |
| `address_id`       | UUID          | yes      | Dia chi giao hang                    |
| `payment_method`   | string        | yes      | `COD` hoac `PAYOS`                   |
| `shipment_type`    | string (enum) | no       | `STANDARD` (mac dinh), `EXPRESS`, `SAME_DAY` |
| `idempotency_key`  | string        | no       | Tranh tao order trung khi retry      |

```json
{
  "cart_item_ids": ["770e8400-e29b-41d4-a716-446655440002"],
  "address_id": "880e8400-e29b-41d4-a716-446655440003",
  "payment_method": "PAYOS",
  "shipment_type": "STANDARD",
  "idempotency_key": "checkout-20260521-abc"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Checkout thanh cong.",
  "data": {
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "payment_id": "660e8400-e29b-41d4-a716-446655440001",
    "payment_method": "PAYOS",
    "payment_status": "PENDING",
    "order_status": "AWAITING_PAYMENT",
    "final_amount": 1840000,
    "payos_checkout_url": null
  },
  "errors": null,
  "timestamp": "2026-05-21T12:00:00.123Z"
}
```

**COD:** `order_status` = `PROCESSING`, `payment_status` = `PENDING`, `payos_checkout_url` = null.

**PAYOS:** `order_status` = `AWAITING_PAYMENT`; `payos_checkout_url` thuong `null` sau checkout — goi `POST /commerce/api/v1/payments/{payment_id}/payos-checkout-url` de lay URL (xem `CreatePayOSCheckoutUrl-api-and-behavior.md`).

**Idempotency:** Cung `idempotency_key` + buyer → tra ket qua order/payment da tao, message `Don hang da duoc tao truoc do (idempotency).`

## 4. Response – Error

| HTTP | Code string                     | Mo ta                                      |
|------|---------------------------------|--------------------------------------------|
| 400  | `COMMERCE-400-VALIDATION`       | Thieu field / cart_item_ids rong           |
| 400  | `COMMERCE-400-PAYMENT-METHOD`   | payment_method khong hop le                |
| 401  | `COMMERCE-401`                  | Khong co JWT                               |
| 404  | `COMMERCE-404-CART-ITEM`        | Cart item khong thuoc buyer                |
| 404  | `COMMERCE-404-ADDRESS`          | Dia chi khong ton tai                      |
| 404  | `COMMERCE-404-PRODUCT`          | Product khong ton tai                      |
| 409  | `COMMERCE-409-NOT-PURCHASABLE`  | Product/shop/category khong active         |
| 409  | `COMMERCE-409-SHOP-VACATION`    | Shop dang vacation                         |
| 409  | `COMMERCE-409-PRICE`            | Khong co active price                      |
| 409  | `COMMERCE-409-STOCK`            | Khong du ton kho                           |
| 409  | `COMMERCE-409-CART-ITEM`        | Cart item REMOVED / INVALID_PRODUCT        |
| 409  | `COMMERCE-409-SHIPPING-PROFILE` | Thieu seller shipping profile              |
| 500  | `COMMERCE-500`                  | Loi server (inventory conflict)            |

## 5. Business Rules

- Revalidate server-side: khong tin gia/status tu client.
- Cart item khong duoc `REMOVED` / `INVALID_PRODUCT`.
- Product `ACTIVE`, shop `ACTIVE`, category active, khong vacation.
- `stock_quantity >= quantity` (gom gop theo product neu trung).
- Reserve (internal `ReserveInventoryUseCase`): `stock_quantity -= q`, `reserved_quantity += q` — xem `ReserveInventory-api-and-behavior.md`.
- Snapshots tren `order_items`: ten SP, shop, SKU, image, attributes JSON, gia, phi ship allocated.
- **PAYOS:** order `AWAITING_PAYMENT`, payment `PENDING`, `expired_at` theo TTL job (~30 phut).
- **COD:** order `PROCESSING`, payment `PENDING`.
- Outbox: `COMMERCE_ORDER_CREATED`, `COMMERCE_INVENTORY_RESERVED`.
- Shipment khong tao luc checkout.

## 6. Edge Cases

- Cung product nhieu cart lines → gop quantity khi reserve.
- `idempotency_key` trung → tra order cu, khong reserve lan 2.
- PayOS checkout URL: null trong MVP (client cho den webhook/link API).

## 7. Data Dependencies

- Read: cart, cart_items, products, prices, inventory, shops, shop_settings, addresses, shipping profiles, media, attributes.
- Write: orders, order_items, payments, product_inventories, histories, outbox_events.

## 8. FE Integration Notes

- Goi `POST /checkout/quote` truoc de preview amount.
- Luu `idempotency_key` (UUID client) khi submit checkout.
- PAYOS: redirect khi co `payos_checkout_url` (future); hien tai poll order/payment status.
- COD: hien trang order processing, cho seller ship.
