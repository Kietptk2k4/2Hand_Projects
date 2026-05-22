# Update Product – API & Behavior

## 1. Business Goal

Seller cap nhat thong tin core cua san pham thuoc shop minh (title, mo ta, category, condition, weight, brand/product type). **Khong** doi gia, ton kho, media, hoac trang thai publish/pause/archive qua API nay.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/seller/products/{productId}`
- **Auth:** Bearer JWT (seller — `user_id` = `products.seller_id`)

### Path

| Param        | Type | Required | Mo ta        |
|--------------|------|----------|--------------|
| `productId`  | UUID | yes      | San pham can sua |

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
  "title": "iPhone 15 Pro 256GB (cap nhat)",
  "description": "May dung tot, pin 90%",
  "weight_gram": 220
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat san pham thanh cong.",
  "data": {
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "status": "ACTIVE",
    "product_type": "PHYSICAL",
    "category_id": "880e8400-e29b-41d4-a716-446655440003",
    "brand_id": null,
    "condition": "USED",
    "title": "iPhone 15 Pro 256GB (cap nhat)",
    "description": "May dung tot, pin 90%",
    "weight_gram": 220,
    "created_at": "2026-05-20T10:00:00Z",
    "updated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

- `status` giu nguyen (DRAFT / ACTIVE / PAUSED); API nay **khong** publish/pause/archive.
- Order item snapshots da tao **khong** bi thay doi khi update product.

## 4. FE Behavior

- Cho phep sua khi product o trang thai **DRAFT**, **ACTIVE**, hoac **PAUSED**.
- Chan form khi `REMOVED` hoac `ARCHIVED`.
- Gia / ton kho / media: dung API rieng (FR_UpdateProductPrice, FR_UpdateProductInventory, upload media).
- Shop phai **ACTIVE**; neu shop `SUSPENDED` / `CLOSED` → loi shop status.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Payload khong hop le |
| 404 | `COMMERCE-404-PRODUCT` | Product khong ton tai hoac khong thuoc seller |
| 404 | `COMMERCE-404-CATEGORY` | Category khong ton tai / inactive |
| 409 | `COMMERCE-409-PRODUCT-REMOVED` | Product da `REMOVED` |
| 409 | `COMMERCE-409-PRODUCT-STATUS` | Product `ARCHIVED` |
| 409 | `COMMERCE-409-SELLER-SHOP` | Seller chua co shop |
| 409 | `COMMERCE-409-SHOP-STATUS` | Shop khong ACTIVE |

## 6. Events

- Outbox: `COMMERCE_PRODUCT_UPDATED` → topic `commerce.product.updated` (cung transaction)

## 7. Related

- Create: `POST /commerce/api/v1/seller/products` — `CreateProduct-api-and-behavior.md`
- Publish / Pause / Archive: action endpoints rieng
- FR: `docs/feature_requirements/commerce/FR_UpdateProduct.md`
