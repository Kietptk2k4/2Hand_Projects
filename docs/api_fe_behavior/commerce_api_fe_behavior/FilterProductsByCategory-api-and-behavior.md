# Filter Products By Category – API & Behavior

## 1. Business Goal

Buyer/guest loc san pham buyer-visible theo category. Ho tro loc ca cay con (subtree) qua `path` khi `include_children=true`.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/categories/{categoryId}/products`
- **Auth:** Public (khong bat buoc JWT)

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |
| `sort` | string | `NEWEST` | `NEWEST`, `PRICE_ASC`, `PRICE_DESC` |
| `include_children` | boolean | `true` | Loc ca category con (subtree) |

### Response `data`

| Field | Mo ta |
|-------|-------|
| `category_id` | Category dang loc |
| `category_name` | Ten category |
| `category_slug` | Slug |
| `include_children` | Co loc subtree hay khong |
| `items[]` | Product cards |
| `pagination` | `page`, `limit`, `total_items`, `total_pages`, `has_next` |

### Product card (`items[]`)

| Field | Mo ta |
|-------|-------|
| `product_id` | UUID |
| `title` | Tieu de |
| `thumbnail_url` | Anh chinh (MinIO `2hands-commerce-product`) |
| `shop_id`, `shop_name` | Shop |
| `category_id`, `condition`, `status` | `ACTIVE` / `OUT_OF_STOCK` |
| `price`, `sale_price`, `effective_price` | Gia active |
| `in_stock`, `low_stock` | Ton kho |
| `rating_avg`, `rating_count` | Rating shop |
| `shop_vacation`, `vacation_message` | Vacation mode |

## 3. Visibility rules

Chi tra product khi:

- `products.status IN (ACTIVE, OUT_OF_STOCK)`
- `seller_shops.status = ACTIVE`
- `product_categories.is_active = true`
- Co **active price** (`product_prices` hop le tai thoi diem query)

Category **inactive** hoac khong ton tai → **404** `COMMERCE-404-CATEGORY`.

## 4. FE Behavior

- Mac dinh `include_children=true` de loc parent + descendants.
- Out-of-stock van hien (`in_stock: false`) — chan add cart o flow khac.
- Dung `pagination.has_next` cho infinite scroll / load more.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 400 | `COMMERCE-400-PAGINATION` | `page` / `limit` khong hop le |
| 400 | `COMMERCE-400-VALIDATION` | `sort` khong hop le |
| 404 | `COMMERCE-404-CATEGORY` | Category khong ton tai / inactive |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_FilterProductsByCategory.md`
- List chung: `FR_ViewProductList.md` (endpoint `/products` rieng)
