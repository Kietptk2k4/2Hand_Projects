# Remove Product By Admin – API & Behavior

## 1. Business Goal

Admin remove sản phẩm vi phạm: `products.status = REMOVED`. Sản phẩm không hiển thị discovery, không add cart/checkout; seller không publish lại được.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/admin/products/{productId}/remove`
- **Auth:** Bearer JWT + permission `COMMERCE_PRODUCT_REMOVE` (role `ADMIN` fallback nếu JWT thiếu claim `permissions`)

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `reason` | string | yes | Lý do remove |

```json
{
  "reason": "Vi pham chinh sach noi dung"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Remove san pham thanh cong.",
  "data": {
    "product_id": "770e8400-e29b-41d4-a716-446655440002",
    "seller_id": "880e8400-e29b-41d4-a716-446655440003",
    "shop_id": "990e8400-e29b-41d4-a716-446655440004",
    "title": "iPhone 14",
    "status": "REMOVED",
    "previous_status": "ACTIVE",
    "already_removed": false,
    "cart_items_invalidated": 3,
    "removed_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

`already_removed: true` khi product đã `REMOVED` (idempotent).

## 4. Errors

| HTTP | Code | Khi nào |
|------|------|---------|
| 401 | `COMMERCE-401` | Thiếu JWT |
| 403 | `COMMERCE-403` | Thiếu `COMMERCE_PRODUCT_REMOVE` |
| 400 | `COMMERCE-400-VALIDATION` | `reason` trống |
| 404 | `COMMERCE-404-PRODUCT` | Product không tồn tại |
| 409 | `COMMERCE-409-PRODUCT-STATUS` | Race khi cập nhật status |

## 5. Business Rules

- Soft remove: không hard delete product.
- Cart items `ACTIVE`/`OUT_OF_STOCK` → `INVALID_PRODUCT` (đồng bộ).
- Không đổi inventory.
- Order item snapshots giữ nguyên.
- Outbox: `COMMERCE_PRODUCT_REMOVED` → `commerce.product.removed`, key `product:{id}:removed`.

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_RemoveProductByAdmin.md`
- Flow: `docs/business_flow/commerce_business_flow/product-management-flow.md` §14
