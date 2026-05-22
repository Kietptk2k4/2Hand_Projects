# Update Cart Item Quantity – API & Behavior

## 1. Business Goal

Buyer cap nhat so luong mot cart item. Server revalidate product/shop/price/stock hien tai; **khong** reserve inventory.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/cart/items/{cartItemId}`
- **Auth:** Bearer JWT (`user_id` = buyer)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `quantity` | int | yes | > 0 |

```json
{
  "quantity": 3
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat so luong san pham trong gio hang thanh cong.",
  "data": {
    "cart_id": "...",
    "cart_item_id": "...",
    "product_id": "...",
    "quantity": 3,
    "status": "ACTIVE",
    "product": {
      "product_id": "...",
      "seller_id": "...",
      "shop_id": "...",
      "product_name": "...",
      "image_url": "...",
      "price": 1000000,
      "sale_price": 900000,
      "effective_price": 900000,
      "in_stock": true,
      "available_quantity": 5
    },
    "cart_summary": {
      "active_item_count": 2
    }
  }
}
```

## 4. Business rules

- Chi cart item thuoc cart cua JWT user.
- `quantity` phai > 0; neu muon xoa item dung `DELETE /cart/items/{cartItemId}`.
- Item `REMOVED` khong cap nhat duoc.
- Product phai ACTIVE, shop ACTIVE, category active, co active price.
- `quantity` > `available_quantity` → **409** `COMMERCE-409-STOCK` (MVP reject, khong luu `OUT_OF_STOCK`).
- Thanh cong → item `status = ACTIVE` (neu truoc do la `OUT_OF_STOCK`/`INVALID_PRODUCT` va product hop le lai).
- Khong thay doi `product_inventories`.

## 5. FE Behavior

- Spinner/stepper goi PATCH khi user doi quantity.
- Hien loi 409 stock neu vuot ton kho; goi y giam quantity hoac xoa item.
- Dung `cart_summary.active_item_count` cap nhat badge gio hang.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 400 | `COMMERCE-400-VALIDATION` | `quantity` <= 0 |
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-CART-ITEM` | Cart/item khong ton tai hoac khong thuoc buyer |
| 404 | `COMMERCE-404-PRODUCT` | Product khong ton tai |
| 409 | `COMMERCE-409-CART-ITEM` | Item da `REMOVED` |
| 409 | `COMMERCE-409-NOT-PURCHASABLE` | Product/shop/category khong hop le |
| 409 | `COMMERCE-409-STOCK` | Het hang hoac quantity > stock |
| 409 | `COMMERCE-409-PRICE` | Khong co active price |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_UpdateCartItemQuantity.md`
- Add item: `AddProductToCart-api-and-behavior.md`
- Remove: `RemoveCartItem-api-and-behavior.md`
