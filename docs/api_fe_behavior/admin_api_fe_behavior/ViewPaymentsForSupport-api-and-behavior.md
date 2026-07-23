# View Payments For Support – API & Behavior

## 1. Business Goal

Cho phép admin/support **lọc và duyệt danh sách thanh toán** phục vụ đối soát PayOS/VNPay, tra cứu ticket và liên kết sang order detail.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/support/payments` | Bearer + `PAYMENT_SUPPORT_READ` |

### Query parameters

| Param | Type | Mô tả |
|-------|------|--------|
| `q` | string (UUID fragment) | Tìm theo `payment_id` |
| `status` | string | `PENDING`, `PAID`, `FAILED`, `CANCELLED`, `EXPIRED` |
| `reconciliation_status` | string | `NOT_APPLICABLE`, `RECONCILED`, `OUTSTANDING`, `AWAITING_WEBHOOK`, `WEBHOOK_RECEIVED`, `TERMINAL_*` |
| `payment_method` | string | `COD`, `PAYOS`, `VNPAY` |
| `order_id` | UUID | Lọc theo đơn hàng |
| `from` | ISO-8601 | `created_at >= from` |
| `to` | ISO-8601 | `created_at <= to` |
| `page` | int | Trang (1-based), default `1` |
| `size` | int | Kích thước trang, default `20` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Payments retrieved successfully",
  "data": {
    "page": 1,
    "size": 20,
    "total_elements": 42,
    "total_pages": 3,
    "payments": [
      {
        "payment_id": "uuid",
        "order_id": "uuid",
        "payment_method": "VNPAY",
        "amount": 1071500,
        "currency": "VND",
        "status": "PAID",
        "paid_at": "2026-07-23T12:16:48Z",
        "created_at": "2026-07-23T12:16:48Z"
      }
    ]
  }
}
```

## 3. Commerce integration

Khi `admin.integrations.commerce.enabled=true`:

- Admin forward Bearer token tới Commerce.
- Commerce: `GET /commerce/api/v1/admin/support/payments`.

Khi integration **tắt**: `503`.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `PAYMENT_SUPPORT_READ` |
| 400 | ADMIN-400 | Query không hợp lệ |
| 503 | ADMIN-503 | Commerce integration tắt hoặc không khả dụng |

## 5. Business Rules

- Read-only; không mutate payment.
- `reconciliation_status` filter chỉ áp dụng logic PayOS (non-PayOS → `NOT_APPLICABLE`).
- Ghi audit `PAYMENT_SUPPORT_VIEW` (non-critical).

## 6. FE Integration

1. Màn **Chi tiết thanh toán** (`tab=payment-detail`) → `GET .../support/payments` với filter URL `pay_*`.
2. Tra cứu nhanh UUID → set `paymentId` + mở drawer.
3. `paymentView=summary|timeline|webhooks` cho drawer tabs.
4. MSW: `adminOrderSupportHandlers.js` + `adminOrderSupportData.js`.

## 7. Related

- Detail: [ViewPaymentSupportDetail-api-and-behavior.md](./ViewPaymentSupportDetail-api-and-behavior.md)
