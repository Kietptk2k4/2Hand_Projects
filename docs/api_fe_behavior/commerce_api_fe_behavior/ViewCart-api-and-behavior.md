# View Cart – API & Behavior

## 1. Business Goal

Buyer xem gio hang hien tai voi gia, ton kho, trang thia san pham va canh bao truoc checkout. API **khong** reserve stock, **khong** tinh phi ship.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/cart`
- **Auth:** Bearer JWT (buyer)

Khong co request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay gio hang thanh cong.",
  "data": {
    "cart_id": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "cart_item_id": "880e8400-e29b-41d4-a716-446655440003",
        "product_id": "660e8400-e29b-41d4-a716-446655440001",
        "seller_id": "770e8400-e29b-41d4-a716-446655440002",
        "shop_id": "990e8400-e29b-41d4-a716-446655440004",
        "product_name": "iPhone 13",
        "image_url": "http://localhost:9000/2hands-commerce-product/abc.jpg",
        "quantity": 2,
        "status": "ACTIVE",
        "effective_price": 100000,
        "in_stock": true,
        "available_quantity": 10,
        "unavailable_reason": null
      }
    ],
    "summary": {
      "active_item_count": 1,
      "invalid_item_count": 0,
      "subtotal": 200000,
      "can_checkout": true,
      "warnings": []
    },
    "created_at": "2026-05-21T10:00:00Z",
    "updated_at": "2026-05-21T12:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T12:00:01Z"
}
```

### Field notes

| Field | Mo ta |
|-------|-------|
| `effective_price` | Gia active hien tai (sale neu co) |
| `in_stock` | `true` khi item checkout-eligible va du ton |
| `unavailable_reason` | Ma ly do (vd `OUT_OF_STOCK`, `SHOP_ON_VACATION`) — `null` neu OK |
| `subtotal` | Tong `effective_price * quantity` cho item hop le checkout |
| `can_checkout` | `true` khi co item hop le va khong co item invalid |
| `warnings` | Thong bao tong hop cho FE (het hang, shop nghi, ...) |

## 4. Server behavior

1. `getOrCreate` cart theo JWT `user_id` (cart trong neu chua co).
2. Load `cart_items` khong `REMOVED`.
3. Enrich tu product/shop/price/inventory/media.
4. Lazy cap nhat `cart_items.status` neu catalog doi.
5. Khong thay doi inventory.

Checkout van phai goi `POST /cart/validate` va checkout APIs de revalidate.

## 5. FE Behavior

- Man hinh gio hang: goi GET khi mo tab cart.
- Hien badge/warning tu `summary.warnings`.
- Disable checkout khi `can_checkout=false`.
- `image_url` tu bucket product MinIO.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewCart.md`
- Validate: `ValidateCartItems-api-and-behavior.md`
- Flow: `docs/business_flow/commerce_business_flow/cart-lifecycle-flow.md`
