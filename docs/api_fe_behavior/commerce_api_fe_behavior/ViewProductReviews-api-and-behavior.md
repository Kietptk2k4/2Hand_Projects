# View Product Reviews - API & Behavior

## 1. Business Goal

Buyer/guest xem danh sach review **VISIBLE** cua san pham buyer-visible, kem rating summary, media va seller reply. Read-only.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/products/{productId}/reviews`
- **Auth:** Public (khong bat buoc JWT)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `productId` | UUID | ID san pham |

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |
| `rating` | int | — | Loc 1–5 (optional) |
| `sort` | string | `NEWEST` | `NEWEST`, `OLDEST`, `RATING_DESC`, `RATING_ASC` |

### Response `data`

| Field | Mo ta |
|-------|-------|
| `product_id` | UUID san pham |
| `shop` | Shop cua san pham: `shop_id`, `shop_name`, `avatar_url`, `seller_id` (nullable neu khong load duoc) |
| `rating_summary` | `rating_avg`, `rating_count` (chi review `VISIBLE` cua product) |
| `reviews[]` | Trang review |
| `pagination` | `page`, `limit`, `total_items`, `total_pages`, `has_next` |

### Review item (`reviews[]`)

| Field | Mo ta |
|-------|-------|
| `review_id` | UUID |
| `buyer_id` | UUID buyer — FE link toi social profile |
| `buyer_display_name` | Ten hien thi (Auth profile hoac fallback `Người mua`) |
| `buyer_avatar_url` | URL avatar buyer (nullable) |
| `rating` | 1–5 |
| `comment` | Nullable |
| `created_at` | Thoi gian tao |
| `media[]` | `media_id`, `url` (MinIO `2hands-commerce-review`), `media_type` |
| `seller_reply` | `reply_id`, `content`, `created_at` hoac `null` |

**Khong** tra `seller_id` tren review item, review `HIDDEN`, moderation fields.

## 3. Visibility rules

**Product** phai buyer-visible (giong discovery):

- `products.status IN (ACTIVE, OUT_OF_STOCK)`
- `seller_shops.status = ACTIVE`
- `product_categories.is_active = true`
- Co active price

**Reviews:** chi `reviews.status = VISIBLE`, join `order_items.product_id = productId`.

Product khong visible → **404** `COMMERCE-404-PRODUCT`. Khong co review → `reviews: []`, `rating_count: 0`.

## 4. FE Behavior

- Mac dinh sort `NEWEST`.
- Dung `rating` filter cho tab 1–5 sao.
- `rating_summary` tinh tren tat ca visible reviews (khong chi trang hien tai).
- Hidden review khong bao gio xuat hien trong list.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 404 | `COMMERCE-404-PRODUCT` | Product khong ton tai / khong buyer-visible |
| 400 | `COMMERCE-400-PAGINATION` | `page` / `limit` khong hop le |
| 400 | `COMMERCE-400-RATING` | `rating` ngoai 1–5 |
| 400 | `COMMERCE-400-VALIDATION` | `sort` khong hop le |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ViewProductReviews.md`
- Create review: `CreateProductReview-api-and-behavior.md`
- Product detail: `ViewProductDetail-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-review-management.md` (5.3)
