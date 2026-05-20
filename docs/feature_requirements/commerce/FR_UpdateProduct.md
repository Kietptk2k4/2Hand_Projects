# Functional Requirement - Update Product

## 1. Feature Overview

Cho phep seller cap nhat thong tin core cua product thuoc shop minh.

## 2. Actors

- **Seller:** Cap nhat product.
- **System:** Validate ownership, editable status and payload.

## 3. Scope

**In Scope:**

- Update title, description, category, condition, weight, brand/product type.

**Out of Scope:**

- Price update.
- Inventory update.
- Media update.
- Publish/pause/archive status action.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/seller/products/{productId}`

**Auth:** Required (JWT seller)

## 5. Business Rules

- Seller can update only own product.
- Product `REMOVED` cannot be updated by seller.
- Product `ARCHIVED` update is blocked in MVP unless restore policy exists.
- Category must be active if changed.
- `weight_gram > 0` if provided.
- Updating product does not alter existing order item snapshots.

## 6. Database Impact

- Read/update `products`.
- Read `seller_shops`, `product_categories`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Ownership check by seller/shop.

## 9. Failure Cases

- Product not found/not owned -> 404.
- Product removed/archived -> 409.
- Invalid category/payload -> 400/409.

## 10. Acceptance Criteria

- Seller updates own editable product.
- Other seller product cannot be updated.
- Removed product cannot be modified by seller.
- Order snapshots remain unchanged.

