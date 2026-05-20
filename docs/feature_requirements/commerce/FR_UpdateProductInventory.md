# Functional Requirement - Update Product Inventory

## 1. Feature Overview

Cho phep seller cap nhat ton kho cua product thuoc shop minh. Inventory update tac dong truc tiep den kha nang publish, add cart va checkout.

## 2. Actors

- **Seller:** Cap nhat stock va low stock threshold.
- **System:** Validate ownership, update product stock status, trigger cart sync if needed.

## 3. Scope

**In Scope:**

- Update `stock_quantity`.
- Update `low_stock_threshold`.
- Create inventory record if product has none and policy allows.
- Update product `ACTIVE/OUT_OF_STOCK` status when needed.

**Out of Scope:**

- Reserve/release inventory for checkout/payment.
- Multi-warehouse inventory.

## 4. API Contract

**Endpoint:** `PATCH /commerce/api/v1/seller/products/{productId}/inventory`

**Auth:** Required (JWT seller)

**Request body:**

- `stock_quantity`
- `low_stock_threshold` optional

## 5. Business Rules

- Seller can update only own product inventory.
- `stock_quantity >= 0`.
- `low_stock_threshold >= 0`.
- Seller must not directly overwrite `reserved_quantity`.
- If stock becomes 0, product can become `OUT_OF_STOCK`.
- If stock becomes > 0 and product was `OUT_OF_STOCK`, product can return `ACTIVE` if shop/category/product valid.

## 6. Database Impact

- Read `products`.
- Insert/update `product_inventories`.
- Optional update `products.status`.
- Optional update related `cart_items` via sync/event.
- Insert outbox event if configured.

## 7. Transaction

- Write transaction required.

## 8. Security

- JWT required.
- Seller ownership check by product/shop.

## 9. Failure Cases

- Product not found/not owned -> 404.
- Negative stock/threshold -> 400.
- Product removed -> 409.

## 10. Acceptance Criteria

- Seller updates own product stock.
- Negative values are rejected.
- Reserved quantity is not directly modified.
- Product/cart availability reflects new stock.

