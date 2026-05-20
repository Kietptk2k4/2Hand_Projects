# Functional Requirement - Create Product

## 1. Feature Overview

Cho phep seller tao product draft trong shop cua minh. Product moi mac dinh `DRAFT` va chua buyer-visible cho den khi publish.

## 2. Actors

- **Seller:** Tao product.
- **System:** Validate shop ownership and persist product draft.

## 3. Scope

**In Scope:**

- Create product with core fields.
- Initial status `DRAFT`.
- Optional create initial inventory/attributes/media according API policy.

**Out of Scope:**

- Publish product.
- Price update if separated by dedicated feature.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/products`

**Auth:** Required (JWT seller)

**Request body:**

- `product_type`
- `category_id`
- `brand_id` optional
- `condition`
- `title`
- `description`
- `weight_gram`

## 5. Business Rules

- Seller must have own shop.
- Shop must not be `SUSPENDED/CLOSED`.
- Product starts as `DRAFT`.
- `seller_id` and `shop_id` derived from authenticated seller/shop.
- `weight_gram > 0`.

## 6. Database Impact

- Read `seller_shops`.
- Read `product_categories`.
- Insert `products`.
- Optional insert related inventory/media/attributes.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Seller ownership derived from JWT.

## 9. Failure Cases

- Seller has no shop -> 409.
- Shop suspended/closed -> 409.
- Category invalid -> 400/409.
- Invalid payload -> 400.

## 10. Acceptance Criteria

- Seller creates draft product for own shop.
- Product is not buyer-visible until publish.
- Invalid shop/category/payload is rejected.

