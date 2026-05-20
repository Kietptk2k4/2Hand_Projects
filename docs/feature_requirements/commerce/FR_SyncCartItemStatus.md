# Functional Requirement - Sync Cart Item Status

## 1. Feature Overview

System job hoac lazy validation cap nhat status cua cart items khi product, shop hoac inventory thay doi. Feature nay giup buyer thay item het hang/invalid truoc checkout, nhung checkout van phai revalidate synchronous.

## 2. Actors

- **System:** Background job/lazy validator.
- **Buyer:** Nhan cart status moi khi xem cart.

## 3. Scope

**In Scope:**

- Mark cart item `OUT_OF_STOCK`.
- Mark cart item `INVALID_PRODUCT`.
- Restore `OUT_OF_STOCK` to `ACTIVE` khi stock du va product/shop valid.

**Out of Scope:**

- Inventory reservation.
- Order creation.
- Payment.

## 4. Trigger

- Scheduled cart sync job.
- Product/shop/inventory change event.
- Lazy validation during view cart.

## 5. Business Rules

- Product invalid -> `INVALID_PRODUCT`.
- Shop inactive/suspended/closed -> `INVALID_PRODUCT`.
- `stock_quantity < cart_items.quantity` -> `OUT_OF_STOCK`.
- Stock sufficient + product/shop valid -> `ACTIVE` if previously `OUT_OF_STOCK`.
- `REMOVED` items should not be auto-restored.
- Sync does not reserve/release stock.

## 6. Database Impact

- Read `cart_items`.
- Read `products`, `seller_shops`, `product_inventories`.
- Update `cart_items.status`.

## 7. Transaction

- Write transaction per batch if persisting status changes.
- Small batches recommended.

## 8. Security

- System/internal job or buyer-owned cart lazy validation.

## 9. Failure Cases

- Product missing -> mark `INVALID_PRODUCT`.
- Job partial failure -> retry; checkout still protects.

## 10. Acceptance Criteria

- Cart items reflect current product/shop/stock state.
- Removed items are not restored.
- Inventory quantities do not change.
- Checkout remains final validation gate.

