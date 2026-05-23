# View Shipment Support Detail – API & Behavior

## 1. Business Goal

Cho phép admin/support xem **chi tiết vận đơn** (tracking, carrier, milestones) phục vụ khiếu nại giao hàng. Dữ liệu do Commerce Service sở hữu.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/support/shipments/{shipmentId}` | Bearer + `SHIPMENT_SUPPORT_READ` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Shipment support detail retrieved successfully",
  "data": {
    "shipment_id": "uuid",
    "order_id": "uuid",
    "seller_id": "uuid",
    "buyer_id": "uuid",
    "order_status": "PROCESSING",
    "carrier": "GHN",
    "shipment_type": "STANDARD",
    "internal_status": "SHIPPED",
    "carrier_status": "transporting",
    "ghn_order_code": "GHN-123",
    "tracking_number": "TRACK-9",
    "shipping_fee": 30000,
    "cod_amount": 0,
    "shipped_at": "2026-05-20T08:00:00Z",
    "delivered_at": null,
    "shipping_address": {
      "receiver_name": "Nguyen ***",
      "phone": "***4567",
      "province_code": "79",
      "address_detail": "***",
      "full_address": "***"
    },
    "status_history": [
      {
        "old_status": "PENDING",
        "new_status": "SHIPPED",
        "raw_status": "transporting",
        "occurred_at": "2026-05-20T08:00:00Z"
      }
    ],
    "carrier_webhook_events": [
      {
        "carrier_status": "transporting",
        "processed": true,
        "received_at": "2026-05-20T08:00:01Z"
      }
    ],
    "contact_fields_masked": true
  }
}
```

- `internal_status`: trạng thái nội bộ Commerce (`ShipmentStatus`).
- `carrier_status`: trạng thái carrier (GHN raw status / webhook mới nhất).
- Không trả webhook `payload`, `external_provider_response`.

## 3. Commerce integration

Khi `admin.integrations.commerce.enabled=true`:

- Admin forward Bearer token.
- Commerce: `GET /commerce/api/v1/admin/support/shipments/{shipmentId}` (`SHIPMENT_SUPPORT_READ`).

Khi integration **tắt**: `503`.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SHIPMENT_SUPPORT_READ` |
| 404 | ADMIN-404 | Shipment không tồn tại |
| 503 | ADMIN-503 | Commerce tắt / không khả dụng |

## 5. Business Rules

- Read-only; không mutate shipment.
- Audit `SHIPMENT_SUPPORT_VIEW` (non-critical).
- Mask shipping contact (tên, SĐT, địa chỉ chi tiết).

## 6. FE Integration

1. Từ order support → mở shipment → `GET .../support/shipments/{shipmentId}`.
2. Hiển thị song song `internal_status` và `carrier_status`.
3. Timeline + bảng `carrier_webhook_events` cho điều tra GHN.

## 7. Related

- FR: `docs/feature_requirements/admin/FR_ViewShipmentSupportDetail.md`
- Permission JWT: `SHIPMENT_SUPPORT_READ`
- Audit action: `SHIPMENT_SUPPORT_VIEW`
