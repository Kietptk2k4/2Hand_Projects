# Create Product – API & Behavior

## 1. Business Goal

Seller tao san pham **DRAFT** trong shop cua minh. San pham chua hien buyer discovery cho den khi publish.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/products`
- **Auth:** Bearer JWT (seller — `user_id` = `products.seller_id`)

### Request body

| Field          | Type    | Required | Mo ta                          |
|----------------|---------|----------|--------------------------------|
| `product_type` | string  | yes      | Loai san pham (vd: `PHYSICAL`) |
| `category_id`  | UUID    | yes      | Category active                |
| `brand_id`     | UUID    | no       | Thuong hieu                    |
| `condition`    | string  | yes      | Tinh trang (vd: `NEW`, `USED`) |
| `title`        | string  | yes      | Toi da 500 ky tu               |
| `description`  | string  | yes      | Mo ta                          |
| `weight_gram`  | integer | yes      | > 0                            |

```json
{
  "product_type": "PHYSICAL",
  "category_id": "880e8400-e29b-41d4-a716-446655440003",
  "brand_id": null,
  "condition": "USED",
  "title": "iPhone 15 Pro 256GB",
  "description": "May dung tot, pin 92%",
  "weight_gram": 220
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao san pham draft thanh cong.",
  "data": {
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "status": "DRAFT",
    "product_type": "PHYSICAL",
    "category_id": "880e8400-e29b-41d4-a716-446655440003",
    "brand_id": null,
    "condition": "USED",
    "title": "iPhone 15 Pro 256GB",
    "description": "May dung tot, pin 92%",
    "weight_gram": 220,
    "created_at": "2026-05-21T10:00:00Z",
    "updated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. FE Behavior

- Sau create: them gia (`product_prices`), ton kho, media (MinIO bucket `2hands-commerce-product`), roi **Publish**.
- `seller_id` / `shop_id` **khong** gui tu client — he thong lay tu JWT + shop cua seller.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Payload khong hop le |
| 404 | `COMMERCE-404-CATEGORY` | Category khong ton tai / inactive |
| 409 | `COMMERCE-409-SELLER-SHOP` | Seller chua co shop |
| 409 | `COMMERCE-409-SHOP-STATUS` | Shop `SUSPENDED` / `CLOSED` |

## 6. Events

- Outbox: `COMMERCE_PRODUCT_CREATED` (cung transaction)

## 7. Related

- Publish: `POST /commerce/api/v1/seller/products/{productId}/publish` — xem `PublishProduct-api-and-behavior.md`
- Media: bucket `2hands-commerce-product`
- FR: `docs/feature_requirements/commerce/FR_CreateProduct.md`
