# View Orders For Support – API & Behavior

## 1. Business Goal

Cho phép admin/support **lọc và duyệt danh sách đơn hàng** phục vụ tra cứu ticket, đối soát thanh toán và vận chuyển. Dữ liệu order do Commerce Service sở hữu; Admin Service aggregate qua internal API.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/support/orders` | Bearer + `ORDER_SUPPORT_READ` |

### Query parameters

| Param | Type | Mô tả |
|-------|------|--------|
| `q` | string (UUID) | Tìm theo `order_id` hoặc `buyer_id` (exact/prefix match tùy Commerce) |
| `status` | string | `CREATED`, `AWAITING_PAYMENT`, `PROCESSING`, `COMPLETED`, `CANCELLED` |
| `payment_status` | string | `PAID`, `PENDING`, `FAILED`, `EXPIRED`, … |
| `payment_method` | string | `COD`, `PAYOS`, `VNPAY` |
| `from` | ISO-8601 | Lọc `created_at >= from` |
| `to` | ISO-8601 | Lọc `created_at <= to` |
| `sort` | string | `created_at` (default), `updated_at` |
| `page` | int | Trang (1-based), default `1` |
| `size` | int | Kích thước trang, default `20` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "page": 1,
    "size": 20,
    "total_elements": 42,
    "total_pages": 3,
    "orders": [
      {
        "order_id": "uuid",
        "buyer_id": "uuid",
        "order_status": "PROCESSING",
        "payment_status": "PAID",
        "payment_method": "VNPAY",
        "final_amount": 350000,
        "created_at": "2026-05-19T10:00:00Z",
        "updated_at": "2026-05-20T08:00:00Z"
      }
    ]
  }
}
```

## 3. Commerce integration

Khi `admin.integrations.commerce.enabled=true`:

- Admin forward Bearer token tới Commerce.
- Commerce: `GET /commerce/api/v1/admin/support/orders` (actor cần `ORDER_SUPPORT_READ`).

Khi integration **tắt**: `503`.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `ORDER_SUPPORT_READ` |
| 400 | ADMIN-400 | Query không hợp lệ (status/method/payment_status) |
| 503 | ADMIN-503 | Commerce integration tắt hoặc không khả dụng |

## 5. Business Rules

- Read-only; không mutate order.
- Không trả PII buyer (chỉ `buyer_id`).
- Ghi audit `ORDER_SUPPORT_LIST` (non-critical) tùy policy admin-service.

## 6. FE Integration

1. Màn **Chi tiết đơn hàng** (`tab=order-detail`) → `GET .../support/orders` với filter URL `ord_*`.
2. Tra cứu nhanh UUID → set `orderId` URL + mở drawer detail.
3. Search `q` / `payment_status` map `ord_q`, `ord_payment_status` trong `adminUrlParams.js`.
4. Stats bar: parallel requests `size=1` + `status` preset (client-side aggregate).
5. MSW: `adminOrderSupportHandlers.js` + `adminOrderSupportData.js`.

## 7. Related

- Detail: [ViewOrderSupportDetail-api-and-behavior.md](./ViewOrderSupportDetail-api-and-behavior.md)
- Payment list: payment-detail tab
- Shipment list: shipment-detail tab
