# Create Shop – API & Behavior

## 1. Business Goal

User (seller) tao **mot shop duy nhat** trong MVP. He thong tao `shop_settings` mac dinh va co the tao pickup profile neu gui kem.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/shop`
- **Auth:** Bearer JWT (`seller_id` = `user_id` tu token)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `shop_name` | string | yes | Toi da 255 ky tu |
| `description` | string | no | Mo ta shop |
| `avatar_url` | string | no | URL MinIO bucket `2hands-commerce-shop` |
| `cover_url` | string | no | URL MinIO bucket `2hands-commerce-shop` |
| `pickup_profile` | object | no | Neu gui thi day du tat ca field pickup |

`pickup_profile`:

| Field | Required |
|-------|----------|
| `pickup_name` | yes |
| `phone` | yes |
| `province_code` | yes |
| `district_code` | yes |
| `ward_code` | yes |
| `address_detail` | yes |

```json
{
  "shop_name": "2Hands Store",
  "description": "Do cu chat luong",
  "avatar_url": "http://localhost:9000/2hands-commerce-shop/seller-1/avatar.png",
  "cover_url": null,
  "pickup_profile": {
    "pickup_name": "Kho chinh",
    "phone": "0901234567",
    "province_code": "79",
    "district_code": "760",
    "ward_code": "26734",
    "address_detail": "123 Nguyen Van Linh"
  }
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao shop thanh cong.",
  "data": {
    "shop_id": "550e8400-e29b-41d4-a716-446655440000",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "shop_name": "2Hands Store",
    "description": "Do cu chat luong",
    "avatar_url": "http://localhost:9000/2hands-commerce-shop/seller-1/avatar.png",
    "cover_url": null,
    "status": "ACTIVE",
    "is_vacation": false,
    "shipping_profile_created": true,
    "created_at": "2026-05-21T12:00:00Z",
    "updated_at": "2026-05-21T12:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T12:00:01Z"
}
```

## 4. FE Behavior

1. Upload avatar/cover qua presigned URL (bucket `2hands-commerce-shop`) neu can.
2. Goi create shop **mot lan** — duplicate tra 409.
3. Sau khi co shop moi tao product / shipment.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Payload khong hop le |
| 400 | `COMMERCE-400-MEDIA-URL` | URL khong thuoc bucket shop (khi bat MinIO validation) |
| 409 | `COMMERCE-409-SHOP-EXISTS` | Seller da co shop |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_CreateShop.md`
- Outbox: `COMMERCE_SHOP_CREATED` → `commerce.shop.created`
