# Remove Cart Item – API & Behavior

## 1. Business Goal

Buyer xóa sản phẩm khỏi giỏ hàng. MVP dùng soft delete: `cart_items.status = REMOVED` (không hard delete, không đổi inventory).

## 2. API Contract

- **Method:** DELETE
- **URL:** `/commerce/api/v1/cart/items/{cartItemId}`
- **Auth:** Bearer JWT (required)

Không có request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Xoa san pham khoi gio hang thanh cong.",
  "data": {
    "cart_id": "660e8400-e29b-41d4-a716-446655440001",
    "cart_item_id": "770e8400-e29b-41d4-a716-446655440002",
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "REMOVED",
    "removed_at": "2026-05-21T10:00:00Z",
    "cart_summary": {
      "active_item_count": 1
    },
    "already_removed": false
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

`already_removed: true` khi item đã `REMOVED` trước đó (idempotent).

## 4. Response – Error

| HTTP | Code | Mô tả |
|------|------|-------|
| 401 | `COMMERCE-401` | Thiếu JWT |
| 404 | `COMMERCE-404-CART-ITEM` | Cart/cart item không tồn tại hoặc không thuộc buyer |
| 500 | `COMMERCE-500` | Lỗi server |

## 5. Business Rules

- Ownership qua `carts.user_id` = JWT `user_id`.
- Remove idempotent nếu item đã `REMOVED`.
- Không thay đổi `product_inventories`.
- Item `REMOVED` không xuất hiện trong active cart view (`findByCartIdExcludingRemoved`).
- Thêm lại cùng `product_id` → upsert/reactivate row (unique `(cart_id, product_id)`).

## 6. Database

- Read: `carts`, `cart_items`
- Write: `cart_items.status = REMOVED`, `cart_items.updated_at`, `carts.updated_at`

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_RemoveCartItem.md`
- Flow: `docs/business_flow/commerce_business_flow/cart-lifecycle-flow.md`
