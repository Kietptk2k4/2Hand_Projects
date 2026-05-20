# Functional Requirement - Update Product Price

## 1. Feature Overview

Cho phep seller cap nhat gia product cua shop minh. Price update nen tao record `product_prices` moi thay vi overwrite gia cu de giu lich su va dam bao order snapshots khong bi anh huong.

## 2. Actors

- **Seller:** Cap nhat product price.
- **System:** Validate ownership and price window.

## 3. Scope

**In Scope:**

- Create new product price record.
- Optional close previous active price.
- Support sale price.

**Out of Scope:**

- Promotion/voucher.
- Bulk price update.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/products/{productId}/prices`

**Auth:** Required (JWT seller)

**Request body:**

- `price`
- `sale_price` optional
- `start_at`
- `end_at` optional

## 5. Business Rules

- Seller can update price only for own product.
- `price >= 0`.
- `sale_price <= price` when provided.
- Active price windows should not overlap.
- Cart view and checkout use current active price.
- Existing order item snapshots remain unchanged.

## 6. Database Impact

- Read `products`.
- Read/update previous `product_prices` if closing active price.
- Insert new `product_prices`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership check by seller/shop.

## 9. Failure Cases

- Product not found/not owned -> 404.
- Invalid price/sale price -> 400.
- Overlapping price window -> 409.
- Product removed -> 409.

## 10. Acceptance Criteria

- Seller creates valid price for own product.
- Invalid sale price is rejected.
- Active price used by cart/checkout updates.
- Existing orders remain unchanged.

