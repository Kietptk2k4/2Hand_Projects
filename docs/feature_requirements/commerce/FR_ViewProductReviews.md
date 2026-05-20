# Functional Requirement - View Product Reviews

## 1. Feature Overview

Cho phep buyer xem danh sach review visible cua mot san pham, bao gom rating, comment, media va seller reply. Review list giup buyer danh gia social proof truoc khi mua.

## 2. Actors

- **Buyer/Guest:** Xem review product.
- **System:** Query visible reviews va rating summary.

## 3. Scope

**In Scope:**

- View paginated product reviews.
- Include review media.
- Include seller reply.
- Return rating summary.

**Out of Scope:**

- Create/update review.
- Seller reply.
- Review moderation.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/products/{productId}/reviews`

**Auth:** Optional hoac Required theo API policy.

**Query params:**

- `page` / `cursor`
- `limit`
- optional `rating`
- optional `sort`

## 5. Business Rules

- Product phai buyer-visible hoac review public policy cho phep.
- Chi tra `reviews.status = VISIBLE`.
- Hidden reviews khong duoc hien thi.
- Review media va seller reply duoc include neu co.
- Default sort newest first.

## 6. Database Impact

- Read `products`.
- Read `reviews`.
- Read `review_media`.
- Read `review_replies`.
- Read order item/product linkage if needed for product filter.

## 7. Transaction

- Read-only.

## 8. Security

- Khong expose hidden review.
- Khong expose buyer private fields beyond public display policy.

## 9. Failure Cases

- Product not found/not visible -> 404.
- Invalid rating filter -> 400.

## 10. Acceptance Criteria

- API chi tra visible reviews.
- Review response co rating/comment/media/reply.
- Hidden reviews khong xuat hien.
- Pagination hoat dong dung.

