# Functional Requirement - Delete User Address

## 1. Feature Overview

Cho phep buyer xoa dia chi khoi address book cua minh. Xoa address khong anh huong den shipping address snapshots da duoc tao cho shipment/order truoc do.

## 2. Actors

- **Buyer:** Xoa dia chi cua minh.
- **System:** Validate ownership va quan ly default address sau khi xoa.

## 3. Scope

**In Scope:**

- Delete address by id.
- Reassign default address neu address bi xoa la default va con address khac.

**Out of Scope:**

- Delete shipping snapshots.
- Cancel order/shipment.

## 4. API Contract

**Endpoint:** `DELETE /commerce/api/v1/addresses/{addressId}`

**Auth:** Required (JWT)

## 5. Business Rules

- Buyer chi xoa address cua minh.
- Xoa address da dung trong shipment van allowed vi shipment dung snapshot.
- Neu delete default address, system co the set address khac lam default.
- Neu khong con address, buyer khong co default address.

## 6. Database Impact

- Read `user_addresses` by `id` and `user_id`.
- Delete address.
- Optional update another address `is_default = true`.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership check bat buoc.

## 9. Failure Cases

- Address not found/not owned -> 404.

## 10. Acceptance Criteria

- Buyer delete duoc own address.
- Other buyer address khong delete duoc.
- Shipment snapshots khong bi anh huong.
- Default state sau delete hop le.

