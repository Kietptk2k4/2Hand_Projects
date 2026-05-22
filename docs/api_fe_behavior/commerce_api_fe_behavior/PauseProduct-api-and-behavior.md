# Pause Product – API & Behavior

## 1. Business Goal

Seller tam dung ban san pham (`PAUSED`). San pham khong hien discovery, khong add cart/checkout; cart items lien quan co the thanh `INVALID_PRODUCT`.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/products/{productId}/pause`
- **Auth:** Bearer JWT (`user_id` = `products.seller_id`)

Khong co request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Pause san pham thanh cong.",
  "data": {
    "product_id": "770e8400-e29b-41d4-a716-446655440002",
    "shop_id": "880e8400-e29b-41d4-a716-446655440003",
    "status": "PAUSED",
    "paused_at": "2026-05-21T10:00:00Z",
    "cart_items_invalidated": 2,
    "already_paused": false
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

`already_paused: true` khi product da o `PAUSED` (idempotent).

## 4. Business rules

| Trang thai hien tai | Pause |
|---------------------|-------|
| `ACTIVE` | Cho phep |
| `OUT_OF_STOCK` | Cho phep |
| `PAUSED` | Idempotent 200 |
| `DRAFT` | 409 |
| `ARCHIVED` | 409 |
| `REMOVED` | 409 |

- Order da tao **khong** bi anh huong.
- Discovery chi hien `ACTIVE` / `OUT_OF_STOCK` — `PAUSED` bi loai.
- Add cart yeu cau `ACTIVE` — `PAUSED` tra `COMMERCE-409-NOT-PURCHASABLE`.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-PRODUCT` | Product khong ton tai / khong thuoc seller |
| 409 | `COMMERCE-409-PRODUCT-STATUS` | Trang thai khong cho pause |
| 409 | `COMMERCE-409-PRODUCT-REMOVED` | Product da removed |

## 6. Database & Events

- Write: `products`, `cart_items` (optional), `outbox_events`
- Outbox: `COMMERCE_PRODUCT_PAUSED` → `commerce.product.paused`

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_PauseProduct.md`
- Flow: `docs/business_flow/commerce_business_flow/product-management-flow.md`
