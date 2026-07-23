# View Shipments For Support – API & Behavior

## 1. Business Goal

Cho phép admin/support **lọc và duyệt danh sách vận chuyển** phục vụ tra cứu ticket GHN, theo dõi tracking và liên kết sang order detail / override trạng thái.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/support/shipments` | Bearer + `SHIPMENT_SUPPORT_READ` |

### Query parameters

| Param | Type | Mô tả |
|-------|------|--------|
| `q` | string | Tìm theo `shipment_id`, `order_id`, `tracking_number`, `ghn_order_code` (ILIKE) |
| `status` | string | `PENDING`, `READY_TO_SHIP`, `SHIPPED`, `DELIVERED`, `FAILED`, `CANCELLED` |
| `carrier` | string | `GHN`, `MANUAL`, `SELF_DELIVERY` |
| `order_id` | UUID | Lọc theo đơn hàng |
| `from` | ISO-8601 | `created_at >= from` |
| `to` | ISO-8601 | `created_at <= to` |
| `sort` | string | `created_at`, `updated_at`, `shipped_at` (default `updated_at`) |
| `page` | int | Trang (1-based), default `1` |
| `size` | int | Kích thước trang, default `20` |

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Shipment support list retrieved successfully",
  "data": {
    "page": 1,
    "size": 20,
    "total_elements": 12,
    "total_pages": 1,
    "shipments": [
      {
        "shipment_id": "uuid",
        "order_id": "uuid",
        "seller_id": "uuid",
        "carrier": "GHN",
        "internal_status": "SHIPPED",
        "tracking_number": "TRACK-123",
        "ghn_order_code": "GHN-456",
        "shipped_at": "2026-07-23T12:16:48Z",
        "created_at": "2026-07-22T10:00:00Z",
        "updated_at": "2026-07-23T12:16:48Z"
      }
    ]
  }
}
```

## 3. Commerce integration

Khi `admin.integrations.commerce.enabled=true`:

- Admin forward Bearer token tới Commerce.
- Commerce: `GET /commerce/api/v1/admin/support/shipments` với cùng query params.

Khi integration **tắt**: `503`.

## 4. Response – Error

| HTTP | code | Mô tả |
|------|------|--------|
| 401 | ADMIN-401 | Thiếu JWT |
| 403 | ADMIN-403 | Thiếu `SHIPMENT_SUPPORT_READ` |
| 400 | ADMIN-400 | Query không hợp lệ (status/carrier/sort/date/order_id) |
| 503 | ADMIN-503 | Commerce integration tắt hoặc không khả dụng |

## 5. Business Rules

- Read-only list; override status qua `PATCH .../shipments/{id}/status`.
- `q` tìm trên nhiều cột; không bắt buộc full UUID.
- `from` / `to` lọc theo `created_at`.
- Ghi audit `SHIPMENT_SUPPORT_VIEW` (non-critical).

## 6. FE Integration

1. Màn **Chi tiết vận chuyển** (`tab=shipment-detail`) → `GET .../support/shipments` với filter URL `sh_*`.
2. Tra cứu nhanh UUID → set `shipmentId` + mở drawer.
3. `shipmentView=summary|timeline|webhooks|override` cho drawer tabs.
4. URL params: `sh_q`, `sh_order_id`, `sh_from`, `sh_to`, `sh_status`, `sh_carrier`, `sh_page`, `sh_size`.
5. MSW: `adminOrderSupportHandlers.js` + `adminOrderSupportData.js`.

## 7. Related

- Detail: [ViewShipmentSupportDetail-api-and-behavior.md](./ViewShipmentSupportDetail-api-and-behavior.md)
- Override: [AdminOverrideShipmentStatus-api-and-behavior.md](./AdminOverrideShipmentStatus-api-and-behavior.md)
