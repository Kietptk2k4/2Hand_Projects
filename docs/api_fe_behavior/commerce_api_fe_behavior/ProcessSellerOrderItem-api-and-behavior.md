# Process Seller Order Item – API & Behavior

## 1. Business Goal

Seller xac nhan chuan bi hang: chuyen order items thuoc shop tu `PENDING` sang `PROCESSING` khi order san sang fulfillment.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/seller/order-items/process`
- **Auth:** Bearer JWT (`user_id` = `order_items.seller_id`)

### Request body

| Field | Type | Required |
|-------|------|----------|
| `order_item_ids` | UUID[] | yes |

```json
{
  "order_item_ids": ["660e8400-e29b-41d4-a716-446655440001"]
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Danh dau chuan bi hang thanh cong.",
  "data": {
    "items": [
      {
        "order_item_id": "660e8400-e29b-41d4-a716-446655440001",
        "order_id": "550e8400-e29b-41d4-a716-446655440000",
        "status": "PROCESSING",
        "product_name_snapshot": "Ao thun",
        "quantity": 1,
        "newly_processed": true
      }
    ],
    "newly_processed_count": 1,
    "already_processing_count": 0,
    "processed_at": "2026-05-21T10:00:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. Business rules

- Seller phai co shop (`seller_shops`).
- Tat ca `order_item_ids` phai thuoc seller (khong thuoc seller khac → 404).
- Parent `orders.status` = `PROCESSING`.
- PayOS: `orders.payment_status` = `PAID`.
- COD: cho phep khi order `PROCESSING` (payment co the `PENDING`).
- Item status: chi `PENDING` hoac idempotent `PROCESSING`.
- Khong xu ly `CANCELLED` / `COMPLETED` / `SHIPPED` / ...

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 400 | `COMMERCE-400-VALIDATION` | `order_item_ids` trong |
| 404 | `COMMERCE-404-ORDER-ITEM` | Item khong ton tai / khong thuoc seller |
| 409 | `COMMERCE-409-ORDER-PROCESSING` | Order khong o `PROCESSING` |
| 409 | `COMMERCE-409-PAYMENT-STATE` | PayOS chua `PAID` |
| 409 | `COMMERCE-409-ORDER-ITEM-PROCESS` | Item status khong cho phep |
| 409 | `COMMERCE-409-SELLER-SHOP` | Seller chua co shop |

## 6. Database & Events

- Read: `seller_shops`, `orders`, `order_items`
- Write: `order_items`
- Outbox: `COMMERCE_SELLER_ORDER_ITEM_PROCESSING` → `commerce.seller_order_item.processing` (theo order, khi co item moi chuyen)

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ProcessSellerOrderItem.md`
- Flow: `docs/business_flow/commerce_business_flow/seller-order-management-flow.md` §8
