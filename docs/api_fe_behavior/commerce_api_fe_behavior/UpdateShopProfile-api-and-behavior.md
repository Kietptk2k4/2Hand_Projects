# Update Shop Profile – API & Behavior

## 1. Business Goal

Seller cap nhat thong tin public profile cua shop (ten, mo ta, avatar, cover). **Khong** doi `status`, `rating_avg`, `rating_count`, vacation mode hay shipping profile.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/seller/shop`
- **Auth:** Bearer JWT (seller — `user_id` = `seller_shops.seller_id`)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `shop_name` | string | no* | Toi da 255 ky tu, khong blank neu gui |
| `description` | string | no* | Mo ta shop; `""` de xoa |
| `avatar_url` | string | no* | URL MinIO bucket `2hands-commerce-shop` |
| `cover_url` | string | no* | URL MinIO bucket `2hands-commerce-shop` |

\* Phai co it nhat mot field.

```json
{
  "shop_name": "2Hands Official Store",
  "description": "Hang secondhand chinh hang",
  "avatar_url": "https://minio.example/2hands-commerce-shop/shops/avatar.png",
  "cover_url": "https://minio.example/2hands-commerce-shop/shops/cover.png"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat thong tin shop thanh cong.",
  "data": {
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_name": "2Hands Official Store",
    "description": "Hang secondhand chinh hang",
    "avatar_url": "https://minio.example/2hands-commerce-shop/shops/avatar.png",
    "cover_url": "https://minio.example/2hands-commerce-shop/shops/cover.png",
    "status": "ACTIVE",
    "rating_avg": 4.50,
    "rating_count": 2,
    "is_vacation": false,
    "created_at": "2026-05-20T10:00:00Z",
    "updated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

- `status`, `rating_*`, `is_vacation`: **read-only** trong response.

## 4. FE Behavior

- Upload avatar/cover len MinIO bucket **`2hands-commerce-shop`** (presigned), roi gui URL vao PATCH.
- Shop `SUSPENDED` van cap nhat duoc profile nhung van khong ban hang.
- Khong gui field khong muon doi.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Khong gui field nao; `shop_name` blank |
| 400 | `COMMERCE-400-MEDIA-URL` | URL khong thuoc bucket shop |
| 404 | `COMMERCE-404-SHOP` | Seller chua co shop |

## 6. Events

- Outbox: `COMMERCE_SHOP_UPDATED` → topic `commerce.shop.updated` (cung transaction)

## 7. Related

- Create shop: `POST /commerce/api/v1/seller/shop`
- Object storage: `docs/engineering_rules/commerce-object-storage.md`
- FR: `docs/feature_requirements/commerce/FR_UpdateShopProfile.md`
