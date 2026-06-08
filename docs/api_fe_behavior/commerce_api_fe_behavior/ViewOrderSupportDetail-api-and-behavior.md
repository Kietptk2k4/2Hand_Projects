# View Order Support Detail - API & Behavior (Commerce)

## 1. Business Goal

API **Commerce-side** cho admin/support tra cuu chi tiet don hang read-only. Admin Service forward Bearer token va aggregate response cho man support; **Commerce FE buyer/seller khong goi** endpoint nay.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/admin/support/orders/{orderId}`
- **Auth:** Bearer JWT — permission `ORDER_SUPPORT_READ`

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `orderId` | UUID | ID order |

## 3. Response - Success

**HTTP 200 OK** — cung shape `ViewOrderDetailResponse`:

```json
{
  "code": 200,
  "success": true,
  "message": "Order support detail retrieved successfully",
  "data": {
    "order_id": "550e8400-e29b-41d4-a716-446655440000",
    "buyer_id": "660e8400-e29b-41d4-a716-446655440001",
    "order_status": "PROCESSING",
    "order_payment_status": "PAID",
    "payment_method": "PAYOS",
    "total_amount": 1050000,
    "final_amount": 1050000,
    "created_at": "2026-05-21T10:00:00Z",
    "updated_at": "2026-05-21T14:00:00Z",
    "completed_at": null,
    "payment": { "payment_id": "...", "status": "PAID" },
    "items": [],
    "shipments": [],
    "order_timeline": []
  }
}
```

- Khong yeu cau buyer ownership — thay bang admin permission.
- Admin Service co the mask PII shipping truoc khi hien FE admin.

## 4. FE Behavior

- **Out of scope Commerce FE** (buyer/seller app).
- Consumer: Admin Service -> `GET /admin/api/v1/support/orders/{orderId}` — xem `admin_api_fe_behavior/ViewOrderSupportDetail-api-and-behavior.md`.

## 5. Business Rules

- Read-only; `ViewOrderSupportDetailUseCase`.
- Khong mutate order/payment/shipment.
- Permission bat buoc trong JWT Commerce admin claims.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 403 | `COMMERCE-403` | Thieu `ORDER_SUPPORT_READ` |
| 404 | `COMMERCE-404-ORDER` | Order khong ton tai |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewOrderSupportDetail.md`
- Admin proxy: `docs/api_fe_behavior/admin_api_fe_behavior/ViewOrderSupportDetail-api-and-behavior.md`
- Buyer order detail: `ViewOrderDetail-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-commerce-support-read.md`