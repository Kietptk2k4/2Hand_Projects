# Functional Requirement - Update Shop Vacation Mode

## 1. Feature Overview

Cho phep seller bat/tat vacation mode va cap nhat vacation message cho shop. MVP recommended block checkout khi shop dang vacation.

## 2. Actors

- **Seller:** Quan ly vacation mode.
- **System:** Persist setting and expose it in discovery/checkout validation.

## 3. Scope

**In Scope:**

- Update `is_vacation`.
- Update `vacation_message`.
- Return new shop settings.

**Out of Scope:**

- Close/suspend shop.
- Auto-cancel existing orders.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/seller/shop/vacation`

**Auth:** Required (JWT seller)

**Request body:**

- `is_vacation`
- `vacation_message` optional

## 5. Business Rules

- Seller can update only own shop settings.
- Vacation mode does not change shop status.
- Existing orders remain visible and must follow fulfillment policy.
- Product discovery can show products with vacation notice.
- Checkout should block new orders for vacation shop in MVP.

## 6. Database Impact

- Read `seller_shops`.
- Update `shop_settings`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Seller ownership check required.

## 9. Failure Cases

- Shop not found -> 404.
- Invalid payload -> 400.

## 10. Acceptance Criteria

- Seller toggles vacation mode for own shop.
- Vacation message is returned in buyer-facing shop/product responses.
- Checkout validation can detect vacation mode.

