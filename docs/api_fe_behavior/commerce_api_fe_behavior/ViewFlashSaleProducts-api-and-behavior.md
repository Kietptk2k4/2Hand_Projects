# View Flash Sale Products – API & Behavior

## 1. Business Goal

Tra ve san pham dang khuyen mai thuc (co `sale_price` < `price`) va **dang active trong slot Flash Sale 3 gio** hien tai (timezone `Asia/Ho_Chi_Minh`).

Khuyen mai duoc tinh la thuoc slot neu **giao nhau** voi khoang `[slot_start, slot_end]` — khong bat buoc nam tron trong slot. Khuyen mai vo han (`end_at = null`) van duoc hien thi; countdown tren FE lay `min(promotion_end_at, slot_end)`.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/products/flash-sale`
- **Auth:** Public (khong bat buoc JWT)

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1..50 |
| `sort` | string | `NEWEST` | `NEWEST`, `PRICE_ASC`, `PRICE_DESC` |

## 3. Response – Success

```json
{
  "code": 200,
  "success": true,
  "data": {
    "items": [
      {
        "product_id": "...",
        "price": 800000,
        "sale_price": 680000,
        "effective_price": 680000,
        "promotion_start_at": "2026-08-02T08:00:00Z",
        "promotion_end_at": "2026-08-02T10:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 1,
      "total_pages": 1,
      "has_next": false
    },
    "slot_start": "2026-08-02T08:00:00Z",
    "slot_end": "2026-08-02T11:00:00Z"
  }
}
```

## 4. Business Rules

- Chi `status = ACTIVE`, `stock_quantity > 0`, shop active, khong vacation.
- `sale_price` bat buoc va nho hon `price`.
- Khuyen mai dang active tai `now` (qua lateral join `product_prices`).
- **Giao slot:** `start_at < slot_end` va (`end_at IS NULL` hoac `end_at > slot_start`). Khuyen mai vo han van duoc bao gom.
- Sap xep: `NEWEST` = `created_at DESC` (tie-break `end_at`); `PRICE_*` theo `effective_price` + tie-break `end_at`.

## 5. FE Integration

- Home preview goi API rieng (limit nho), trang `/commerce/flash-sale` dung pagination + sort.
- Hien thi gia khuyen mai + gia niem yet gach ngang + tiet kiem = `price - sale_price`.
