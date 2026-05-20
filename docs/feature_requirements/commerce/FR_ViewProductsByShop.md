# Functional Requirement - View Products By Shop

## 1. Feature Overview

Cho phep buyer xem danh sach san pham public cua mot shop. Feature nay dung cho shop page va seller storefront trong Commerce Service.

## 2. Actors

- **Buyer/Guest:** Xem product cua shop.
- **System:** Load shop public info va product list.

## 3. Scope

**In Scope:**

- View public shop products.
- Return shop summary, vacation info va product cards.
- Pagination/filter by product status visible.

**Out of Scope:**

- Seller product management.
- Shop update.
- Seller order management.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/shops/{shopId}/products`

**Auth:** Optional hoac Required theo API policy.

**Query params:**

- `page` / `cursor`
- `limit`
- optional `sort`

## 5. Business Rules

- Shop phai ton tai va `ACTIVE`.
- Shop `SUSPENDED/CLOSED` khong public-visible.
- Chi tra product `ACTIVE/OUT_OF_STOCK`.
- Product `PAUSED/ARCHIVED/REMOVED/DRAFT` khong tra ve.
- Include `is_vacation` va `vacation_message`.

## 6. Database Impact

- Read `seller_shops`.
- Read `shop_settings`.
- Read `products`.
- Read media, price, inventory, rating summary.

## 7. Transaction

- Read-only.

## 8. Security

- Public-safe fields only.
- Khong expose seller private payout/shipping profile.

## 9. Failure Cases

- Shop not found/not public-visible -> 404.
- Invalid pagination -> 400.

## 10. Acceptance Criteria

- Active shop tra product visible.
- Suspended/closed shop khong public-visible.
- Vacation info duoc include.
- Product card co price/stock/rating summary.

