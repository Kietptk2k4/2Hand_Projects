# View Order Support Detail – API & Behavior

## 1. Business Goal

Cho phép admin/support xem **chi tiết đơn hàng** phục vụ tranh chấp, vận chuyển và thanh toán. Dữ liệu order do Commerce Service sở hữu; Admin Service chỉ aggregate qua internal API.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/support/orders/{orderId}` | Bearer + `ORDER_SUPPORT_READ` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Order support detail retrieved successfully",
  "data": {
    "order_id": "uuid",
    "buyer_id": "uuid",
    "order_status": "SHIPPED",
    "order_payment_status": "PAID",
    "payment_method": "VNPAY",
    "total_amount": 100000,
    "final_amount": 100000,
    "created_at": "2026-05-19T00:00:00Z",
    "updated_at": "2026-05-20T00:00:00Z",
    "completed_at": null,
    "payment": { "payment_id": "uuid", "status": "PAID" },
    "items": [],
    "shipments": [
      {
        "shipment_id": "uuid",
        "shipping_address": {
          "receiver_name": "Nguyen ***",
          "phone": "***4567",
          "province_code": "79",
          "address_detail": "***",
          "full_address": "***"
        }
      }
    ],
    "order_timeline": [],
    "contact_fields_masked": true,
    "cancellation_note": null,
    "active_refund_request": null
  }
}
```

- `cancellation_note`: lý do hủy đơn (nếu có), passthrough từ Commerce.
- `active_refund_request`: yêu cầu hoàn tiền đang mở `{ status, amount, ... }` (nếu có).
- `contact_fields_masked`: `true` khi PII shipping đã được mask (mặc định với `ORDER_SUPPORT_READ`).
- Không trả password, token, OTP.

## 3. Commerce integration

Khi `admin.integrations.commerce.enabled=true`:

- Admin forward Bearer token tới Commerce.
- Commerce: `GET /commerce/api/v1/admin/support/orders/{orderId}` (actor cần `ORDER_SUPPORT_READ` trong JWT).

Khi integration **tắt**: `503` — order support detail bắt buộc Commerce.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `ORDER_SUPPORT_READ` (Admin hoặc Commerce từ chối) |
| 404 | ADMIN-404 | Order không tồn tại |
| 503 | ADMIN-503 | Commerce integration tắt hoặc Commerce không khả dụng |

## 5. Business Rules

- Read-only; không mutate order.
- Ghi `admin_action_logs` với action `ORDER_SUPPORT_VIEW` (non-critical, không lưu request payload đầy đủ).
- Mask receiver name, phone, address detail/full address trên shipping snapshot.

## 6. FE Integration

1. Màn support order → `GET .../support/orders/{orderId}` (Bearer admin).
2. Hiển thị status, items, payment, shipments; chú ý `contact_fields_masked`.
3. Nếu `503`, hiển thị thông báo Commerce không khả dụng.

## 7. Related

- FR: `docs/feature_requirements/admin/FR_ViewOrderSupportDetail.md`
- Permission JWT: `ORDER_SUPPORT_READ`
- Audit action type: `ORDER_SUPPORT_VIEW`
