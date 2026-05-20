# Functional Requirement - Publish Product

## 1. Feature Overview

Cho phep seller publish product sau khi product du dieu kien ban hang. Publish chuyen product sang `ACTIVE` neu co stock, hoac `OUT_OF_STOCK` neu stock = 0.

## 2. Actors

- **Seller:** Publish product.
- **System:** Validate readiness and update status.

## 3. Scope

**In Scope:**

- Validate publish readiness.
- Set status `ACTIVE` or `OUT_OF_STOCK`.
- Emit product published event.

**Out of Scope:**

- Product approval workflow.
- Inventory update.
- Price creation.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/seller/products/{productId}/publish`

**Auth:** Required (JWT seller)

## 5. Business Rules

- Seller can publish only own product.
- Shop must be `ACTIVE`.
- Product must not be `REMOVED`.
- Category must be active.
- Required fields must exist: title, description, category, condition, weight.
- Active price must exist.
- Inventory record must exist.
- If `stock_quantity > 0`, status becomes `ACTIVE`.
- If `stock_quantity = 0`, status becomes `OUT_OF_STOCK`.

## 6. Database Impact

- Read `products`, `seller_shops`, `product_categories`, `product_prices`, `product_inventories`, optional media.
- Update `products.status`.
- Insert outbox event.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Seller ownership check required.

## 9. Failure Cases

- Product not found/not owned -> 404.
- Shop not active -> 409.
- Missing price/inventory/required fields -> 409.
- Category inactive -> 409.

## 10. Acceptance Criteria

- Ready product can be published.
- Product without price/inventory cannot publish.
- Published product visibility follows status and stock.

