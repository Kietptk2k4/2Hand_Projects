# Process GHN Webhook – API & Behavior

## 1. Business Goal

Nhan webhook tu GHN, ghi log raw payload, cap nhat `shipments` / `order_items` theo mapping status, ghi history + outbox. Idempotent khi status khong doi.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/shipments/webhooks/ghn`
- **Auth:** Public (permitAll). Verify `Token` hoac `Authorization: Bearer` neu `COMMERCE_GHN_WEBHOOK_SECRET` duoc cau hinh.

### Request body (flexible)

He thong doc cac field pho bien:

| Field | Vi tri |
|-------|--------|
| `OrderCode` / `order_code` / `ghn_order_code` | root hoac `Data` / `data` |
| `Status` / `status` | root hoac `Data` / `data` |

```json
{
  "OrderCode": "GHN-MOCK-770E8400",
  "Status": "delivered"
}
```

## 3. Response – Success

**HTTP 200 OK** (luon tra 200 de GHN khong retry vo han, ke ca shipment khong tim thay)

```json
{
  "code": 200,
  "success": true,
  "message": "Da nhan GHN webhook.",
  "data": {
    "ghn_order_code": "GHN-MOCK-770E8400",
    "raw_status": "delivered",
    "signature_valid": true,
    "processed": true,
    "shipment_found": true,
    "status_changed": true,
    "previous_status": "SHIPPED",
    "new_status": "DELIVERED",
    "shipment_id": "770e8400-e29b-41d4-a716-446655440002"
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:01Z"
}
```

## 4. GHN status mapping

| Raw (vi du) | Shipment status | Order items |
|-------------|-----------------|-------------|
| picking | `PICKING_UP` | `SHIPPED` |
| ready_to_pick, picked, sorting | `READY_TO_SHIP` | `SHIPPED` |
| delivering, transport | `SHIPPED` | `SHIPPED` |
| delivered | `DELIVERED` | `DELIVERED` (khong `COMPLETED`) |
| delivery_fail, exception | `FAILED` | `FAILED` |
| cancel | `CANCELLED` | — |
| return, returned | `RETURNED` | `RETURNED` |

Transition nguoc (vd `DELIVERED` → `SHIPPED`) bi bo qua, log van `processed=true`.

## 5. Processing flow

1. Insert `ghn_webhook_logs` (luon).
2. Verify token (neu bat).
3. Lock shipment theo `ghn_order_code` (`FOR UPDATE`).
4. Map status; neu khong map / khong tim shipment → ack, log giu hoac `processed=false`.
5. Neu status doi: update shipment, `shipment_status_history.raw_status`, order items, outbox `COMMERCE_SHIPMENT_STATUS_CHANGED`.
6. Mark log `processed=true`.

## 6. Errors / edge cases

| Truong hop | `processed` | Hanh vi |
|------------|-------------|---------|
| Invalid token | false | Khong cap nhat domain |
| Shipment not found | false | Log giu de debug/retry job |
| Status unchanged | true | Idempotent no-op |
| Out-of-order transition | true | Ignore, ack webhook |
| Unmapped raw status | true | Ack, khong doi shipment |

## 7. Config

```env
COMMERCE_GHN_WEBHOOK_SECRET=your-shared-secret
```

De trong secret = bo qua verify (dev/MVP).

## 8. Related

- FR: `docs/feature_requirements/commerce/FR_ProcessGHNWebhook.md`
- Flow: `docs/business_flow/commerce_business_flow/shipping-lifecycle-flow.md` §9–10
