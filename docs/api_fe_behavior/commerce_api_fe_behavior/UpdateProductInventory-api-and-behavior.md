# Update Product Inventory – API & Behavior

## 1. Business Goal

Seller cap nhat ton kho san pham (`stock_quantity`, `low_stock_threshold`). He thong co the doi trang thai product `ACTIVE` ↔ `OUT_OF_STOCK` va dong bo lai cart items.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/seller/products/{productId}/inventory`
- **Auth:** Bearer JWT (seller)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `stock_quantity` | integer | yes | >= 0, khong nho hon `reserved_quantity` hien tai |
| `low_stock_threshold` | integer | no | >= 0; bo qua de giu gia tri cu |

```json
{
  "stock_quantity": 25,
  "low_stock_threshold": 3
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat ton kho san pham thanh cong.",
  "data": {
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "status": "ACTIVE",
    "previous_status": "OUT_OF_STOCK",
    "status_changed": true,
    "stock_quantity": 25,
    "low_stock_threshold": 3,
    "reserved_quantity": 0,
    "cart_items_synced": 2,
    "updated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

- `reserved_quantity` **chi doc** — seller khong ghi de qua API nay.
- Neu chua co row `product_inventories`, he thong **tao moi** (`reserved_quantity = 0`).

## 4. FE Behavior

- Sau khi cap nhat ton kho: product detail / discovery phan anh stock moi.
- `status_changed = true` khi `ACTIVE` → `OUT_OF_STOCK` (stock = 0) hoac `OUT_OF_STOCK` → `ACTIVE` (stock > 0 va shop/category/price hop le).
- `DRAFT` / `PAUSED` / `ARCHIVED`: cap nhat inventory **khong** tu doi product status.
- `cart_items_synced`: so cart item duoc cap nhat trang thai sau sync.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Stock/threshold am; stock < reserved |
| 404 | `COMMERCE-404-PRODUCT` | Product khong ton tai / khong thuoc seller |
| 409 | `COMMERCE-409-PRODUCT-REMOVED` | Product da `REMOVED` |

## 6. Events

- Outbox: `COMMERCE_PRODUCT_INVENTORY_UPDATED` → topic `commerce.product.inventory.updated` (cung transaction)

## 7. Related

- Publish: `POST .../publish` — can inventory truoc publish
- Cart sync: `SyncCartItemStatus` (noi bo, goi sau update)
- FR: `docs/feature_requirements/commerce/FR_UpdateProductInventory.md`
