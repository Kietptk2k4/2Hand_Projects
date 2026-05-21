# Archive Product – API & Behavior

## 1. Business Goal

Cho phep seller archive (soft delete) san pham cua shop minh. Product `ARCHIVED` khong hien buyer discovery, khong add cart/checkout; order snapshots da tao van giu nguyen.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/products/{productId}/archive`
- **Auth:** Bearer JWT (seller — `user_id` trong token phai trung `products.seller_id`)
- **Request Body:** Khong co

### Path Parameters

| Field       | Type | Required | Mo ta        |
|-------------|------|----------|--------------|
| `productId` | UUID | yes      | ID san pham  |

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Archive san pham thanh cong.",
  "data": {
    "product_id": "550e8400-e29b-41d4-a716-446655440000",
    "shop_id": "660e8400-e29b-41d4-a716-446655440001",
    "status": "ARCHIVED",
    "archived_at": "2026-05-21T10:30:00.123Z",
    "cart_items_invalidated": 2,
    "already_archived": false
  },
  "errors": null,
  "timestamp": "2026-05-21T10:30:00.123Z"
}
```

**Idempotent (da ARCHIVED):** `already_archived: true`, `cart_items_invalidated: 0`, message: `San pham da duoc archive truoc do.`

## 4. Response – Error

| HTTP | Code string                      | Mo ta                                      |
|------|----------------------------------|--------------------------------------------|
| 401  | `COMMERCE-401`                   | Khong co hoac JWT khong hop le             |
| 404  | `COMMERCE-404-PRODUCT`           | Product khong ton tai hoac khong thuoc seller |
| 409  | `COMMERCE-409-PRODUCT-REMOVED`   | Product da bi admin remove                 |
| 409  | `COMMERCE-409-PRODUCT-STATUS`    | Status khong cho phep archive              |
| 500  | `COMMERCE-500`                   | Loi server                                 |

## 5. Business Rules

- Seller chi archive product cua minh (`seller_id` = JWT `user_id`).
- Cho phep archive tu: `DRAFT`, `ACTIVE`, `PAUSED`, `OUT_OF_STOCK`.
- `REMOVED` → reject 409 (chi admin remove).
- Da `ARCHIVED` → idempotent 200, khong ghi outbox/cart lan nua.
- Cap nhat `products.status = ARCHIVED`, `updated_at`.
- Cart items `ACTIVE` / `OUT_OF_STOCK` cua product → `INVALID_PRODUCT`.
- Ghi outbox `COMMERCE_PRODUCT_ARCHIVED` (topic `commerce.product.archived`) trong cung transaction.
- Khong xoa vat ly product; khong doi order snapshots.

## 6. Edge Cases

- **Archive DRAFT:** hop le, khong can tung publish truoc.
- **Go archive 2 lan:** lan 2 tra `already_archived: true`.
- **Cart item REMOVED:** khong doi status.
- **Concurrent archive:** transaction + status check.

## 7. Data Dependencies

| Table          | Action                          |
|----------------|---------------------------------|
| `products`     | read/update status              |
| `cart_items`   | bulk update → `INVALID_PRODUCT` |
| `outbox_events`| insert event                    |

## 8. FE Integration Notes

- Seller dashboard: goi sau khi user xac nhan archive; refresh list theo `status`.
- Buyer app: product `ARCHIVED` khong xuat hien discovery (server filter).
- Local: `POST http://localhost:3003/commerce/api/v1/seller/products/{productId}/archive`
