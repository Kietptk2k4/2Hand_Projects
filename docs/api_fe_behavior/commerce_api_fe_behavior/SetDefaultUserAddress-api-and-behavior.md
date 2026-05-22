# Set Default User Address – API & Behavior

## 1. Business Goal

Buyer chon mot dia chi trong address book lam mac dinh cho checkout. Moi buyer chi co **mot** default address tai mot thoi diem. Shipment/order snapshots **khong** bi thay doi.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/addresses/{addressId}/default`
- **Auth:** Bearer JWT (`user_id` = buyer)

Khong co request body.

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Dat dia chi mac dinh thanh cong.",
  "data": {
    "address_id": "550e8400-e29b-41d4-a716-446655440000",
    "user_id": "660e8400-e29b-41d4-a716-446655440001",
    "receiver_name": "Nguyen Van A",
    "phone": "0901234567",
    "province_code": "79",
    "district_code": "760",
    "ward_code": "26734",
    "address_detail": "123 Nguyen Van Linh, Q.7",
    "is_default": true,
    "created_at": "2026-05-21T14:00:00Z",
    "updated_at": "2026-05-21T15:30:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T15:30:01Z"
}
```

Response shape giong `POST /addresses` (create) de FE cap nhat danh sach dia chi.

## 4. Server behavior

1. Tim `user_addresses` theo `addressId` + JWT `user_id`.
2. Trong mot transaction: unset `is_default` cho tat ca dia chi cua buyer, set `is_default = true` cho dia chi target.
3. Tra ve ban ghi dia chi sau khi cap nhat.

DB co partial unique index: `UNIQUE(user_id) WHERE is_default = TRUE`.

## 5. FE Behavior

- Sau success: danh dau dia chi target la default trong UI; bo default cac dia chi khac.
- Goi lai khi user chon "Dat lam mac dinh" — idempotent neu dia chi da la default.
- Khong can body; chi can JWT + `addressId`.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-ADDRESS` | Khong ton tai hoac khong thuoc buyer |
| 409 | `COMMERCE-409-ADDRESS-DEFAULT` | Xung dot unique default (race) |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_SetDefaultUserAddress.md`
- Create: `CreateUserAddress-api-and-behavior.md`
- Delete: `DeleteUserAddress-api-and-behavior.md`
