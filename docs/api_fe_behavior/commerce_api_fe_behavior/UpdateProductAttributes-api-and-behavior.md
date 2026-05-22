# Update Product Attributes – API & Behavior

## 1. Business Goal

Seller cap nhat danh sach thuoc tinh cau truc cua san pham (mau, size, chat lieu, ...). Thuoc tinh duoc snapshot vao order item khi checkout; order da tao **khong** bi thay doi.

## 2. API Contract

- **Method:** PUT (replace toan bo)
- **URL:** `/commerce/api/v1/seller/products/{productId}/attributes`
- **Auth:** Bearer JWT (seller — `user_id` = `products.seller_id`)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `attributes` | array | yes | Danh sach thay the toan bo (co the `[]` de xoa het) |
| `attributes[].attribute_name` | string | yes | Toi da 255 ky tu, unique trong request |
| `attributes[].attribute_value` | string | yes | Toi da 500 ky tu, khong blank |

```json
{
  "attributes": [
    { "attribute_name": "Color", "attribute_value": "Black" },
    { "attribute_name": "Size", "attribute_value": "L" }
  ]
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat thuoc tinh san pham thanh cong.",
  "data": {
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "status": "ACTIVE",
    "attributes": [
      { "attribute_name": "Color", "attribute_value": "Black" },
      { "attribute_name": "Size", "attribute_value": "L" }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. FE Behavior

- **PUT = replace:** Gui day du danh sach mong muon; attribute khong co trong body se bi xoa.
- Gui `attributes: []` de xoa toan bo thuoc tinh.
- Chan khi product `REMOVED`.
- `ARCHIVED` / `PAUSED` / `DRAFT` van cho phep cap nhat attributes (theo FR).
- Khong gui `product_id` trong body — chi path + JWT.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Blank name/value, duplicate name trong request, vuot do dai |
| 404 | `COMMERCE-404-PRODUCT` | Product khong ton tai hoac khong thuoc seller |
| 409 | `COMMERCE-409-PRODUCT-REMOVED` | Product da `REMOVED` |

## 6. Events

- Outbox: `COMMERCE_PRODUCT_ATTRIBUTES_UPDATED` → topic `commerce.product.attributes.updated` (cung transaction)

## 7. Related

- Update product core: `PATCH /commerce/api/v1/seller/products/{productId}` — `UpdateProduct-api-and-behavior.md`
- Checkout snapshot: `CheckoutFromCart-api-and-behavior.md`
- FR: `docs/feature_requirements/commerce/FR_UpdateProductAttributes.md`
