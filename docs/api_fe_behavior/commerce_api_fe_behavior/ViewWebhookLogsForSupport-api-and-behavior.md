# View Webhook Logs For Support - API & Behavior (Commerce)

## 1. Business Goal

API Commerce-side tra cuu **webhook logs** PayOS/GHN de debug callback, idempotency va trang thai xu ly. Read-only; payload raw khong tra ve client.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/admin/support/webhook-logs`
- **Auth:** Bearer JWT — permission `WEBHOOK_SUPPORT_READ`

### Query params

| Param | Mo ta |
|-------|--------|
| `provider` | `PAYOS` hoac `GHN` |
| `reference_id` | `payos_order_code` hoac `ghn_order_code` |
| `status` | `PROCESSED`, `PENDING`, `INVALID_SIGNATURE` (PayOS) |
| `from` / `to` | ISO-8601 instant |
| `page` | Mac dinh `1` |
| `size` | Mac dinh `20`, max `100` |

## 3. Response - Success

**HTTP 200 OK**

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
        "retry_count": 0,
        "idempotency_key": "PAYOS:PAYOS-123:PAYMENT_SUCCESS",
        "payload_summary": { "code": "00" },
        "received_at": "2026-05-20T10:00:00Z"
      }
    ]
  }
}
```

## 4. FE Behavior

- **Out of scope Commerce FE.**
- Consumer: Admin Service — `admin_api_fe_behavior/ViewWebhookLogsForSupport-api-and-behavior.md`.

## 5. Business Rules

- `ViewWebhookLogsForSupportUseCase` — read-only, paginated.
- `payload_summary`: truong da sanitize, khong raw JSON.
- Khong replay/edit webhook tu API nay.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 400 | `COMMERCE-400-VALIDATION` | Filter/pagination khong hop le |
| 401 | `COMMERCE-401` | Thieu JWT |
| 403 | `COMMERCE-403` | Thieu `WEBHOOK_SUPPORT_READ` |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewWebhookLogsForSupport.md`
- Admin proxy: `docs/api_fe_behavior/admin_api_fe_behavior/ViewWebhookLogsForSupport-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-commerce-support-read.md`