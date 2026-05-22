# Moderate Review – API & Behavior

## 1. Business Goal

Admin an (hide) hoac khoi phuc (restore) review vi pham. Review `HIDDEN` khong hien trong public list; `seller_shops.rating_avg` / `rating_count` chi tinh tu review `VISIBLE`.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/admin/reviews/{reviewId}/moderate`
- **Auth:** Bearer JWT — permission `COMMERCE_REVIEW_HIDE` (hoac role `ADMIN` trong MVP)

### Request body

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `action` | string | yes | `HIDE` hoac `RESTORE` |
| `reason` | string | yes | Ly do moderation (audit) |

```json
{
  "action": "HIDE",
  "reason": "Noi dung vi pham chinh sach"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "An review thanh cong.",
  "data": {
    "review_id": "770e8400-e29b-41d4-a716-446655440002",
    "order_item_id": "660e8400-e29b-41d4-a716-446655440001",
    "seller_id": "880e8400-e29b-41d4-a716-446655440003",
    "buyer_id": "990e8400-e29b-41d4-a716-446655440004",
    "rating": 5,
    "status": "HIDDEN",
    "previous_status": "VISIBLE",
    "already_moderated": false,
    "seller_rating_avg": 4.50,
    "seller_rating_count": 12,
    "moderated_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

`already_moderated: true` khi review da o trang thai dich (idempotent, khong ghi outbox lan nua).

## 4. FE Behavior

- Chi admin panel goi endpoint nay.
- Sau `HIDE`, refresh product/shop review list — review bien mat khoi public.
- `RESTORE` dua review ve `VISIBLE` va cap nhat lai rating shop.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 403 | `COMMERCE-403` | Thieu `COMMERCE_REVIEW_HIDE` / ADMIN |
| 400 | `COMMERCE-400-VALIDATION` | `reason` trong |
| 400 | `COMMERCE-400-REVIEW-MODERATION` | `action` khong hop le |
| 404 | `COMMERCE-404-REVIEW` | Review khong ton tai |

## 6. Database & Events

- Write: `reviews`, `seller_shops`, `outbox_events`
- Outbox:
  - `COMMERCE_REVIEW_HIDDEN` → `commerce.review.hidden`
  - `COMMERCE_REVIEW_RESTORED` → `commerce.review.restored`

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ModerateReview.md`
- Flow: `docs/business_flow/commerce_business_flow/review-lifecycle-flow.md` §12
