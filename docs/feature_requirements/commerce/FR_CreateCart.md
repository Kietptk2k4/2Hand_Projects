# Functional Requirement - Create Cart

## 1. Feature Overview

Dam bao moi buyer co mot cart duy nhat de quan ly san pham truoc checkout. Cart co the duoc tao tu dong khi buyer add item hoac view cart lan dau.

## 2. Actors

- **Buyer:** Can cart de them/xem san pham.
- **System:** Get-or-create cart idempotently.

## 3. Scope

**In Scope:**

- Create cart for authenticated buyer.
- Return existing cart if already exists.
- Handle concurrent create safely.

**Out of Scope:**

- Add item.
- Checkout.
- Inventory reservation.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/cart`

**Auth:** Required (JWT)

**Request body:** Empty.

**Response data:**

- `cart_id`
- `user_id`
- `items`
- `created_at`
- `updated_at`

## 5. Business Rules

- `carts.user_id` unique.
- User id lay tu JWT.
- Create cart is idempotent: neu cart da ton tai, tra cart hien co.
- Cart creation khong tao cart item.
- Cart creation khong reserve stock.

## 6. Database Impact

- Read `carts` by `user_id`.
- Insert `carts` neu chua ton tai.

## 7. Transaction

- Write transaction required.
- Handle unique conflict by reloading cart.

## 8. Security

- JWT required.
- Khong cho client truyen `user_id`.

## 9. Failure Cases

- Unauthenticated -> 401.
- DB unique race -> reload existing cart.
- DB failure -> 500.

## 10. Acceptance Criteria

- Buyer co toi da mot cart.
- Calling create multiple times returns same cart.
- Concurrent create does not duplicate cart.
- Inventory khong thay doi.

