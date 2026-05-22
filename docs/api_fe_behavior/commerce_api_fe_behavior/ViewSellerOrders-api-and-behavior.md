# View Seller Orders – API & Behavior

## 1. Business Goal

Seller xem danh sach **order items** thuoc shop minh (scope `order_items.seller_id`), kem tom tat order/payment/shipment de fulfillment. Trong multi-seller order, seller chi thay phan cua minh.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/seller/orders`
- **Auth:** Bearer JWT (seller)

### Query params

| Param | Type | Default | Mo ta |
|-------|------|---------|-------|
| `page` | int | 1 | >= 1 |
| `limit` | int | 20 | 1–50 |
| `status` | string | — | Loc `order_items.status` |
| `shipment_status` | string | — | Loc `shipments.status` (chi item da co shipment) |

### `status` values

`PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `COMPLETED`, `CANCELLED`, `FAILED`, `RETURNED`

### `shipment_status` values

`PENDING`, `PICKING_UP`, `READY_TO_SHIP`, `SHIPPED`, `DELIVERED`, `FAILED`, `CANCELLED`, `RETURNED`

### Response `data`

| Field | Mo ta |
|-------|-------|
| `items[]` | Danh sach order item cua seller |
| `pagination` | `page`, `limit`, `total_items`, `total_pages`, `has_next` |

### Order item (`items[]`)

| Field | Mo ta |
|-------|-------|
| `order_item_id`, `order_id`, `product_id` | IDs |
| `quantity`, `unit_price_snapshot`, `final_price`, `shipping_fee_allocated` | Snapshot gia/ship |
| `product_name_snapshot`, `image_snapshot` | Snapshot san pham |
| `item_status`, `item_created_at`, `item_updated_at` | Trang thai item |
| `order_status`, `order_payment_status`, `order_payment_method`, `order_created_at` | Tom tat order |
| `payment` | `payment_id`, `status`, `payment_method`, `amount`, `currency` (khong co PayOS/provider) |
| `shipment_summary` | `shipment_id`, `status`, `carrier`, `tracking_number`, `delivery_address_summary` hoac `null` |

**Khong** tra `buyer_id`, PayOS fields, provider response.

## 3. Business rules

- Query bat buoc scope `oi.seller_id = JWT user_id`.
- Seller phai co shop → neu khong: **409** `COMMERCE-409-SELLER-SHOP`.
- Sap xep mac dinh: `order_items.created_at DESC`.
- Loc `shipment_status`: chi item co `shipment_id` va shipment match (item chua ship khong match filter shipment).

## 4. FE Behavior

- Dung cho man hinh quan ly don / chuan bi ship.
- Ket hop `POST /seller/order-items/process` cho item `PENDING`.
- Ket hop shipment APIs cho item `PROCESSING`.

## 5. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 409 | `COMMERCE-409-SELLER-SHOP` | Seller chua co shop |
| 400 | `COMMERCE-400-PAGINATION` | `page` / `limit` khong hop le |
| 400 | `COMMERCE-400-VALIDATION` | `status` / `shipment_status` khong hop le |

## 6. Related

- FR: `docs/feature_requirements/commerce/FR_ViewSellerOrders.md`
- Process items: `ProcessSellerOrderItem-api-and-behavior.md`
- UC: `docs/use_cases/commerce_use_cases/uc-seller-order-management.md` (5.1)
