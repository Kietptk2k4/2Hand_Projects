# Delete User Address – API & Behavior

## 1. Business Goal

Buyer xoa dia chi khoi address book. Shipment/order da tao van giu `shipping_address_snapshots` — khong bi anh huong.

## 2. API Contract

- **Method:** DELETE
- **URL:** `/commerce/api/v1/addresses/{addressId}`
- **Auth:** Bearer JWT (`user_id` = buyer)

Khong co request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Xoa dia chi giao hang thanh cong.",
  "data": {
    "address_id": "550e8400-e29b-41d4-a716-446655440000",
    "user_id": "660e8400-e29b-41d4-a716-446655440001",
    "was_default": true,
    "new_default_address_id": "770e8400-e29b-41d4-a716-446655440002",
    "deleted_at": "2026-05-21T15:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T15:00:01Z"
}
```

- `new_default_address_id`: `null` neu khong con dia chi hoac dia chi xoa khong phai default.
- Khi xoa default va con dia chi khac: he thong dat **dia chi con lai co `created_at` som nhat** lam default.

## 4. FE Behavior

- Sau delete: refresh danh sach dia chi; neu `new_default_address_id` co gia tri, cap nhat UI default.
- Khong can goi API rieng de xoa snapshot — server hard-delete `user_addresses` only.
- Chi xoa duoc address thuoc JWT user.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-ADDRESS` | Khong ton tai hoac khong thuoc buyer |
| 409 | `COMMERCE-409-ADDRESS-DEFAULT` | Xung dot khi gan default moi (race) |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_DeleteUserAddress.md`
- Create: `CreateUserAddress-api-and-behavior.md`
