# Functional Requirement - View Product List

## 1. Feature Overview

Cho phep buyer xem danh sach san pham buyer-visible trong Commerce Service. Feature nay phuc vu product discovery, la read-only, khong reserve stock va khong thay doi cart/order.

## 2. Actors

- **Buyer/Guest:** Xem danh sach san pham.
- **System:** Enrich product cards voi price, media, inventory va rating summary.

## 3. Scope

**In Scope:**

- List product theo pagination.
- Filter mac dinh theo visibility rule.
- Return product card gom price/media/stock/rating summary.
- Ho tro sort co ban: newest, price asc/desc neu implemented.

**Out of Scope:**

- Search keyword.
- Filter category/shop rieng.
- Recommendation/personalization.
- Checkout/add cart.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/products`

**Auth:** Optional hoac Required theo API policy; MVP co the cho public read.

**Query params:**

- `page` / `cursor`
- `limit`
- `sort`

**Response data:**

- `items[]`
  - `product_id`
  - `title`
  - `thumbnail_url`
  - `shop_id`
  - `shop_name`
  - `category_id`
  - `condition`
  - `status`
  - `price`
  - `sale_price`
  - `effective_price`
  - `in_stock`
  - `low_stock`
  - `rating_avg`
  - `rating_count`
- pagination metadata.

## 5. Business Rules

- Chi hien product co `status IN (ACTIVE, OUT_OF_STOCK)`.
- Khong hien `DRAFT`, `PAUSED`, `ARCHIVED`, `REMOVED`.
- Shop phai `ACTIVE`.
- Category phai active.
- Product `OUT_OF_STOCK` co the hien nhung `in_stock = false`.
- Active price phai ton tai de hien product list.
- Khong expose `reserved_quantity`.

## 6. Database Impact

- Read `products`.
- Read `seller_shops`.
- Read `product_categories`.
- Read `product_media`.
- Read `product_prices`.
- Read `product_inventories`.
- Read review/rating summary.

## 7. Transaction

- Read-only.
- Khong lock inventory.

## 8. Security

- Neu public endpoint, chi tra public-safe fields.
- Khong tra provider/internal/moderation fields.

## 9. Failure Cases

- Invalid pagination/sort -> 400.
- DB timeout -> 500.

## 10. Acceptance Criteria

- Listing chi tra product buyer-visible.
- Product card co active price va inventory summary.
- Product out of stock duoc mark unavailable.
- Product cua shop suspended/closed khong hien thi.

