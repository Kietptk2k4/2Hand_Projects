# UC - Commerce Support Read

## 1. Overview

Use case mo ta cac API tra cuu van hanh **Commerce-side** duoi `/commerce/api/v1/admin/support/**`. Day la read-only support APIs; Admin Service forward Bearer token va aggregate cho admin UI. Commerce FE buyer/seller **khong** goi cac endpoint nay.

## 2. Actors

- **Support Admin:** Tra cuu order/payment/shipment/webhook (qua Admin Service).
- **Commerce Service:** Own data va enforce permission trong JWT.
- **Admin Service:** Proxy/aggregate, mask PII neu can.

## 3. Related Data

- `orders`, `order_items`, `payments`, `shipments`
- Payment/GHN webhook log tables
- Khong ghi domain mutation

## 4. Business Rules

- Moi endpoint yeu cau permission rieng trong JWT Commerce admin claims.
- Read-only — khong refund, khong sua order/shipment/payment tu support API MVP.
- Khong tra provider secret, raw webhook payload, checkout URL day du.
- Admin Service khong truy cap DB Commerce truc tiep.

## 5. Sub-Use Cases

### 5.1. View Order Support Detail

**Endpoint:** `GET /commerce/api/v1/admin/support/orders/{orderId}`

**Permission:** `ORDER_SUPPORT_READ`

**Main Flow:**

1. Admin Service forward Bearer token toi Commerce.
2. Commerce validate permission.
3. Commerce load order detail projection (`ViewOrderSupportDetailUseCase`).
4. Tra ve shape giong buyer order detail.

**Postconditions:** Order state khong doi.

### 5.2. View Payment Support Detail

**Endpoint:** `GET /commerce/api/v1/admin/support/payments/{paymentId}`

**Permission:** `PAYMENT_SUPPORT_READ`

**Main Flow:**

1. Validate permission.
2. Load payment snapshot, timeline, webhook summaries.
3. Tinh `reconciliation_status` va `checkout_url_available`.

### 5.3. View Shipment Support Detail

**Endpoint:** `GET /commerce/api/v1/admin/support/shipments/{shipmentId}`

**Permission:** `SHIPMENT_SUPPORT_READ`

**Main Flow:**

1. Validate permission.
2. Load shipment, address snapshot, status history, carrier webhook events.
3. Resolve `carrier_status` tu webhook/history.

### 5.4. View Webhook Logs For Support

**Endpoint:** `GET /commerce/api/v1/admin/support/webhook-logs`

**Permission:** `WEBHOOK_SUPPORT_READ`

**Main Flow:**

1. Validate permission.
2. Parse filter (`provider`, `reference_id`, `status`, `from`, `to`) va pagination.
3. Tra ve page logs voi `payload_summary` sanitized.

## 6. Security

- Permission bat buoc tung loai support read.
- PII co the duoc mask o Admin Service truoc UI.
- Audit action types: `ORDER_SUPPORT_VIEW`, `PAYMENT_SUPPORT_VIEW`, `SHIPMENT_SUPPORT_VIEW`, `WEBHOOK_SUPPORT_VIEW` (Admin Service).

## 7. Acceptance Criteria

- Authorized support co the tra cuu read-only qua Commerce APIs.
- Commerce FE buyer/seller khong phu thuoc endpoints nay.
- Khong co mutation/refund trong MVP support read.

## 8. Related Docs

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewOrderSupportDetail-api-and-behavior.md` (va 3 file support con lai)
- Admin proxy: `docs/api_fe_behavior/admin_api_fe_behavior/`
- Admin UC: `docs/use_cases/admin_use_cases/uc-order-support.md`