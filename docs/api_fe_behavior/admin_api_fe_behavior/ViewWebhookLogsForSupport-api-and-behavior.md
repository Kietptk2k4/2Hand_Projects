# View Webhook Logs For Support – API & Behavior

## 1. Business Goal

Cho phép admin/support tra cứu **webhook logs** PayOS/GHN để debug callback, idempotency và trạng thái xử lý.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/support/webhook-logs` | Bearer + `WEBHOOK_SUPPORT_READ` |
| GET | `/admin/api/v1/support/webhook-logs/stats` | Bearer + `WEBHOOK_SUPPORT_READ` |
| GET | `/admin/api/v1/support/webhook-logs/{log_id}?provider=PAYOS\|GHN` | Bearer + `WEBHOOK_SUPPORT_READ` |
| GET | `/admin/api/v1/support/webhook-logs/export?format=csv` | Bearer + `WEBHOOK_SUPPORT_READ` |

**Query params (list/stats/export):**

| Param | Mô tả |
|-------|--------|
| `provider` | `PAYOS` hoặc `GHN` |
| `reference_id` | Khớp chính xác `payos_order_code` / `ghn_order_code` |
| `q` | Tìm một phần mã tham chiếu (ILIKE) |
| `event_type` | Khớp chính xác loại sự kiện PayOS / status GHN |
| `status` | `PROCESSED`, `PENDING`, `INVALID_SIGNATURE` (PayOS only) |
| `from` / `to` | ISO-8601 instant |
| `page` | Mặc định `1` (list only) |
| `size` | Mặc định `20`, max `100` (list only) |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Webhook logs retrieved successfully",
  "data": {
    "page": 1,
    "size": 20,
    "total_elements": 1,
    "total_pages": 1,
    "logs": [
      {
        "log_id": "uuid",
        "provider": "PAYOS",
        "reference_id": "PAYOS-123",
        "event_type": "PAYMENT_SUCCESS",
        "processing_status": "PROCESSED",
        "signature_valid": true,
        "idempotency_key": "PAYOS:PAYOS-123:PAYMENT_SUCCESS",
        "payload_summary": { "code": "00", "order_code": "PAYOS-123" },
        "received_at": "2026-05-20T10:00:00Z",
        "payment_id": "uuid|null",
        "shipment_id": "uuid|null",
        "order_id": "uuid|null"
      }
    ]
  }
}
```

- Không trả raw `payload`, signature, secret.
- `payload_summary`: trường an toàn đã sanitize; `parse_error: true` khi không parse được payload.
- `payment_id` / `shipment_id` / `order_id`: liên kết cross-navigation (nullable).
- Export CSV: tối đa 5000 dòng, cùng bộ filter với list.

## 3. Commerce integration

`GET /commerce/api/v1/admin/support/webhook-logs` (cùng query params, Bearer + `WEBHOOK_SUPPORT_READ`).

Integration tắt → `503`.

## 4. Errors

| HTTP | Mô tả |
|------|--------|
| 400 | Filter/pagination không hợp lệ |
| 403 | Thiếu permission |
| 503 | Commerce không khả dụng |

## 5. Business Rules

- Read-only; không replay/edit webhook.
- `processing_status`:
  - PayOS: `INVALID_SIGNATURE` / `PROCESSED` / `PENDING`
  - GHN: `PROCESSED` / `PENDING`
- Audit: `WEBHOOK_SUPPORT_VIEW` (non-critical).

## 6. Related

- FR: `docs/feature_requirements/admin/FR_ViewWebhookLogsForSupport.md`
- Permission: `WEBHOOK_SUPPORT_READ`
- Audit: `WEBHOOK_SUPPORT_VIEW`
