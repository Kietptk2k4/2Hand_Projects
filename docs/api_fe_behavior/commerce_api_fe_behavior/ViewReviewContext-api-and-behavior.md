# View Review Context - API & Behavior

## 1. Business Goal

Buyer lay **context de viet review** cho mot `order_item` da mua: snapshot san pham/shop, trang thai item, va co review hay chua. Dung khi vao form tao review ma khong co day du state tu man order detail.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/reviews/context`
- **Auth:** Bearer JWT (buyer)

### Query params

| Param | Type | Required | Mo ta |
|-------|------|----------|-------|
| `order_item_id` | UUID | yes | Dong hang buyer muon danh gia |

## 3. Response - Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay thong tin danh gia thanh cong.",
  "data": {
    "order_item_id": "880e8400-e29b-41d4-a716-446655440003",
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "product_id": "660e8400-e29b-41d4-a716-446655440001",
    "status": "COMPLETED",
    "product_name_snapshot": "iPhone 15",
    "image_snapshot": "http://localhost:9000/2hands-commerce-product/p1.jpg",
    "shop_name_snapshot": "Tech Shop",
    "final_price": 1000000,
    "completed_at": "2026-05-20T10:00:00Z",
    "has_review": false,
    "review_id": null
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

| Field | Mo ta |
|-------|-------|
| `status` | Trang thai `order_items` (can `COMPLETED` de tao review) |
| `has_review` | `true` neu da co review cho item |
| `review_id` | ID review neu `has_review = true` |

## 4. FE Behavior

- Goi khi vao trang tao review voi query `?orderItemId=` ma **khong** co `location.state` tu order detail.
- Neu `has_review = true` -> chan form, huong user sang edit.
- Neu `status !== COMPLETED` -> hien loi, khong cho submit.
- Khong dung cho public list review.

## 5. FE Integration Notes

- API client: `productReviewWriteApi.js` -> `fetchReviewContext(orderItemId)`.
- Hook: `useReviewFormPage` (`mode = create`).
- Mapper: `productReviewWriteMapper.js` -> `mapReviewContextResponse`.

## 6. Business Rules

- Chi buyer so huu order (`orders.buyer_id = JWT user_id`) duoc xem context.
- Read-only; khong tao/sua review tren endpoint nay.
- Snapshot fields lay tu `order_items` tai thoi diem order — khong phu thuoc catalog hien tai.

## 7. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | Thieu `order_item_id` |
| 404 | `COMMERCE-404-ORDER-ITEM` | Item khong ton tai / khong thuoc buyer |

## 8. Related

- FR: `docs/feature_requirements/commerce/FR_ViewReviewContext.md`
- Create review: `CreateProductReview-api-and-behavior.md`
- Edit review context: `ViewProductReviewForEdit-api-and-behavior.md`