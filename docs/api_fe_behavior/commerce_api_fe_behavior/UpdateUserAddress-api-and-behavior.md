# Update User Address – API & Behavior

## 1. Business Goal

Buyer cap nhat dia chi trong address book (ten, SDT, ma hanh chinh, chi tiet, mac dinh). Thay doi address book **khong** lam mutate shipping address snapshots cua order/shipment da tao.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/addresses/{addressId}`
- **Auth:** Bearer JWT (`user_id` = buyer)

### Request body (partial update)

It nhat **mot** field phai co trong body.

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `receiver_name` | string | no | Toi da 255 ky tu |
| `phone` | string | no | Dinh dang VN (0/+84 + 9–10 so) |
| `province_code` | string | no | Ma tinh |
| `district_code` | string | no | Ma quan/huyen |
| `ward_code` | string | no | Ma phuong/xa |
| `address_detail` | string | no | Dia chi chi tiet |
| `is_default` | boolean | no | Dat lam mac dinh |

```json
{
  "receiver_name": "Tran Thi B",
  "phone": "0912345678",
  "is_default": true
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat dia chi giao hang thanh cong.",
  "data": {
    "address_id": "550e8400-e29b-41d4-a716-446655440000",
    "user_id": "660e8400-e29b-41d4-a716-446655440001",
    "receiver_name": "Tran Thi B",
    "phone": "0912345678",
    "province_code": "79",
    "district_code": "760",
    "ward_code": "26734",
    "address_detail": "123 Nguyen Van Linh, Q.7",
    "is_default": true,
    "created_at": "2026-05-21T14:00:00Z",
    "updated_at": "2026-05-21T15:45:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T15:45:01Z"
}
```

Response shape giong `POST /addresses` de FE cap nhat danh sach dia chi.

## 4. Server behavior

1. Tim `user_addresses` theo `addressId` + JWT `user_id`.
2. Merge field gui len voi gia tri hien tai; validate **ban ghi sau merge** (phone, do dai, khong blank).
3. Neu `is_default: true`: trong cung transaction unset default cac dia chi khac, roi update target.
4. Khong ghi outbox; khong sua shipment/order snapshots.

DB co partial unique index: `UNIQUE(user_id) WHERE is_default = TRUE`.

## 5. FE Behavior

- Form edit: chi gui field thay doi hoac gui full payload.
- Khong gui body rong `{}` — server tra 400 validation.
- Dat mac dinh: co the dung `is_default: true` tren PATCH nay hoac endpoint rieng `PATCH /addresses/{id}/default`.
- Checkout da tao order: snapshot van giu dia chi luc checkout.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-ADDRESS` | Khong ton tai hoac khong thuoc buyer |
| 400 | `COMMERCE-400-VALIDATION` | Body rong / field khong hop le sau merge |
| 400 | `COMMERCE-400-PHONE` | So dien thoai khong hop le |
| 409 | `COMMERCE-409-ADDRESS-DEFAULT` | Xung dot unique default (race) |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_UpdateUserAddress.md`
- Create: `CreateUserAddress-api-and-behavior.md`
- Set default: `SetDefaultUserAddress-api-and-behavior.md`
- Delete: `DeleteUserAddress-api-and-behavior.md`
