# Update Product Price – API & Behavior

## 1. Business Goal

Seller tao **record gia moi** trong `product_prices` (khong overwrite gia cu). Gia active duoc cart/checkout dung ngay; order da tao giu snapshot gia cu.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/products/{productId}/prices`
- **Auth:** Bearer JWT (seller)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `price` | number | yes | >= 0 |
| `sale_price` | number | no | >= 0 va <= `price` |
| `start_at` | ISO-8601 | yes | Thoi diem bat dau hieu luc |
| `end_at` | ISO-8601 | no | Phai sau `start_at` neu co |

```json
{
  "price": 1500000,
  "sale_price": 1350000,
  "start_at": "2026-05-21T10:00:00Z",
  "end_at": null
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat gia san pham thanh cong.",
  "data": {
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "status": "DRAFT",
    "price_id": "880e8400-e29b-41d4-a716-446655440003",
    "price": 1500000,
    "sale_price": 1350000,
    "effective_price": 1350000,
    "start_at": "2026-05-21T10:00:00Z",
    "end_at": null,
    "created_at": "2026-05-21T10:00:01Z",
    "previous_active_price_closed": true
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

- `effective_price` = `sale_price` neu co, nguoc lai = `price`.
- `previous_active_price_closed`: gia active trung window da duoc dong (`end_at = start_at` moi).

## 4. FE Behavior

- Moi lan doi gia: goi POST tao record moi (lich su gia duoc giu).
- `start_at` thuong = now cho cap nhat ngay; co the dat tuong lai neu business cho phep.
- Sau khi co gia: tiep tuc inventory, media, roi publish neu can.
- Cart/checkout doc active price theo `start_at` / `end_at` — khong can API sync cart rieng.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Gia/sale am; `end_at` <= `start_at`; sale > price |
| 404 | `COMMERCE-404-PRODUCT` | Product khong ton tai / khong thuoc seller |
| 409 | `COMMERCE-409-PRODUCT-REMOVED` | Product da `REMOVED` |
| 409 | `COMMERCE-409-PRICE-WINDOW` | Window trung gia da len lich (future overlap) |

## 6. Events

- Outbox: `COMMERCE_PRODUCT_PRICE_UPDATED` → topic `commerce.product.price.updated` (cung transaction)

## 7. Related

- Publish: can active price — `PublishProduct-api-and-behavior.md`
- FR: `docs/feature_requirements/commerce/FR_UpdateProductPrice.md`
