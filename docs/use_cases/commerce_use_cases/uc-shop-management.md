# UC - Shop Management

## 1. Overview

Use case nay mo ta nghiep vu seller tao va quan ly shop. Shop la root cho seller commerce capability: product, shipping profile, vacation mode, rating va moderation status. Product discovery, cart va checkout phai ton trong shop status.

## 2. Actors

- **Seller:** Tao/cap nhat shop, vacation mode, pickup profile.
- **Buyer:** Xem public shop profile.
- **Admin:** Suspend/close/restore shop.
- **System:** Sync cart/product availability khi shop status thay doi.

## 3. Related Data

- `seller_shops`
- `shop_settings`
- `seller_shipping_profiles`
- `products`
- `cart_items`
- `reviews`
- `outbox_events`

## 4. Business Rules

- Moi seller co toi da mot shop trong MVP.
- Seller chi quan ly shop cua minh.
- Shop `ACTIVE` moi duoc publish product va checkout.
- Shop `SUSPENDED/CLOSED` bi hide khoi buyer discovery va block checkout.
- Vacation mode co the hien thi product nhung MVP recommended block checkout.
- Seller khong duoc tu restore shop bi admin suspend.

## 5. Sub-Use Cases

### 5.1. Create Shop

**Preconditions:** User authenticated; user chua co shop.

**Main Flow:**

1. Seller submit shop name, description; `avatar_url`/`cover_url` optional (URL MinIO bucket `2hands-commerce-shop` sau khi FE upload).
2. System validate seller has no shop va URL bucket/domain neu co.
3. System create `seller_shops` status `ACTIVE`.
4. System create default `shop_settings`.
5. System create pickup profile neu payload co.
6. System ghi outbox event.

**Exception Flow:** Duplicate shop -> 409; invalid payload -> 400.

### 5.2. Update Shop Profile

**Preconditions:** Seller owns shop.

**Main Flow:**

1. Seller update shop name, description, avatar, cover (URL tro MinIO `2hands-commerce-shop`).
2. System validate ownership va URL hop le.
3. System update shop fields.
4. System ghi event neu can.

**Exception Flow:** Shop not found -> 404; seller not owner -> 403/404.

### 5.3. Update Vacation Mode

**Preconditions:** Seller owns shop.

**Main Flow:**

1. Seller set `is_vacation` and optional `vacation_message`.
2. System update `shop_settings`.
3. System tra settings moi.

**Postconditions:** Checkout can block new orders while vacation active.

### 5.4. Manage Seller Shipping Profile

**Preconditions:** Seller owns shop.

**Main Flow:**

1. Seller submit pickup name, phone, province/district/ward, detail.
2. System validate payload.
3. System upsert `seller_shipping_profiles`.
4. System tra pickup profile.

**Exception Flow:** Missing pickup fields -> 400.

### 5.5. View Public Shop

**Preconditions:** Shop exists and is public-visible.

**Main Flow:**

1. Buyer request shop profile.
2. System load shop, settings, rating summary.
3. System apply visibility rule.
4. System return public profile and product summary/page.

**Exception Flow:** Shop `SUSPENDED/CLOSED` -> 404/unavailable.

### 5.6. Admin Moderate Shop

**Preconditions:** Admin has permission.

**Main Flow:**

1. Admin suspend/close/restore shop.
2. System update shop status.
3. System invalidates cart items asynchronously or synchronously where needed.
4. System writes outbox event.

## 6. Acceptance Criteria

- Seller has at most one shop.
- Seller can update only own shop.
- Shop status is enforced in product publish and checkout.
- Vacation mode is visible to buyer and blocks checkout per MVP policy.
- Suspended/closed shop products do not appear in discovery.

