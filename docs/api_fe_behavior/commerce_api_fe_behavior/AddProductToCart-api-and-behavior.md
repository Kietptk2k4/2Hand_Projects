# Add Product To Cart – API & Behavior

## 1. Business Goal

Cho phep buyer them san pham vao gio hang. Neu buyer chua co cart, he thong tu tao cart. Thao tac nay **khong** reserve ton kho; chi checkout moi reserve inventory.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/cart/items`
- **Auth:** Bearer JWT (required)

### Request body

| Field        | Type    | Required | Mo ta                          |
|--------------|---------|----------|--------------------------------|
| `product_id` | UUID    | yes      | ID san pham can them           |
| `quantity`   | integer | yes      | So luong (> 0), set quantity   |

```json
{
  "product_id": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 2
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Them san pham vao gio hang thanh cong.",
  "data": {
    "cart_id": "660e8400-e29b-41d4-a716-446655440001",
    "cart_item_id": "770e8400-e29b-41d4-a716-446655440002",
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2,
    "status": "ACTIVE",
    "product": {
      "product_id": "550e8400-e29b-41d4-a716-446655440000",
      "seller_id": "880e8400-e29b-41d4-a716-446655440003",
      "shop_id": "990e8400-e29b-41d4-a716-446655440004",
      "product_name": "iPhone 13",
      "image_url": "https://cdn.example.com/product.jpg",
      "price": 1000000,
      "sale_price": 900000,
      "effective_price": 900000,
      "in_stock": true,
      "available_quantity": 10
    }
  },
  "errors": null,
  "timestamp": "2026-05-21T10:30:00.123Z"
}
```

`status` cart item co the la `OUT_OF_STOCK` khi `quantity` lon hon `available_quantity` (stock > 0).

## 4. Response – Error

| HTTP | Code string                    | Mo ta                                           |
|------|--------------------------------|-------------------------------------------------|
| 400  | `COMMERCE-400-VALIDATION`      | `quantity` <= 0 hoac thieu `product_id`         |
| 401  | `COMMERCE-401`                 | Khong co hoac JWT khong hop le                  |
| 404  | `COMMERCE-404-PRODUCT`         | Product khong ton tai                           |
| 409  | `COMMERCE-409-NOT-PURCHASABLE` | Product/shop/category khong hop le de mua         |
| 409  | `COMMERCE-409-PRICE`           | Khong co active price                           |
| 409  | `COMMERCE-409-STOCK`           | Stock = 0 (reject add moi)                      |
| 409  | `COMMERCE-409-SELF-PURCHASE`   | Buyer khong the mua san pham cua chinh minh     |
| 500  | `COMMERCE-500`                 | Loi server                                      |

## 5. Business Rules

- Buyer chi thao tac cart cua minh (`user_id` tu JWT).
- Product phai `ACTIVE`, shop `ACTIVE`, category active.
- Buyer khong the them san pham cua chinh minh (`seller_id` == buyer id) → `COMMERCE-409-SELF-PURCHASE`.
- Phai co active price (`start_at <= now`, `end_at` null hoac > now).
- Stock = 0 → reject 409 (khong tao item moi).
- Cung `(cart_id, product_id)` → upsert (cap nhat quantity), khong duplicate.
- Item `REMOVED` duoc reactivate khi add lai cung product.
- `seller_id` lay tu product/shop, khong tin client.
- Khong thay doi `product_inventories`.
- Quantity la **gia tri moi** (set), khong cong delta.

## 6. Edge Cases

- **Hai request tao cart dong thoi:** unique `user_id` → reload cart.
- **Add lai product da REMOVED:** reactivate + cap nhat quantity.
- **Quantity > stock (stock > 0):** tao/cap nhat item voi `OUT_OF_STOCK`.
- **Product PAUSED/REMOVED hoac shop SUSPENDED:** 409.

## 7. Data Dependencies

| Table               | Action        |
|---------------------|---------------|
| `carts`             | read/insert   |
| `cart_items`        | insert/update |
| `products`          | read          |
| `seller_shops`      | read          |
| `product_categories`| read          |
| `product_prices`    | read          |
| `product_inventories` | read      |
| `product_media`     | read (optional image) |

## 8. FE Integration Notes

- Goi sau khi user dang nhap; luu `cart_id` neu can cho cac API cart khac.
- Hien thi `effective_price` va `in_stock` / `status` de canh bao truoc checkout.
- Khong gui `seller_id` trong request.
- Local dev: `http://localhost:3003/commerce/api/v1/cart/items`.
