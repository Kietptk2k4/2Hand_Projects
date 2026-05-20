# Functional Requirement - Create User Address

## 1. Feature Overview

Cho phep buyer them dia chi giao hang vao address book cua minh. Dia chi nay co the duoc dung trong checkout de tao shipping address snapshot.

## 2. Actors

- **Buyer:** Tao dia chi moi.
- **System:** Validate payload va quan ly default address.

## 3. Scope

**In Scope:**

- Create address.
- Auto set default neu la address dau tien.
- Set as default neu request yeu cau.

**Out of Scope:**

- Shipping snapshot creation.
- External geocoding.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/addresses`

**Auth:** Required (JWT)

**Request body:**

- `receiver_name`
- `phone`
- `province_code`
- `district_code`
- `ward_code`
- `address_detail`
- `is_default` optional

## 5. Business Rules

- `user_id` lay tu JWT.
- Required fields khong duoc rong.
- First address auto default.
- Neu `is_default = true`, unset default cu trong cung transaction.
- Moi user toi da mot default address.

## 6. Database Impact

- Insert `user_addresses`.
- Update existing default addresses neu can.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Client khong duoc truyen `user_id`.

## 9. Failure Cases

- Invalid payload -> 400.
- Default unique conflict -> retry/409.

## 10. Acceptance Criteria

- Buyer tao duoc address hop le.
- First address la default.
- Set default clear default cu.
- Address thuoc dung buyer tu JWT.

