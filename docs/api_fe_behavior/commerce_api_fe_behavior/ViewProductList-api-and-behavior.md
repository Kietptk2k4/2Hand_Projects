# View Product List – API & Behavior

## 1. Business Goal

Buyer/guest xem danh sach san pham buyer-visible (discovery feed), read-only, co phan trang va sort co ban. Khong search/filter category/shop tren endpoint nay.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/products`
- **Auth:** Public (khong bat buoc JWT)

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |
| `sort` | string | `NEWEST` | `NEWEST`, `PRICE_ASC`, `PRICE_DESC` |

### Response `data`

| Field | Mo ta |
|-------|-------|
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
| `in_stock`, `low_stock` | Ton kho (khong co `reserved_quantity`) |
| `rating_avg`, `rating_count` | Rating shop |
| `shop_vacation`, `vacation_message` | Vacation mode |

## 3. Visibility rules

Chi tra product khi:

- `products.status = ACTIVE`
- `COALESCE(product_inventories.stock_quantity, 0) > 0` (con hang)
- `seller_shops.status = ACTIVE`
- `product_categories.is_active = true`
- Co **active price** tai thoi diem query

Product `OUT_OF_STOCK`, het ton kho, `DRAFT` / `PAUSED` / `ARCHIVED` / `REMOVED` va shop `SUSPENDED`/`CLOSED` **khong** xuat hien.

## 4. FE Behavior

- Mac dinh sort `NEWEST` (`created_at` DESC).
- Commerce home chi hien san pham con hang (`in_stock: true`, `status: ACTIVE`).
- Dung `pagination.has_next` cho load more.
- Tim kiem keyword → `GET /products/search`. Loc category → `GET /categories/{id}/products`.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 400 | `COMMERCE-400-PAGINATION` | `page` / `limit` khong hop le |
| 400 | `COMMERCE-400-VALIDATION` | `sort` khong hop le |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ViewProductList.md`
- Detail: `ViewProductDetail-api-and-behavior.md`
- Search: `SearchProduct-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-product-discovery.md` (5.1)
