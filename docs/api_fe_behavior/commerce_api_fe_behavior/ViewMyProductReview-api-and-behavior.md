# View My Product Review - API & Behavior

## 1. Business Goal

Buyer xem **review cua chinh minh** cho mot san pham (neu co) tren trang danh sach review san pham. Tra ve wrapper `has_review` de FE hien strip "Danh gia cua ban" hoac nut viet/sua review.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/me/products/{productId}/review`
- **Auth:** Bearer JWT (buyer)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `productId` | UUID | ID san pham |

## 3. Response - Success

**HTTP 200 OK** — chua co review:

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh gia cua ban thanh cong.",
  "data": {
    "has_review": false,
    "product_id": "660e8400-e29b-41d4-a716-446655440001",
    "review_id": null,
    "can_edit": false
  }
}
```

**HTTP 200 OK** — da co review:

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh gia cua ban thanh cong.",
  "data": {
    "has_review": true,
    "review_id": "990e8400-e29b-41d4-a716-446655440004",
    "product_id": "660e8400-e29b-41d4-a716-446655440001",
    "order_item_id": "880e8400-e29b-41d4-a716-446655440003",
    "rating": 5,
    "comment": "San pham dung mo ta.",
    "status": "VISIBLE",
    "created_at": "2026-05-20T10:00:00Z",
    "updated_at": "2026-05-20T10:00:00Z",
    "can_edit": true
  }
}
```

| Field | Mo ta |
|-------|-------|
| `can_edit` | `true` khi review `VISIBLE` va thuoc buyer — FE hien nut sua |
| `status` | `VISIBLE` hoac `HIDDEN` (admin moderation) |

## 4. FE Behavior

- Chi goi khi user da dang nhap.
- Khong co review -> strip "Ban chua danh gia" / CTA viet review (neu da mua va completed).
- Co review -> hien rating/comment tom tat + link edit khi `can_edit`.
- Khong thay the public list `GET /products/{productId}/reviews`.

## 5. FE Integration Notes

- API client: `productReviewWriteApi.js` -> `fetchMyProductReview(productId)`.
- Hook: `useMyProductReview`.
- UI: `MyProductReviewStrip` tren `CommerceProductReviewsPage`.
- Mapper: `mapMyProductReviewResponse`.

## 6. Business Rules

- Chi tra review cua buyer hien tai cho product do.
- Mot buyer toi da mot review visible per product (qua unique `order_item_id`).
- Review `HIDDEN` van co the tra ve cho owner nhung `can_edit = false`.

## 7. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-PRODUCT` | San pham khong ton tai / khong visible |

## 8. Related

- FR: `docs/feature_requirements/commerce/FR_ViewMyProductReview.md`
- Public list: `ViewProductReviews-api-and-behavior.md`
- Edit: `ViewProductReviewForEdit-api-and-behavior.md`