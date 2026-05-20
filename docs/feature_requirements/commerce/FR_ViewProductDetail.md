# Functional Requirement - View Product Detail

## 1. Feature Overview

Cho phep buyer xem chi tiet mot san pham buyer-visible, bao gom thong tin product, shop, category, media, active price, inventory summary, attributes va review summary.

## 2. Actors

- **Buyer/Guest:** Xem chi tiet san pham.
- **System:** Load va enrich product detail.

## 3. Scope

**In Scope:**

- View product detail by product id/slug.
- Return media, attributes, price, stock, shop info, review summary.
- Return vacation mode/message neu shop dang vacation.

**Out of Scope:**

- Add cart.
- Checkout.
- Seller product edit.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/products/{productId}`

**Auth:** Optional hoac Required theo API policy.

**Response data:**

- `product_id`
- `title`
- `description`
- `condition`
- `weight_gram`
- `status`
- `category`
- `shop`
- `media[]`
- `attributes[]`
- `price`
- `sale_price`
- `effective_price`
- `inventory_summary`
- `rating_avg`
- `rating_count`
- `shop_vacation`
- `vacation_message`

## 5. Business Rules

- Product phai ton tai.
- Buyer chi xem product `ACTIVE` hoac `OUT_OF_STOCK`.
- Product cua shop `SUSPENDED/CLOSED` khong duoc hien.
- Product `PAUSED/ARCHIVED/REMOVED/DRAFT` nen tra 404 cho buyer.
- Active price missing -> product unavailable hoac 409 theo API policy.
- Checkout van phai revalidate product/price/stock.

## 6. Database Impact

- Read `products`.
- Read `seller_shops`, `shop_settings`.
- Read `product_categories`.
- Read `product_media`.
- Read `product_prices`.
- Read `product_inventories`.
- Read `product_attributes`.
- Read review summary.

## 7. Transaction

- Read-only.
- Khong lock inventory.

## 8. Security

- Khong expose internal inventory reserved quantity.
- Khong expose moderation/provider fields.

## 9. Failure Cases

- Product not found/not visible -> 404.
- Invalid product id -> 400.
- DB timeout -> 500.

## 10. Acceptance Criteria

- Detail API tra du thong tin buyer can de quyet dinh mua.
- Product hidden statuses khong visible cho buyer.
- Out-of-stock product hien thi ro unavailable.
- Shop vacation info duoc include neu co.

