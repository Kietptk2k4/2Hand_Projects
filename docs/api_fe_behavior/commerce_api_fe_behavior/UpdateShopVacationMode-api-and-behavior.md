# Update Shop Vacation Mode – API & Behavior

## 1. Business Goal

Seller bat/tat **vacation mode** va cap nhat thong bao cho buyer. Khong doi `seller_shops.status`; checkout MVP chan don moi khi shop dang vacation (`COMMERCE-409-SHOP-VACATION`).

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/seller/shop/vacation`
- **Auth:** Bearer JWT (seller)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `is_vacation` | boolean | yes | `true` = nghi, `false` = mo ban |
| `vacation_message` | string | no | Toi da 500 ky tu; khuyen dung khi bat vacation |

```json
{
  "is_vacation": true,
  "vacation_message": "Shop tam nghi den 25/05. Cam on ban da quan tam!"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Bat che do nghi shop thanh cong.",
  "data": {
    "shop_id": "770e8400-e29b-41d4-a716-446655440002",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "status": "ACTIVE",
    "is_vacation": true,
    "vacation_message": "Shop tam nghi den 25/05. Cam on ban da quan tam!",
    "updated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

- `status`: read-only — vacation **khong** doi shop status.

## 4. FE Behavior

- Bat vacation: gui `is_vacation: true` + message (optional nhung nen co).
- Tat vacation: `is_vacation: false` — khong gui `vacation_message` se **xoa** message cu.
- Discovery van hien product voi `shop_vacation` / `vacation_message` cho buyer.
- Checkout: bi chan voi `COMMERCE-409-SHOP-VACATION` (da co san trong `CheckoutFromCart`).

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | `vacation_message` > 500 ky tu |
| 404 | `COMMERCE-404-SHOP` | Seller chua co shop |

## 6. Events

- Outbox: `COMMERCE_SHOP_VACATION_UPDATED` → topic `commerce.shop.vacation.updated` (cung transaction)

## 7. Related

- Update profile: `PATCH /commerce/api/v1/seller/shop` — `UpdateShopProfile-api-and-behavior.md`
- Checkout block: `CheckoutFromCart-api-and-behavior.md`
- FR: `docs/feature_requirements/commerce/FR_UpdateShopVacationMode.md`
