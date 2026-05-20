# Functional Requirement - Update User Address

## 1. Feature Overview

Cho phep buyer cap nhat dia chi trong address book cua minh. Viec update address book khong lam thay doi shipping address snapshots cua order/shipment da tao.

## 2. Actors

- **Buyer:** Cap nhat dia chi cua minh.
- **System:** Validate ownership va payload.

## 3. Scope

**In Scope:**

- Update receiver/phone/location/detail.
- Optional set address as default.

**Out of Scope:**

- Update shipping snapshots.
- Validate address voi provider ngoai.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/addresses/{addressId}`

**Auth:** Required (JWT)

**Request body:** Partial or full address fields.

## 5. Business Rules

- Buyer chi update address cua minh.
- Required fields sau update phai hop le.
- Neu set default, unset default cu atomically.
- Existing shipment snapshots khong thay doi.

## 6. Database Impact

- Read `user_addresses` by `id` and `user_id`.
- Update address fields.
- Update default flags neu can.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership check bat buoc.

## 9. Failure Cases

- Address not found/not owned -> 404.
- Invalid payload -> 400.

## 10. Acceptance Criteria

- Buyer update duoc own address.
- Other buyer address khong update duoc.
- Default uniqueness duoc giu.
- Shipping snapshots khong bi mutate.

