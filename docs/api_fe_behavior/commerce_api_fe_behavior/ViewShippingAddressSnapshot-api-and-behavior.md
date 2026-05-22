# View Shipping Address Snapshot – API & Behavior

## 1. Business Goal

Buyer hoac seller xem **shipping address snapshot** immutable cua shipment (dia chi tai thoi diem tao shipment). Cap nhat address book **khong** lam thay doi snapshot.

## 2. API Contract

- **Method:** GET
- **URL:** `/commerce/api/v1/shipments/{shipmentId}/address-snapshot`
- **Auth:** Bearer JWT (buyer hoac seller)

### Path params

| Param | Type | Mo ta |
|-------|------|-------|
| `shipmentId` | UUID | ID shipment |

### Response `data`

| Field | Mo ta |
|-------|-------|
| `shipment_id` | ID shipment |
| `snapshot_id` | ID ban ghi snapshot |
| `accessed_as` | `BUYER` hoac `SELLER` |
| `created_at` | Thoi diem tao snapshot |
| `address_snapshot` | Noi dung dia chi immutable |

### `address_snapshot`

| Field | Mo ta |
|-------|-------|
| `receiver_name` | Ten nguoi nhan |
| `phone` | So dien thoai |
| `province_code`, `district_code`, `ward_code` | Ma dia ban |
| `address_detail` | Dia chi chi tiet |
| `full_address` | Dia chi day du hien thi |

## 3. Ownership

- **Buyer:** `orders.buyer_id = JWT user_id`
- **Seller:** `shipments.seller_id = JWT user_id`
- Khong co quyen hoac snapshot chua tao → **404** `COMMERCE-404-SHIPMENT`

## 4. Security (PII)

- Dia chi la PII; bat buoc ownership check.
- User khong lien quan khong duoc truy cap.

## 5. FE Behavior

- Snapshot **read-only**; khong co API update tren endpoint nay.
- Dung cho man hinh fulfillment (seller) hoac theo doi don (buyer).
- Chi tiet shipment day du: `GET /shipments/{shipmentId}`.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 404 | `COMMERCE-404-SHIPMENT` | Shipment/snapshot khong ton tai hoac khong owned |
| 401 | `COMMERCE-401` | Thieu JWT |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_ViewShippingAddressSnapshot.md`
- View shipment: `ViewShipment-api-and-behavior.md`
