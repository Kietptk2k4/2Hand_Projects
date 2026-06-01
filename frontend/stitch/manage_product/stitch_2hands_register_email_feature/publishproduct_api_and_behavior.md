# Publish Product – API & Behavior

## 1. Business Goal

Seller publish sản phẩm sau khi đủ điều kiện bán: chuyển `DRAFT` (hoặc resume `PAUSED`) sang `ACTIVE` nếu còn stock, hoặc `OUT_OF_STOCK` nếu `stock_quantity = 0`.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/products/{productId}/publish`
- **Auth:** Bearer JWT (`user_id` = `products.seller_id`)

Không có request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Publish san pham thanh cong.",
  "data": {
    "product_id": "770e8400-e29b-41d4-a716-446655440002",
    "shop_id": "880e8400-e29b-41d4-a716-446655440003",
    "status": "ACTIVE",
    "published_at": "2026-05-21T10:00:00Z",
    "already_published": false
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

- `status`: `ACTIVE` hoặc `OUT_OF_STOCK` theo tồn kho.
- `already_published: true` khi product đã ở trạng thái publish đúng với stock hiện tại (idempotent).

## 4. Publish preconditions

| Điều kiện | Rule |
|-----------|------|
| Ownership | Seller sở hữu product |
| Shop | `seller_shops.status = ACTIVE` |
| Product | Không `REMOVED`, không `ARCHIVED` |
| Status | `DRAFT`, `PAUSED`, hoặc idempotent `ACTIVE`/`OUT_OF_STOCK` |
| Category | `product_categories.is_active = true` |
| Fields | `title`, `description`, `condition`, `weight_gram > 0` |
| Price | Có active price (`product_prices` trong khoảng hiện tại) |
| Inventory | Có bản ghi `product_inventories` |
| Media (MinIO bật) | Ít nhất một `product_media.media_url` hợp lệ bucket `2hands-commerce-product` |

## 5. Status result

| `stock_quantity` | Status sau publish |
|------------------|-------------------|
| `> 0` | `ACTIVE` |
| `= 0` | `OUT_OF_STOCK` |

`PAUSED` + đủ điều kiện → publish/resume như trên.

## 6. Errors

| HTTP | Code | Khi nào |
|------|------|---------|
| 401 | `COMMERCE-401` | Thiếu JWT |
| 404 | `COMMERCE-404-PRODUCT` | Product không tồn tại / không thuộc seller |
| 409 | `COMMERCE-409-SHOP-STATUS` | Shop không `ACTIVE` |
| 409 | `COMMERCE-404-CATEGORY` | Category không active |
| 409 | `COMMERCE-409-PRICE` | Thiếu active price |
| 409 | `COMMERCE-409-PRODUCT-STATUS` | Thiếu inventory hoặc status không cho publish |
| 409 | `COMMERCE-409-PRODUCT-REMOVED` | Product đã removed |
| 400 | `COMMERCE-400` | Thiếu field bắt buộc |
| 400 | `COMMERCE-400-MEDIA-URL` | MinIO bật nhưng thiếu/sai media URL |

## 7. Database & Events

- Read: `products`, `seller_shops`, `product_categories`, `product_prices`, `product_inventories`, `product_media`
- Write: `products.status`, `outbox_events`
- Outbox: `COMMERCE_PRODUCT_PUBLISHED` → `commerce.product.published`

## 8. Related

- FR: `docs/feature_requirements/commerce/FR_PublishProduct.md`
- Flow: `docs/business_flow/commerce_business_flow/product-management-flow.md` §11
