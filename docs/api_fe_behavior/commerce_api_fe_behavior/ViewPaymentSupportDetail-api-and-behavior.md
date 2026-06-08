# View Payment Support Detail - API & Behavior (Commerce)

## 1. Business Goal

API Commerce-side tra cuu **chi tiet payment** phuc vu support: status, timeline, webhook summary, reconciliation. Read-only; khong expose provider secret/raw payload.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/admin/support/payments/{paymentId}`
- **Auth:** Bearer JWT — permission `PAYMENT_SUPPORT_READ`

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `paymentId` | UUID | ID payment |

## 3. Response - Success

**HTTP 200 OK**

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
    "provider_order_code": "PAYOS-123",
    "reconciliation_status": "RECONCILED",
    "status_timeline": [],
    "webhook_events": []
  }
}
```

- `checkout_url_available`: boolean — khong tra full PayOS checkout URL.
- `webhook_events`: metadata an toan (provider, event_type, signature_valid, processed, received_at).

## 4. FE Behavior

- **Out of scope Commerce FE.**
- Consumer: Admin Service — `admin_api_fe_behavior/ViewPaymentSupportDetail-api-and-behavior.md`.

## 5. Business Rules

- `ViewPaymentSupportDetailUseCase` — read-only transaction.
- `reconciliation_status` tu `PaymentSupportReconciliationPolicy`.
- Khong tra `provider_response`, webhook raw JSON, API key.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 403 | `COMMERCE-403` | Thieu `PAYMENT_SUPPORT_READ` |
| 404 | `COMMERCE-404-PAYMENT` | Payment khong ton tai |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewPaymentSupportDetail.md`
- Admin proxy: `docs/api_fe_behavior/admin_api_fe_behavior/ViewPaymentSupportDetail-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-commerce-support-read.md`