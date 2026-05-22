# View Product Detail – API & Behavior

## 1. Business Goal

Buyer/guest xem chi tiet san pham buyer-visible truoc khi add cart/checkout: thong tin product, shop, category, media, gia active, ton kho (summary), attributes va review summary.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/products/{productId}`
- **Auth:** Public (khong bat buoc JWT)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `productId` | UUID | ID san pham |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay chi tiet san pham thanh cong.",
  "data": {
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "iPhone 15",
    "description": "Like new device",
    "condition": "LIKE_NEW",
    "weight_gram": 200,
    "status": "ACTIVE",
    "category": {
      "category_id": "...",
      "name": "Phones",
      "slug": "phones"
    },
    "shop": {
      "shop_id": "...",
      "shop_name": "Tech Shop",
      "avatar_url": "http://localhost:9000/2hands-commerce-shop/avatar.jpg",
      "cover_url": null
    },
    "media": [
      {
        "media_id": "...",
        "media_url": "http://localhost:9000/2hands-commerce-product/p1.jpg",
        "media_type": "IMAGE",
        "sort_order": 0
      }
    ],
    "attributes": [
      { "attribute_name": "color", "attribute_value": "black" }
    ],
    "price": 20000000,
    "sale_price": 18000000,
    "effective_price": 18000000,
    "inventory_summary": {
      "stock_quantity": 5,
      "low_stock_threshold": 2,
      "in_stock": true,
      "low_stock": false
    },
    "rating_avg": 4.67,
    "rating_count": 3,
    "shop_vacation": false,
    "vacation_message": null
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

### Field notes

| Field | Mo ta |
|-------|-------|
| `status` | `ACTIVE` hoac `OUT_OF_STOCK` (buyer-visible) |
| `effective_price` | `sale_price` neu co, nguoc lai `price` |
| `inventory_summary` | Chi `stock_quantity`, `low_stock_threshold`, `in_stock`, `low_stock` — **khong** tra `reserved_quantity` |
| `rating_avg`, `rating_count` | Aggregate review `VISIBLE` qua `order_items.product_id` |
| `media[].media_url` | MinIO bucket `2hands-commerce-product` |
| `shop_vacation`, `vacation_message` | Tu `shop_settings` |

## 4. Visibility rules

Chi tra product khi:

- `products.status IN (ACTIVE, OUT_OF_STOCK)`
- `seller_shops.status = ACTIVE`
- `product_categories.is_active = true`
- Co **active price** tai thoi diem query

Cac truong hop khac (DRAFT, PAUSED, ARCHIVED, REMOVED, shop SUSPENDED/CLOSED, khong co gia) → **404** `COMMERCE-404-PRODUCT`.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 404 | `COMMERCE-404-PRODUCT` | Khong ton tai hoac khong buyer-visible |
| 400 | `COMMERCE-400` | `productId` khong phai UUID hop le |

## 6. FE guidance

- `status = OUT_OF_STOCK` hoac `inventory_summary.in_stock = false` → hien ro het hang; van co the xem detail.
- `shop_vacation = true` → hien `vacation_message`, chan add cart/checkout (revalidate server).
- Sau khi xem detail, add cart/checkout phai revalidate gia/ton kho tren server.
- Khong dung detail response cho seller edit (dung seller APIs).
