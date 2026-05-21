# Functional Requirement - Update Shop Profile

## 1. Feature Overview

Cho phep seller cap nhat thong tin public profile cua shop minh.

## 2. Actors

- **Seller:** Cap nhat shop profile.
- **System:** Validate ownership and persist changes.

## 3. Scope

**In Scope:**

- Update shop name, description, avatar, cover.

**Out of Scope:**

- Vacation mode.
- Shipping profile.
- Shop moderation status.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/seller/shop`

**Auth:** Required (JWT seller)

**Request body:**

- `shop_name` optional
- `description` optional
- `avatar_url` optional
- `cover_url` optional

## 5. Business Rules

- Seller can update only own shop.
- Shop name cannot be blank if provided.
- Suspended shop may be allowed to edit profile but remains not sellable.
- Seller cannot change `status`, `rating_avg`, or `rating_count`.

## 5.1 MinIO

- `avatar_url` / `cover_url` tro bucket **`2hands-commerce-shop`** (MinIO shared). Validate URL prefix/domain khi cap nhat.

## 6. Database Impact

- Read/update `seller_shops`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership derived from JWT seller id.

## 9. Failure Cases

- Shop not found -> 404.
- Invalid payload -> 400.
- Seller not owner -> 403/404.

## 10. Acceptance Criteria

- Seller updates own shop profile.
- Status/rating fields cannot be changed by seller.
- Updated profile appears in public shop view.

