# Create Product Review – API & Behavior

## 1. Business Goal

Buyer danh gia san pham sau khi **order item** da `COMPLETED`. Mot order item toi da mot review. `seller_id` lay tu order item, khong tu client.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/reviews`
- **Auth:** Bearer JWT (buyer)

### Request body

| Field           | Type    | Required | Mo ta                    |
|-----------------|---------|----------|--------------------------|
| `order_item_id` | UUID    | yes      | Dong hang da hoan thanh  |
| `rating`        | integer | yes      | 1–5                      |
| `comment`       | string  | no       | Nhan xet                 |

```json
{
  "order_item_id": "880e8400-e29b-41d4-a716-446655440003",
  "rating": 5,
  "comment": "San pham dung mo ta, giao nhanh."
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao danh gia san pham thanh cong.",
  "data": {
    "review_id": "990e8400-e29b-41d4-a716-446655440004",
    "order_item_id": "880e8400-e29b-41d4-a716-446655440003",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "buyer_id": "550e8400-e29b-41d4-a716-446655440000",
    "rating": 5,
    "comment": "San pham dung mo ta, giao nhanh.",
    "status": "VISIBLE",
    "created_at": "2026-05-21T10:00:00Z",
    "seller_rating_avg": 4.67,
    "seller_rating_count": 3
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. FE Behavior

- Chi goi sau khi buyer da **confirm received** / order item `COMPLETED`.
- Media review: upload rieng (`FR_UploadReviewMedia`, bucket `2hands-commerce-review`) — khong trong request create MVP.
- Redirect/webhook **khong** lien quan review.

## 5. Business Rules

- `order_items.status = COMPLETED`
- `orders.buyer_id` = JWT `user_id`
- `reviews.status` = `VISIBLE` luc tao
- Cap nhat `seller_shops.rating_avg`, `rating_count` (chi review `VISIBLE`)
- Outbox: `COMMERCE_REVIEW_CREATED`

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-RATING` | Rating ngoai 1–5 |
| 400 | `COMMERCE-400-VALIDATION` | Thieu field bat buoc |
| 404 | `COMMERCE-404-ORDER-ITEM` | Item khong ton tai / khong thuoc buyer |
| 409 | `COMMERCE-409-ORDER-ITEM-REVIEW` | Item chua `COMPLETED` |
| 409 | `COMMERCE-409-REVIEW-EXISTS` | Da co review cho item |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_CreateProductReview.md`
- Form context: `ViewReviewContext-api-and-behavior.md`
- Media: `FR_UploadReviewMedia` (khi co)
