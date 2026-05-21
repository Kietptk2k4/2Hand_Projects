# Functional Requirement - Create Shop

## 1. Feature Overview

Cho phep user tao seller shop trong Commerce Service. Moi user chi co toi da mot shop trong MVP.

## 2. Actors

- **Seller:** Tao shop.
- **System:** Validate uniqueness and create default settings.

## 3. Scope

**In Scope:**

- Create `seller_shops`.
- Create default `shop_settings`.
- Optional create pickup/shipping profile if provided.

**Out of Scope:**

- KYC/onboarding advanced.
- Payout account setup.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/shop`

**Auth:** Required (JWT)

**Request body:**

- `shop_name`
- `description` optional
- `avatar_url` optional
- `cover_url` optional

## 5. Business Rules

- `seller_id` comes from JWT.
- One shop per seller in MVP.
- Initial shop status `ACTIVE`.
- Default `shop_settings.is_vacation = false`.
- Shop name required.

## 5.1 MinIO

- `avatar_url` / `cover_url` (optional) tro object bucket **`2hands-commerce-shop`** (MinIO shared). FE upload presigned truoc, API chi luu URL.

## 6. Database Impact

- Insert `seller_shops`.
- Insert `shop_settings`.
- Optional insert `seller_shipping_profiles`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Client cannot provide arbitrary `seller_id`.

## 9. Failure Cases

- Seller already has shop -> 409.
- Invalid payload -> 400.
- Unauthenticated -> 401.

## 10. Acceptance Criteria

- Authenticated user can create one shop.
- Duplicate shop creation is rejected.
- Default shop settings are created.

