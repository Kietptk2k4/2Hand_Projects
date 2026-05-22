# Reply To Review – API & Behavior

## 1. Business Goal

Seller phản hồi đánh giá của buyer trên shop/product của mình. MVP: **một reply** cho mỗi review.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/reviews/{reviewId}/reply`
- **Auth:** Bearer JWT (`user_id` = `reviews.seller_id`)

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `content` | string | yes | Nội dung phản hồi (không trống) |

```json
{
  "content": "Cam on ban da mua hang!"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Phan hoi danh gia thanh cong.",
  "data": {
    "reply_id": "880e8400-e29b-41d4-a716-446655440003",
    "review_id": "770e8400-e29b-41d4-a716-446655440002",
    "seller_id": "660e8400-e29b-41d4-a716-446655440001",
    "content": "Cam on ban da mua hang!",
    "created_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. Errors

| HTTP | Code | Khi nào |
|------|------|---------|
| 401 | `COMMERCE-401` | Thiếu JWT |
| 400 | `COMMERCE-400-VALIDATION` | `content` trống |
| 404 | `COMMERCE-404-REVIEW` | Review không tồn tại / không thuộc seller |
| 409 | `COMMERCE-409-REVIEW-VISIBLE` | Review đang `HIDDEN` |
| 409 | `COMMERCE-409-REVIEW-REPLY` | Đã có reply (unique `review_id`) |

## 5. Business Rules

- Chỉ reply review `VISIBLE` thuộc shop/seller của JWT.
- Không sửa rating/comment của buyer.
- Một reply/review (`review_replies.review_id` UNIQUE).
- Outbox: `COMMERCE_REVIEW_REPLIED` → `commerce.review.replied`.

## 6. Database

- Read: `reviews`
- Write: `review_replies`, `outbox_events`

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ReplyToReview.md`
- Flow: `docs/business_flow/commerce_business_flow/review-lifecycle-flow.md` §11
