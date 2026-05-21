# UC - Review Management

## 1. Overview

Use case nay mo ta nghiep vu review san pham/shop trong Commerce Service. Buyer chi duoc review sau khi order item da `COMPLETED`. Seller co the reply review cua shop minh. Admin co the hide review vi pham. Review anh huong rating summary cua shop/product.

## 2. Actors

- **Buyer:** Tao, cap nhat va xem review.
- **Seller:** Xem review shop va reply review.
- **Admin:** Hide/restore review.
- **System:** Cap nhat rating summary.

## 3. Related Data

- `reviews`
- `review_media`
- `review_replies`
- `orders`
- `order_items`
- `seller_shops`
- `outbox_events`

## 4. Business Rules

- Cannot review unless `order_items.status = COMPLETED`.
- `buyer_id` phai match `orders.buyer_id`.
- One review per order item: `UNIQUE(order_item_id)`.
- Rating must be 1..5.
- Seller chi reply review cua shop minh.
- Public review list chi hien `VISIBLE`.
- Hidden review khong tinh vao rating summary neu policy rating chi tinh visible reviews.
- Review media: file tren MinIO bucket `2hands-commerce-review`; DB `review_media.url`. Xem `docs/engineering_rules/commerce-object-storage.md`.

## 5. Sub-Use Cases

### 5.1. Create Product Review

**Preconditions:** Buyer owns order; order item `COMPLETED`; no existing review.

**Main Flow:**

1. Buyer gui `order_item_id`, rating, comment va optional media.
2. System load order item va order.
3. System validate buyer ownership va completed status.
4. System insert review `VISIBLE`.
5. System attach media neu co.
6. System recalculate rating summary.
7. System ghi outbox event.

**Exception Flow:** Item not completed -> 409; duplicate review -> 409; rating invalid -> 400.

### 5.2. Update Review

**Preconditions:** Review exists and belongs to buyer.

**Main Flow:**

1. Buyer update rating/comment/media.
2. System validate ownership.
3. System update review.
4. System recalculate rating neu rating changed.
5. System ghi outbox event.

**Exception Flow:** Review hidden -> reject or keep hidden; not owner -> 403/404.

### 5.3. View Product Reviews

**Main Flow:**

1. Buyer request reviews by product.
2. System validate product visible.
3. System query `reviews.status = VISIBLE`.
4. System include media and seller replies.
5. System tra paginated reviews va rating summary.

### 5.4. Reply To Review

**Preconditions:** Seller owns shop of review; review visible; no existing reply.

**Main Flow:**

1. Seller submit reply content.
2. System validate seller ownership.
3. System insert `review_replies`.
4. System ghi outbox event.

**Exception Flow:** Duplicate reply -> 409; seller not owner -> 403/404.

### 5.5. Moderate Review

**Preconditions:** Admin has permission.

**Main Flow:**

1. Admin hide or restore review.
2. System update review status.
3. System recalculate rating summary.
4. System ghi moderation event.

**Exception Flow:** Missing permission -> 403.

## 6. Security

- Buyer review ownership derived from order, not client-provided buyer id.
- Seller reply ownership derived from shop/order item seller.
- Admin action requires permission.

## 7. Acceptance Criteria

- Buyer cannot review before order item completed.
- Buyer cannot review someone else's order item.
- One order item has at most one review.
- Seller cannot edit buyer review.
- Hidden reviews do not appear publicly.

