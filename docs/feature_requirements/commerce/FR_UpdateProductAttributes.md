# Functional Requirement - Update Product Attributes

## 1. Feature Overview

Cho phep seller cap nhat thuoc tinh product, vi du mau sac, kich thuoc, chat lieu hoac cac thong tin mo ta cau truc. Attributes duoc snapshot vao order item khi checkout.

## 2. Actors

- **Seller:** Cap nhat attributes cua product.
- **System:** Validate ownership and upsert attributes.

## 3. Scope

**In Scope:**

- Add/update/delete product attributes according API policy.
- Enforce unique attribute name per product.

**Out of Scope:**

- Variant/SKU matrix.
- Inventory per attribute combination.

## 4. API Contract

**Endpoint:** `PUT /commerce/api/v1/seller/products/{productId}/attributes`

**Auth:** Required (JWT seller)

**Request body:**

- `attributes[]`
  - `attribute_name`
  - `attribute_value`

## 5. Business Rules

- Seller can update only own product attributes.
- Attribute name/value cannot be blank.
- Attribute name unique per product.
- Updating attributes does not change existing order item snapshots.
- Product `REMOVED` cannot be updated by seller.

## 6. Database Impact

- Read `products`.
- Upsert/delete `product_attributes`.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required for bulk replace/upsert.

## 8. Security

- JWT required.
- Ownership check by product/shop.

## 9. Failure Cases

- Product not found/not owned -> 404.
- Duplicate attribute name in request -> 400.
- Blank name/value -> 400.
- Product removed -> 409.

## 10. Acceptance Criteria

- Seller updates attributes for own product.
- Duplicate/blank attributes are rejected.
- Buyer detail reflects updated attributes.
- Existing order snapshots remain unchanged.

