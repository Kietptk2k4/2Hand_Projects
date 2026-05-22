# View Shipment – API & Behavior

## 1. Business Goal

Buyer hoac seller xem chi tiet shipment (carrier, tracking, status, phi ship, ETA, order items gan shipment, dia chi giao snapshot, lich su status) theo quyen so huu.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/shipments/{shipmentId}`
- **Auth:** Bearer JWT (buyer hoac seller)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `shipmentId` | UUID | ID shipment |

## 3. Response – Success

**HTTP 200 OK**

Tra ve thong tin shipment day du, gom:

| Field | Mo ta |
|-------|-------|
| `shipment_id`, `order_id`, `seller_id` | IDs |
| `accessed_as` | `BUYER` hoac `SELLER` (theo JWT vs ownership) |
| `carrier`, `shipment_type`, `status` | Thong tin van chuyen |
| `ghn_order_code`, `tracking_number` | Tracking |
| `shipping_fee`, `cod_amount`, `weight_gram` | Phi / COD / trong luong |
| `estimated_delivery_date`, `shipped_at`, `delivered_at` | ETA / thoi gian |
| `created_at`, `updated_at` | Audit |
| `shipping_address` | Snapshot dia chi giao hang |
| `order_items[]` | Item gan shipment |
| `status_history[]` | Timeline `shipment_status_history` |

**Khong** tra payment provider / PayOS fields.

### Ownership

- **Buyer:** `orders.buyer_id = JWT user_id`
- **Seller:** `shipments.seller_id = JWT user_id`
- User khong lien quan → **404** `COMMERCE-404-SHIPMENT`

## 4. Related endpoints

- Tracking timeline ngan: `GET /commerce/api/v1/shipments/{shipmentId}/tracking`
- Seller quan ly: `GET /commerce/api/v1/seller/shipments/{shipmentId}` (cung data shape, seller-only path)

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 404 | `COMMERCE-404-SHIPMENT` | Khong ton tai hoac khong thuoc buyer/seller |
| 401 | `COMMERCE-401` | Thieu JWT |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ViewShipment.md`
- Track: `TrackShipment-api-and-behavior.md` (neu co)
- Seller detail: `ManageSellerShipment-api-and-behavior.md`
