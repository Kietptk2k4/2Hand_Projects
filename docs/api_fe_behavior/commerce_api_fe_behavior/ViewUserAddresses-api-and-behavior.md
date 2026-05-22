# View User Addresses – API & Behavior

## 1. Business Goal

Buyer xem toan bo dia chi giao hang trong address book de chon khi checkout hoac quan ly. Read-only; khong tao/sua/xoa qua endpoint nay.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/addresses`
- **Auth:** Bearer JWT (`user_id` = buyer)

### Response `data`

| Field | Mo ta |
|-------|-------|
| `addresses[]` | Danh sach dia chi cua user |

### Address item (`addresses[]`)

| Field | Mo ta |
|-------|-------|
| `id` | UUID dia chi |
| `receiver_name` | Ten nguoi nhan |
| `phone` | So dien thoai |
| `province_code` | Ma tinh |
| `district_code` | Ma quan/huyen |
| `ward_code` | Ma phuong/xa |
| `address_detail` | Dia chi chi tiet |
| `is_default` | Dia chi mac dinh |
| `created_at` | Thoi gian tao |
| `updated_at` | Thoi gian cap nhat |

**Khong** tra `user_id` trong list item (chi scope theo JWT).

## 3. Business rules

- Query: `user_addresses.user_id = JWT user_id`.
- Sap xep: `is_default DESC`, sau do `updated_at DESC`, `created_at DESC` (mac dinh len dau).
- Khong co dia chi → `addresses: []` (200 OK).
- Buyer khong xem dia chi user khac.

## 4. FE Behavior

- Man hinh address book / chon dia chi checkout.
- Highlight item `is_default: true` (phan tu dau neu sort dung).
- Ket hop POST/PATCH/DELETE tren cung base path de CRUD.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ViewUserAddresses.md`
- Create: `CreateUserAddress-api-and-behavior.md`
- Update: `UpdateUserAddress-api-and-behavior.md`
- Set default: `SetDefaultUserAddress-api-and-behavior.md`
- Delete: `DeleteUserAddress-api-and-behavior.md`
