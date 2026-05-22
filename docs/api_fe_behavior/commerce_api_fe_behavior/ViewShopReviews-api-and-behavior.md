# View Shop Reviews – API & Behavior

## 1. Business Goal

Seller xem danh sach review cua shop minh de theo doi rating, feedback, media va reply da gui. Read-only; khong sua rating/comment buyer.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/seller/reviews`
- **Auth:** Bearer JWT (seller)

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |
| `rating` | int | — | Loc 1–5 (optional) |
| `status` | string | `VISIBLE` | `VISIBLE` hoac `HIDDEN` |

### Response `data`

| Field | Mo ta |
|-------|-------|
| `shop_id` | UUID shop cua seller |
| `rating_summary` | `rating_avg`, `rating_count` (theo `status` filter, khong loc `rating`) |
| `reviews[]` | Trang review |
| `pagination` | `page`, `limit`, `total_items`, `total_pages`, `has_next` |

### Review item (`reviews[]`)

| Field | Mo ta |
|-------|-------|
| `review_id` | UUID |
| `order_item_id` | UUID order item |
| `product_name_snapshot` | Ten san pham luc mua |
| `rating` | 1–5 |
| `comment` | Nullable |
| `status` | `VISIBLE` / `HIDDEN` |
| `created_at` | Thoi gian tao |
| `media[]` | `media_id`, `url`, `media_type` |
| `seller_reply` | `reply_id`, `content`, `created_at` hoac `null` |

**Khong** tra `buyer_id`, moderation fields.

## 3. Business rules

- Scope: `reviews.seller_id = JWT user_id` (chi review shop cua seller).
- Seller phai co shop → neu khong: **409** `COMMERCE-409-SELLER-SHOP`.
- Mac dinh chi review `VISIBLE` (MVP). Truyen `status=HIDDEN` de xem review bi an (ho tro seller).
- Sap xep mac dinh: `created_at DESC` (NEWEST).
- `rating_summary` tinh tren tat ca review match `status` (khong chi trang hien tai, khong loc `rating`).
- Seller khong mutate review qua endpoint nay.

## 4. FE Behavior

- Man hinh quan ly danh gia shop; ket hop `POST .../reply` de tra loi.
- Tab loc sao: query `rating=1` … `rating=5`.
- Mac dinh list `VISIBLE`; tuy chinh `HIDDEN` neu can debug moderation.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 409 | `COMMERCE-409-SELLER-SHOP` | Seller chua co shop |
| 400 | `COMMERCE-400-PAGINATION` | `page` / `limit` khong hop le |
| 400 | `COMMERCE-400-RATING` | `rating` ngoai 1–5 |
| 400 | `COMMERCE-400-VALIDATION` | `status` khong hop le |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ViewShopReviews.md`
- Reply: `ReplyToReview-api-and-behavior.md`
- Product reviews (public): `ViewProductReviews-api-and-behavior.md`
- Flow: `docs/business_flow/commerce_business_flow/review-lifecycle-flow.md` (10)
