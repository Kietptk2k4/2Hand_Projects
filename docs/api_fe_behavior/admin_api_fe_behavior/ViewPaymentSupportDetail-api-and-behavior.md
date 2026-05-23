# View Payment Support Detail – API & Behavior

## 1. Business Goal

Cho phép admin/support xem **chi tiết thanh toán** phục vụ issue PayOS/webhook/reconciliation. Dữ liệu payment do Commerce Service sở hữu.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/support/payments/{paymentId}` | Bearer + `PAYMENT_SUPPORT_READ` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Payment support detail retrieved successfully",
  "data": {
    "payment_id": "uuid",
    "order_id": "uuid",
    "payer_id": "uuid",
    "payment_method": "PAYOS",
    "amount": 150000,
    "currency": "VND",
    "status": "PAID",
    "paid_at": "2026-05-20T08:00:00Z",
    "expired_at": null,
    "created_at": "2026-05-19T10:00:00Z",
    "updated_at": "2026-05-20T08:00:00Z",
    "provider_order_code": "PAYOS-123",
    "provider_transaction_id": "TX-999",
    "checkout_url_available": false,
    "checkout_url_expired_at": null,
    "order_status": "PROCESSING",
    "order_payment_status": "PAID",
    "reconciliation_status": "RECONCILED",
    "status_timeline": [
      {
        "old_status": "PENDING",
        "new_status": "PAID",
        "occurred_at": "2026-05-20T08:00:00Z"
      }
    ],
    "webhook_events": [
      {
        "provider": "PAYOS",
        "event_type": "PAYMENT_SUCCESS",
        "signature_valid": true,
        "processed": true,
        "received_at": "2026-05-20T08:00:01Z"
      }
    ]
  }
}
```

- Không trả `provider_response`, webhook `payload`, API key, signature secret.
- `checkout_url_available`: boolean thay vì URL checkout đầy đủ.
- `webhook_events`: metadata an toàn (không raw JSON).

## 3. Commerce integration

Khi `admin.integrations.commerce.enabled=true`:

- Admin forward Bearer token tới Commerce.
- Commerce: `GET /commerce/api/v1/admin/support/payments/{paymentId}` (actor cần `PAYMENT_SUPPORT_READ` trong JWT).

Khi integration **tắt**: `503`.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `PAYMENT_SUPPORT_READ` |
| 404 | ADMIN-404 | Payment không tồn tại |
| 503 | ADMIN-503 | Commerce integration tắt hoặc Commerce không khả dụng |

## 5. Business Rules

- Read-only; không mutate payment.
- Audit `PAYMENT_SUPPORT_VIEW` (non-critical).
- `reconciliation_status`: derived từ payment status + webhook logs (PayOS).

| Value | Ý nghĩa (PayOS) |
|-------|------------------|
| `NOT_APPLICABLE` | COD / non-PayOS |
| `RECONCILED` | PAID + webhook hợp lệ đã processed |
| `OUTSTANDING` | PAID nhưng chưa có webhook hợp lệ |
| `AWAITING_WEBHOOK` | PENDING, chưa webhook |
| `WEBHOOK_RECEIVED` | PENDING + đã có webhook processed |
| `TERMINAL_*` | FAILED/CANCELLED/EXPIRED |

## 6. FE Integration

1. Từ order support → `GET .../support/payments/{paymentId}`.
2. Hiển thị status, provider refs, timeline, webhook table, reconciliation badge.
3. Không hiển thị secrets hoặc raw provider payload.

## 7. Related

- FR: `docs/feature_requirements/admin/FR_ViewPaymentSupportDetail.md`
- Permission JWT: `PAYMENT_SUPPORT_READ`
- Audit action: `PAYMENT_SUPPORT_VIEW`
