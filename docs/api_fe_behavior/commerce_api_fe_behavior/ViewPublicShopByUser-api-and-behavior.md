# View Public Shop By User - API & Behavior

## 1. Business Goal

Social profile (hoac bat ky FE boundary) tra cuu shop commerce **ACTIVE** cua mot user de hien link "Xem shop" tren profile.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/users/{userId}/shop`
- **Auth:** Public (khong bat buoc JWT)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `userId` | UUID | ID user (seller) |

### Response `data` — khong co shop ACTIVE

```json
{
  "has_shop": false,
  "shop_id": null,
  "shop_name": null,
  "avatar_url": null,
  "seller_id": null
}
```

### Response `data` — co shop ACTIVE

| Field | Mo ta |
|-------|-------|
| `has_shop` | `true` |
| `shop_id` | UUID shop |
| `shop_name` | Ten shop |
| `avatar_url` | Avatar shop (nullable) |
| `seller_id` | UUID seller (trung `userId` khi shop ton tai) |

## 3. FE Behavior

- Goi tu `SocialProfilePage` / `ProfileHero` khi xem profile bat ky user.
- Chi hien CTA shop khi `has_shop === true`.
- Link toi `/commerce/shops/:shopId/products` (route shop storefront).

## 4. FE Integration Notes

- API: `publicShopByUserApi.js` -> `fetchPublicShopByUser(userId)`
- Hook: `usePublicShopByUser`
- UI: nut "Xem shop" tren `ProfileHero`

## 5. Related

- Shop storefront: `ViewProductsByShop-api-and-behavior.md`
- Social profile: social profile routes
