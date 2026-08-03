# View Newest Products – API & Behavior

## 1. Business Goal

Tra ve danh sach san pham **hang moi ve** (ACTIVE, con hang) de hien thi section Home va trang `/commerce/newest`.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/products/newest`
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
    "items": [],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total_items": 0,
      "total_pages": 0,
      "has_next": false
    }
  }
}
```

Item shape giong `ProductCardResponse` (giong `GET /products`).

## 4. Business Rules

- Chi san pham `ACTIVE`, `stock_quantity > 0`, shop/category active.
- Default sort `NEWEST` (`created_at DESC`).
- Khong loc theo khuyen mai (khac Flash Sale).

## 5. FE Integration

- Home section preview (limit nho) + trang `/commerce/newest` co sort + load more.
