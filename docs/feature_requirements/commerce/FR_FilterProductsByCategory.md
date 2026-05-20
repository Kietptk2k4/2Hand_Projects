# Functional Requirement - Filter Products By Category

## 1. Feature Overview

Cho phep buyer loc danh sach san pham theo category. Neu category co cay con, MVP nen ho tro loc ca subtree dua tren `path` hoac recursive category ids.

## 2. Actors

- **Buyer/Guest:** Loc san pham theo category.
- **System:** Resolve category va query product visible.

## 3. Scope

**In Scope:**

- Filter product by category id/slug.
- Include child categories neu category tree duoc support.
- Return product cards with pagination.

**Out of Scope:**

- Category management.
- Advanced faceted filter.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/categories/{categoryId}/products`

**Auth:** Optional hoac Required theo API policy.

**Query params:**

- `page` / `cursor`
- `limit`
- optional `sort`
- optional `include_children=true`

## 5. Business Rules

- Category phai ton tai.
- Category phai `is_active = true`.
- Chi tra product buyer-visible.
- Parent category filter nen include descendants neu API param/policy cho phep.
- Product out of stock co the tra ve nhung unavailable.

## 6. Database Impact

- Read `product_categories`.
- Read `products`.
- Read product enrichment tables: media, price, inventory, shop, rating.

## 7. Transaction

- Read-only.

## 8. Security

- Public-safe fields only.
- Inactive category khong expose product hidden.

## 9. Failure Cases

- Category not found -> 404.
- Category inactive -> 404 hoac empty result theo API policy.
- Invalid pagination -> 400.

## 10. Acceptance Criteria

- Active category tra product visible.
- Inactive category khong tra product.
- Child category products duoc include khi configured.
- Ket qua co pagination va product card summary.

