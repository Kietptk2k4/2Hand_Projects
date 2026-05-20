# Functional Requirement - View User Addresses

## 1. Feature Overview

Cho phep buyer xem danh sach dia chi giao hang cua minh trong Commerce Service.

## 2. Actors

- **Buyer:** Xem address book cua minh.
- **System:** Query va sort addresses.

## 3. Scope

**In Scope:**

- List all addresses of authenticated buyer.
- Default address appears first.

**Out of Scope:**

- Create/update/delete address.
- Shipping snapshot.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/addresses`

**Auth:** Required (JWT)

**Response data:**

- `addresses[]`
  - `id`
  - `receiver_name`
  - `phone`
  - `province_code`
  - `district_code`
  - `ward_code`
  - `address_detail`
  - `is_default`
  - `created_at`
  - `updated_at`

## 5. Business Rules

- Buyer chi xem addresses cua minh.
- Default address sort first.
- Empty list allowed.

## 6. Database Impact

- Read `user_addresses` by `user_id`.

## 7. Transaction

- Read-only.

## 8. Security

- JWT required.
- `user_id` lay tu JWT.

## 9. Failure Cases

- Unauthenticated -> 401.
- DB failure -> 500.

## 10. Acceptance Criteria

- Buyer xem duoc list address cua minh.
- Khong hien address cua user khac.
- Default address dung o dau danh sach.
- Empty address book tra empty list.

