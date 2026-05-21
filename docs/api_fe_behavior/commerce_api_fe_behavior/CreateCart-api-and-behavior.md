# Create Cart – API & Behavior

## 1. Business Goal

Dam bao moi buyer co mot cart duy nhat. API get-or-create: neu da co cart thi tra cart hien tai; neu chua co thi tao moi. Khong tao cart item, khong reserve ton kho.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/cart`
- **Auth:** Bearer JWT (required)
- **Request body:** Khong (empty)

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao gio hang thanh cong.",
  "data": {
    "cart_id": "660e8400-e29b-41d4-a716-446655440001",
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "items": [],
    "created_at": "2026-05-21T10:00:00Z",
    "updated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

Neu cart da ton tai, `message` la `"Lay gio hang thanh cong."` va `items` gom cac cart item khong o trang thai `REMOVED`.

### Item shape (khi cart da co san pham)

| Field           | Type   | Mo ta                          |
|-----------------|--------|--------------------------------|
| `cart_item_id`  | UUID   | ID dong trong cart             |
| `product_id`    | UUID   | San pham                       |
| `seller_id`     | UUID   | Seller                         |
| `quantity`      | int    | So luong                       |
| `status`        | string | `ACTIVE`, `OUT_OF_STOCK`, ...  |

## 4. FE Behavior

- Goi sau dang nhap neu can khoi tao cart truoc khi them san pham (tuy chon; `POST /cart/items` cung tu tao cart).
- Goi lai nhieu lan van tra cung `cart_id` — idempotent.
- Khong truyen `user_id` tu client; lay tu JWT.
- `items` o day la snapshot DB, chua enrich gia/anh nhu View Cart.

## 5. Errors

| HTTP | Code              | Khi nao                |
|------|-------------------|------------------------|
| 401  | `UNAUTHORIZED`    | Thieu/het han JWT      |
| 500  | (server error)    | Loi DB                 |

## 6. Related APIs

- `POST /commerce/api/v1/cart/items` — them san pham (cung get-or-create cart)
- `GET /commerce/api/v1/cart` — xem cart day du (View Cart, khi co)
