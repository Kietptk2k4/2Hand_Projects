# View My Shop – API & Behavior

## 1. Business Goal

Seller xem thong tin shop cua minh (profile, rating, vacation mode) de hien thi trang cai dat shop.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/seller/shop`
- **Auth:** Bearer JWT (seller — `user_id` = `seller_shops.seller_id`)

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay thong tin shop thanh cong.",
  "data": {
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_name": "Vintage Closet",
    "description": "Do secondhand thoi trang",
    "avatar_url": "https://minio.example/2hands-commerce-shop/shops/avatar.png",
    "cover_url": "https://minio.example/2hands-commerce-shop/shops/cover.png",
    "status": "ACTIVE",
    "rating_avg": 4.50,
    "rating_count": 2,
    "is_vacation": false,
    "vacation_message": null,
    "created_at": "2026-05-20T10:00:00Z",
    "updated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. Response – Error

| HTTP | Code | Mo ta |
|------|------|-------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-SHOP` | Seller chua co shop |

## 5. Business Rules

- Moi seller toi da 1 shop (MVP).
- Tra shop thuoc seller dang dang nhap; khong nhan `shop_id` tu client.
- `is_vacation` / `vacation_message` lay tu `shop_settings` (LEFT JOIN — mac dinh `false` / `null`).

## 6. Edge Cases

- Seller chua tao shop → 404; FE redirect sang trang tao shop.
- Shop `SUSPENDED` van tra ve profile (seller van xem/cap nhat settings).

## 7. Data Dependencies

- PostgreSQL: `seller_shops`, `shop_settings`

## 8. FE Integration Notes

- Hook: `useMyShop` → `fetchMyShop()` trong `sellerShopApi.js`.
- Trang: `/commerce/seller/shop/settings`.
- 404 → redirect `/commerce/seller/shop/create`.
- Update profile: `PATCH /commerce/api/v1/seller/shop` — `UpdateShopProfile-api-and-behavior.md`.
- Vacation mode: `PATCH /commerce/api/v1/seller/shop/vacation` — `UpdateShopVacationMode-api-and-behavior.md`.