# View Product Review For Edit - API & Behavior

## 1. Business Goal

Buyer lay **chi tiet review de sua**: rating, comment, trang thai, media count va snapshot san pham/order item. Dung cho form edit review (`PATCH /reviews/{reviewId}`).

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/reviews/{reviewId}`
- **Auth:** Bearer JWT (buyer — `reviews.buyer_id`)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `reviewId` | UUID | ID review |

> Route `/reviews/context` phai duoc map rieng (query param), khong conflict voi `{reviewId}`.

## 3. Response - Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay chi tiet danh gia thanh cong.",
  "data": {
    "review_id": "990e8400-e29b-41d4-a716-446655440004",
    "order_item_id": "880e8400-e29b-41d4-a716-446655440003",
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "product_id": "660e8400-e29b-41d4-a716-446655440001",
    "rating": 5,
    "comment": "San pham dung mo ta, giao nhanh.",
    "status": "VISIBLE",
    "created_at": "2026-05-20T10:00:00Z",
    "updated_at": "2026-05-20T10:00:00Z",
    "media_count": 2,
    "product_name_snapshot": "iPhone 15",
    "image_snapshot": "http://localhost:9000/2hands-commerce-product/p1.jpg",
    "shop_name_snapshot": "Tech Shop",
    "final_price": 1000000,
    "completed_at": "2026-05-20T10:00:00Z"
  }
}
```

| Field | Mo ta |
|-------|-------|
| `media_count` | So file da upload (`review_media`) — FE tinh quota upload |
| Snapshot fields | Tu `order_items`, read-only |

## 4. FE Behavior

- Goi khi vao trang edit review (`mode = edit`).
- Pre-fill rating/comment; hien summary san pham tu `media_count` cho upload.
- Review `HIDDEN` -> 409 khi PATCH; GET co the 404/409 tuy policy.
- Khong dung endpoint nay cho public product review list.

## 5. FE Integration Notes

- API client: `productReviewWriteApi.js` -> `fetchReviewForEdit(reviewId)`.
- Hook: `useReviewFormPage` (`mode = edit`).
- Mapper: `mapReviewForEditResponse`.
- Submit sua: `PATCH /commerce/api/v1/reviews/{reviewId}` — `UpdateProductReview-api-and-behavior.md`.

## 6. Business Rules

- Chi buyer so huu review duoc doc.
- Read-only.
- Khong tra media URL day du trong MVP neu policy giam payload — FE co the goi upload/list rieng sau.

## 7. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-REVIEW` | Review khong ton tai / khong thuoc buyer |

## 8. Related

- FR: `docs/feature_requirements/commerce/FR_ViewProductReviewForEdit.md`
- Update: `UpdateProductReview-api-and-behavior.md`
- Create context: `ViewReviewContext-api-and-behavior.md`