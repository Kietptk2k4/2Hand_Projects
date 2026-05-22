# Sync Cart Item Status – API & Behavior

## 1. Business Goal

Cap nhat `cart_items.status` theo trang thai product/shop/inventory hien tai. Giup buyer thay item het hang hoac invalid truoc checkout. **Checkout van revalidate synchronous** — sync khong thay the gate cuoi.

## 2. Khong co public HTTP API

FR dinh nghia **system job** va **lazy validation**, khong endpoint rieng.

### Trigger

| Trigger | Cach chay |
|---------|-----------|
| Lazy (view cart) | `POST /commerce/api/v1/cart` goi sync truoc khi tra items |
| Scheduled job | `SyncCartItemStatusScheduler` — tat mac dinh |
| Product/shop change | `syncByProductId` / `syncBySellerId` tu PauseProduct, ModerateShop, ... |

### Job config

| Env | Mac dinh |
|-----|----------|
| `COMMERCE_SYNC_CART_ITEM_STATUS_ENABLED` | `false` |
| `COMMERCE_SYNC_CART_ITEM_STATUS_CRON` | `0 */5 * * * *` |
| `COMMERCE_SYNC_CART_ITEM_STATUS_BATCH_SIZE` | `100` |

## 3. Status rules

| Dieu kien | Status moi |
|-----------|------------|
| Product/shop/category khong hop le, product missing | `INVALID_PRODUCT` |
| `stock_quantity < cart_items.quantity` hoac product `OUT_OF_STOCK` | `OUT_OF_STOCK` |
| Product ACTIVE, shop ACTIVE, stock du | `ACTIVE` |

**Khong tu dong:**

- `REMOVED` → khong doi
- `INVALID_PRODUCT` → khong restore (batch job); **co** re-evaluate khi lazy view cart / `syncBySellerId` sau shop restore

**Khong thay doi:** `product_inventories` (khong reserve/release stock).

## 4. FE Behavior

- Sau `POST /cart`, item `status` trong response da duoc sync.
- Hien badge theo `status`: `ACTIVE`, `OUT_OF_STOCK`, `INVALID_PRODUCT`.
- Khong tin status cu — checkout API van validate lai.

## 5. Related

- FR: `docs/feature_requirements/commerce/FR_SyncCartItemStatus.md`
- Flow: `docs/business_flow/commerce_business_flow/cart-lifecycle-flow.md` (§13)
- View cart: `FR_ViewCart.md` (khi co)
