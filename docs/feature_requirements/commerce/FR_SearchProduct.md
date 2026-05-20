# Functional Requirement - Search Product

## 1. Feature Overview

Cho phep buyer tim kiem san pham theo keyword trong Commerce Service. Search MVP co the dua tren title/description/category/shop context va phai ton trong product visibility rule.

## 2. Actors

- **Buyer/Guest:** Tim product theo keyword.
- **System:** Query va enrich ket qua search.

## 3. Scope

**In Scope:**

- Search theo keyword.
- Filter ket qua theo product/shop/category visibility.
- Pagination ket qua.
- Return product card summary.

**Out of Scope:**

- Semantic search.
- Recommendation.
- Search history.
- Advanced ranking engine.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/products/search?q={keyword}`

**Auth:** Optional hoac Required theo API policy.

**Query params:**

- `q`
- `page` / `cursor`
- `limit`
- optional `sort`

## 5. Business Rules

- Keyword required.
- Keyword rong/qua ngan invalid.
- Search chi tra product `ACTIVE/OUT_OF_STOCK`.
- Shop phai `ACTIVE`.
- Category phai active.
- Product hidden statuses khong duoc tra ve.
- Search response su dung current active price.

## 6. Database Impact

- Read `products`.
- Read related shop/category/media/price/inventory/rating data.

## 7. Transaction

- Read-only.

## 8. Security

- Public-safe fields only.
- Khong expose internal ranking/debug data.

## 9. Failure Cases

- Empty keyword -> 400.
- Keyword too long -> 400.
- DB/search timeout -> 500.

## 10. Acceptance Criteria

- Valid keyword tra product page.
- Hidden product/shop khong xuat hien.
- Empty keyword bi reject.
- Ket qua co price va stock summary.

