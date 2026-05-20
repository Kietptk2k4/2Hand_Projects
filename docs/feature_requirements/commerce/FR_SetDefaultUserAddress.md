# Functional Requirement - Set Default User Address

## 1. Feature Overview

Cho phep buyer chon mot dia chi trong address book lam dia chi mac dinh cho checkout.

## 2. Actors

- **Buyer:** Chon default address.
- **System:** Dam bao moi buyer chi co mot default address.

## 3. Scope

**In Scope:**

- Set target address `is_default = true`.
- Unset all other default addresses cua buyer.

**Out of Scope:**

- Create/update address fields.
- Update shipping snapshots.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/addresses/{addressId}/default`

**Auth:** Required (JWT)

## 5. Business Rules

- Address phai thuoc buyer.
- Operation atomic.
- Sau operation, buyer chi co mot default address.

## 6. Database Impact

- Read target `user_addresses`.
- Update all user addresses `is_default = false`.
- Update target `is_default = true`.

## 7. Transaction

- Write transaction required.
- Partial unique index `UNIQUE(user_id) WHERE is_default = true` recommended.

## 8. Security

- JWT required.
- Ownership check bat buoc.

## 9. Failure Cases

- Address not found/not owned -> 404.
- Unique conflict -> retry/409.

## 10. Acceptance Criteria

- Buyer set duoc default address cua minh.
- Default cu bi unset.
- Other user's address khong set duoc.
- Shipping snapshots khong thay doi.

