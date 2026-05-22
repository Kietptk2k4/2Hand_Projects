# Search Product – API & Behavior

## 1. Business Goal

Buyer/guest tim san pham buyer-visible theo keyword (title, description, ten shop, ten category). MVP dung PostgreSQL `ILIKE`, khong semantic search.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/products/search`
- **Auth:** Public (khong bat buoc JWT)

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `q` | string | — | **Bat buoc.** Keyword (trim, gop khoang trang, min 2, max 255 ky tu) |
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |
| `sort` | string | `NEWEST` | `NEWEST`, `PRICE_ASC`, `PRICE_DESC` |

### Response `data`

| Field | Mo ta |
|-------|-------|
| `keyword` | Keyword da normalize |
| `items[]` | Product cards (cung shape voi filter category) |
| `pagination` | `page`, `limit`, `total_items`, `total_pages`, `has_next` |

### Product card (`items[]`)

Giong `FilterProductsByCategory`: `product_id`, `title`, `thumbnail_url`, `shop_id`, `shop_name`, `category_id`, `condition`, `status`, `price`, `sale_price`, `effective_price`, `in_stock`, `low_stock`, `rating_avg`, `rating_count`, `shop_vacation`, `vacation_message`.

## 3. Visibility rules

Chi tra product khi:

- `products.status IN (ACTIVE, OUT_OF_STOCK)`
- `seller_shops.status = ACTIVE`
- `product_categories.is_active = true`
- Co **active price** tai thoi diem query
- Keyword match (ILIKE) mot trong: `title`, `description`, `shop_name`, `category.name`

Product `DRAFT` / `PAUSED` / `ARCHIVED` / `REMOVED` va shop/category khong active **khong** xuat hien.

## 4. FE Behavior

- Debounce input; chi goi API khi `q.length >= 2`.
- Hien empty state khi `items` rong (khong phai loi).
- Out-of-stock van hien (`in_stock: false`).
- Dung `pagination.has_next` cho load more.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 400 | `COMMERCE-400-SEARCH-KEYWORD` | `q` rong, qua ngan (<2), qua dai (>255) |
| 400 | `COMMERCE-400-PAGINATION` | `page` / `limit` khong hop le |
| 400 | `COMMERCE-400-VALIDATION` | `sort` khong hop le |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_SearchProduct.md`
- Filter category: `FilterProductsByCategory-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-product-discovery.md` (5.3)
