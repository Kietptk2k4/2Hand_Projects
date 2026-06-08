# Update Product Review – API & Behavior

## 1. Business Goal

Buyer cap nhat **rating** va/hoac **comment** cua review da tao. Neu rating doi, he thong tinh lai `seller_shops.rating_avg` / `rating_count`. Lien ket `order_item_id`, `buyer_id`, `seller_id` **khong** doi.

## 2. API Contract

- **Method:** PATCH
- **URL:** `/commerce/api/v1/reviews/{reviewId}`
- **Auth:** Bearer JWT (buyer — `user_id` = `reviews.buyer_id`)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `rating` | integer | no* | 1–5 neu gui |
| `comment` | string | no* | Cap nhat comment; `""` de xoa comment |

\* Phai co it nhat mot trong `rating` hoac `comment`.

```json
{
  "rating": 4,
  "comment": "Cap nhat sau khi dung them."
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat danh gia thanh cong.",
  "data": {
    "review_id": "990e8400-e29b-41d4-a716-446655440004",
    "order_item_id": "880e8400-e29b-41d4-a716-446655440003",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "buyer_id": "550e8400-e29b-41d4-a716-446655440000",
    "rating": 4,
    "comment": "Cap nhat sau khi dung them.",
    "status": "VISIBLE",
    "rating_changed": true,
    "created_at": "2026-05-20T10:00:00Z",
    "updated_at": "2026-05-21T10:00:00Z",
    "seller_rating_avg": 4.50,
    "seller_rating_count": 4
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. FE Behavior

- Chi cho phep sua review **VISIBLE** cua chinh buyer.
- Chi gui field can doi (PATCH partial).
- `rating_changed = false` khi chi sua comment — `seller_rating_*` lay tu shop hien tai, khong tinh lai aggregate.
- Media review: upload rieng (`FR_UploadReviewMedia`).

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-RATING` | Rating ngoai 1–5 |
| 400 | `COMMERCE-400-VALIDATION` | Khong gui rating hay comment |
| 404 | `COMMERCE-404-REVIEW` | Review khong ton tai / khong thuoc buyer |
| 409 | `COMMERCE-409-REVIEW-VISIBLE` | Review da `HIDDEN` (admin moderation) |

## 6. Events

- Outbox: `COMMERCE_REVIEW_UPDATED` → topic `commerce.review.updated` (cung transaction)

## 7. Related

- Create: `POST /commerce/api/v1/reviews` — `CreateProductReview-api-and-behavior.md`
- Load edit form: `ViewProductReviewForEdit-api-and-behavior.md`
- Admin hide/restore: `ModerateReview-api-and-behavior.md`
- FR: `docs/feature_requirements/commerce/FR_UpdateProductReview.md`
