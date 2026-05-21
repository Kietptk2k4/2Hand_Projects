# Create User Address – API & Behavior

## 1. Business Goal

Buyer them dia chi giao hang vao address book. Dia chi dung cho checkout / tinh phi ship; shipment luu snapshot rieng.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/addresses`
- **Auth:** Bearer JWT (`user_id` = buyer)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `receiver_name` | string | yes | Toi da 255 ky tu |
| `phone` | string | yes | Dinh dang VN (0/+84 + 9–10 so) |
| `province_code` | string | yes | Ma tinh |
| `district_code` | string | yes | Ma quan/huyen |
| `ward_code` | string | yes | Ma phuong/xa |
| `address_detail` | string | yes | Dia chi chi tiet |
| `is_default` | boolean | no | Dat lam mac dinh |

```json
{
  "receiver_name": "Nguyen Van A",
  "phone": "0901234567",
  "province_code": "79",
  "district_code": "760",
  "ward_code": "26734",
  "address_detail": "123 Nguyen Van Linh, Q.7",
  "is_default": true
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Them dia chi giao hang thanh cong.",
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
    "updated_at": "2026-05-21T14:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T14:00:01Z"
}
```

## 4. FE Behavior

- **Dia chi dau tien** luon `is_default: true` (ke ca khi FE gui `false`).
- Khi `is_default: true`, he thong bo mac dinh cac dia chi cu trong cung transaction.
- Checkout: truyen `address_id` vao `CalculateOrderTotal` / `CheckoutFromCart`.
- Khong gui `user_id` tu client.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Payload khong hop le |
| 400 | `COMMERCE-400-PHONE` | So dien thoai khong hop le |
| 409 | `COMMERCE-409-ADDRESS-DEFAULT` | Xung dot default (race / unique index) |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_CreateUserAddress.md`
- Checkout: `CalculateOrderTotal-api-and-behavior.md`, `CheckoutFromCart-api-and-behavior.md`
