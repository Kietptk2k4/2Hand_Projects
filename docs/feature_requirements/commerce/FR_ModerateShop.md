# Functional Requirement - Moderate Shop

## 1. Feature Overview

Cho phep admin suspend, close hoac restore shop. Shop moderation anh huong product discovery, add cart, checkout va seller product publishing.

## 2. Actors

- **Admin/Moderator:** Moderate shop.
- **System:** Update shop status and enforce availability.

## 3. Scope

**In Scope:**

- Set shop `SUSPENDED`, `CLOSED`, or restore `ACTIVE`.
- Block new commerce activity for non-active shop.
- Emit moderation event.

**Out of Scope:**

- Seller payout/refund.
- Auto-cancel existing paid orders.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/admin/shops/{shopId}/moderate`

**Auth:** Required (JWT admin permission)

**Request body:**

- `action`: `SUSPEND`, `CLOSE`, `RESTORE`
- `reason`

## 5. Business Rules

- Admin permission required.
- `SUSPENDED/CLOSED` shop hidden from buyer discovery.
- Checkout must block products of non-active shop.
- Existing orders remain for support; do not mutate snapshots.
- Restore does not automatically republish archived/removed products.

## 6. Database Impact

- Read/update `seller_shops`.
- Optional update related cart items to `INVALID_PRODUCT`.
- Insert outbox event.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT admin required.
- Permission such as `COMMERCE_SHOP_SUSPEND`/`COMMERCE_SHOP_CLOSE` required.

## 9. Failure Cases

- Missing permission -> 403.
- Shop not found -> 404.
- Invalid transition/action -> 400/409.

## 10. Acceptance Criteria

- Admin can suspend/close/restore shop with permission.
- Non-active shop blocks discovery/checkout.
- Existing order snapshots remain unchanged.

