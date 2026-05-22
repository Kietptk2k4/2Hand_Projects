# View Products By Shop – API & Behavior

## 1. Business Goal

Buyer/guest xem trang san pham public cua mot shop (storefront): shop summary, vacation info va danh sach product cards co phan trang.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/shops/{shopId}/products`
- **Auth:** Public (khong bat buoc JWT)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `shopId` | UUID | ID shop |

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |
| `sort` | string | `NEWEST` | `NEWEST`, `PRICE_ASC`, `PRICE_DESC` |

### Response `data`

| Field | Mo ta |
|-------|-------|
| `shop` | Thong tin shop public |
| `items[]` | Product cards (cung shape discovery) |
| `pagination` | `page`, `limit`, `total_items`, `total_pages`, `has_next` |

### Shop summary (`shop`)

| Field | Mo ta |
|-------|-------|
| `shop_id`, `shop_name`, `description` | Thong tin co ban |
| `avatar_url`, `cover_url` | MinIO `2hands-commerce-shop` |
| `rating_avg`, `rating_count` | Rating shop |
| `shop_vacation`, `vacation_message` | Vacation mode |

### Product card (`items[]`)

Giong `ViewProductList` / `FilterProductsByCategory`: `product_id`, `title`, `thumbnail_url`, `shop_id`, `shop_name`, `category_id`, `condition`, `status`, `price`, `sale_price`, `effective_price`, `in_stock`, `low_stock`, `rating_avg`, `rating_count`, `shop_vacation`, `vacation_message`.

**Khong** tra seller private fields (shipping profile, payout, ...).

## 3. Visibility rules

**Shop:**

- Phai ton tai va `seller_shops.status = ACTIVE`
- `SUSPENDED` / `CLOSED` → **404** `COMMERCE-404-SHOP`

**Products:**

- `products.status IN (ACTIVE, OUT_OF_STOCK)`
- `product_categories.is_active = true`
- Co active price
- Thuoc `shop_id` dang xem

## 4. FE Behavior

- Hien vacation banner khi `shop.shop_vacation = true`.
- Shop khong co san pham visible → `items: []`, van tra `shop` neu shop active.
- Dung `pagination.has_next` cho load more.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 404 | `COMMERCE-404-SHOP` | Shop khong ton tai hoac khong public-visible |
| 400 | `COMMERCE-400-PAGINATION` | `page` / `limit` khong hop le |
| 400 | `COMMERCE-400-VALIDATION` | `sort` khong hop le |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ViewProductsByShop.md`
- Product list: `ViewProductList-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-product-discovery.md` (5.5)
