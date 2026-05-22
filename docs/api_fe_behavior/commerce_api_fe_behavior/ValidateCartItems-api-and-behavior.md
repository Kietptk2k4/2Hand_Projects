# Validate Cart Items – API & Behavior

## 1. Business Goal

Buyer (hoac FE truoc checkout) kiem tra lai cart items theo trang thai catalog/inventory hien tai. API **khong** tao order, **khong** reserve stock. Co the cap nhat `cart_items.status` (`ACTIVE`, `OUT_OF_STOCK`, `INVALID_PRODUCT`).

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/cart/validate`
- **Auth:** Bearer JWT (buyer)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `cart_item_ids` | UUID[] | no | Neu bo trong/null: validate tat ca item khong `REMOVED` |

```json
{
  "cart_item_ids": [
    "880e8400-e29b-41d4-a716-446655440003"
  ]
}
```

Body `{}` hoac khong gui body = validate toan bo cart (non-removed).

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Kiem tra gio hang thanh cong.",
  "data": {
    "valid_items": [
      {
        "cart_item_id": "880e8400-e29b-41d4-a716-446655440003",
        "current_status": "ACTIVE"
      }
    ],
    "invalid_items": [
      {
        "cart_item_id": "990e8400-e29b-41d4-a716-446655440004",
        "reason": "OUT_OF_STOCK",
        "current_status": "OUT_OF_STOCK"
      }
    ],
    "can_checkout": false
  },
  "errors": null,
  "timestamp": "2026-05-21T12:00:01Z"
}
```

### `reason` values

| Code | Y nghia |
|------|---------|
| `CART_ITEM_REMOVED` | Item da xoa khoi cart |
| `INVALID_PRODUCT` | Product/shop khong hop le |
| `OUT_OF_STOCK` | Stock khong du cho quantity |
| `PRODUCT_NOT_ACTIVE` | Product khong ACTIVE |
| `SHOP_NOT_ACTIVE` | Shop khong ACTIVE |
| `CATEGORY_INACTIVE` | Category inactive |
| `ACTIVE_PRICE_MISSING` | Khong co gia active |
| `SHOP_ON_VACATION` | Shop dang vacation (block checkout) |
| `PRODUCT_NOT_FOUND` | Product khong ton tai |

### `can_checkout`

- `true` khi co it nhat 1 `valid_items` va **khong** co `invalid_items` trong pham vi validate.
- Cart trong / khong co item validate -> `valid_items=[]`, `invalid_items=[]`, `can_checkout=false`.

## 4. Server behavior

1. JWT `user_id` -> cart cua buyer.
2. Load items (theo ids hoac tat ca non-removed).
3. Doc product/shop/price/inventory + `shop_settings.is_vacation`.
4. Tinh status moi (reuse `CartItemSyncEvaluator`), persist neu doi.
5. Phan loai valid/invalid + ly do; vacation block checkout nhung co the giu status `ACTIVE`.

Checkout van **bat buoc** revalidate server-side — khong tin tuyet doi ket qua cu.

## 5. FE Behavior

- Goi truoc man checkout / khi mo gio hang.
- Neu `can_checkout=false`: hien ly do tung dong, disable nut checkout.
- Co the gui subset `cart_item_ids` dang chon de checkout.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-CART-ITEM` | `cart_item_ids` co id khong thuoc cart buyer |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ValidateCartItems.md`
- Flow: `docs/business_flow/commerce_business_flow/cart-lifecycle-flow.md`
- Checkout: `CheckoutFromCart` (revalidate lai)
