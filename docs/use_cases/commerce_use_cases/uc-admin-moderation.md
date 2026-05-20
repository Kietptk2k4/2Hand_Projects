# UC - Admin Moderation

## 1. Overview

Use case nay mo ta cac nghiep vu moderation cua Admin trong Commerce Service: remove product, suspend/close shop, hide review va support-read order/shipment. Admin action phai co permission, ghi event/audit, va khong lam thay doi lich su order snapshots.

## 2. Actors

- **Admin/Moderator:** Thuc hien moderation.
- **Seller:** Bi tac dong boi shop/product moderation.
- **Buyer:** Bi tac dong neu cart co product/shop invalid.
- **System:** Sync cart invalid va publish event.

## 3. Related Data

- `products`
- `seller_shops`
- `reviews`
- `cart_items`
- `orders`
- `order_items`
- `shipments`
- `outbox_events`

## 4. Business Rules

- Admin API require permission tu Auth Service.
- Product `REMOVED` khong buyer-visible va seller khong republish duoc.
- Shop `SUSPENDED/CLOSED` block product publish, add cart va checkout.
- Review `HIDDEN` khong hien public.
- Existing order snapshots khong bi sua khi moderation.
- Checkout phai revalidate product/shop live, khong chi dua vao cart sync.

## 5. Sub-Use Cases

### 5.1. Remove Product

**Main Flow:**

1. Admin request remove product voi reason.
2. System check permission.
3. System set product `REMOVED`.
4. System mark related active cart items `INVALID_PRODUCT` hoac emit sync event.
5. System writes outbox event.

**Exception Flow:** Missing permission -> 403; product not found -> 404.

### 5.2. Suspend Or Close Shop

**Main Flow:**

1. Admin request suspend/close shop.
2. System check permission.
3. System update shop status.
4. System invalidates cart items for shop products.
5. System writes outbox event.

**Exception Flow:** Shop not found -> 404; invalid transition -> 409.

### 5.3. Restore Shop

**Main Flow:**

1. Admin restores suspended shop.
2. System validates permission and current status.
3. System sets shop `ACTIVE`.
4. System writes event.

**Postconditions:** Products still need own valid status/readiness.

### 5.4. Hide Or Restore Review

**Main Flow:**

1. Admin request hide/restore review.
2. System checks permission.
3. System updates review status.
4. System recalculates rating summary if needed.
5. System writes event.

**Exception Flow:** Review not found -> 404.

### 5.5. Support Read Order/Shipment

**Main Flow:**

1. Admin searches order/shipment.
2. System checks support-read permission.
3. System returns status, histories, webhook/provider summary.

**Postconditions:** Read-only; no payment/order mutation in MVP.

## 6. Acceptance Criteria

- Admin actions require permission.
- Removed product is hidden and checkout-blocked.
- Suspended/closed shop blocks new commerce activity.
- Hidden review is excluded from public lists.
- Existing order snapshots are preserved.

