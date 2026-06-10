# Functional Requirement - Admin Override Shipment Status

## 1. Feature Overview

Cho phep support admin **cap nhat trang thai shipment noi bo** khi webhook/sync GHN cham, mat hoac khong kha dung (sandbox, dieu tra khieu nai, unblock COD/payout test). Day la **exception path** — khong thay the luong GHN webhook chinh thuc.

**Quan trong:** Override chi cap nhat **Commerce DB** (`shipments`, `order_items`, `shipment_status_history`, outbox). **Khong** goi GHN API de doi trang thai tren he thong GHN.

## 2. Actors

- **Support Admin / Super Admin:** Override shipment status voi ly do bat buoc.
- **Admin Service:** Kiem tra permission, ghi `admin_action_logs`, forward request sang Commerce.
- **Commerce Service:** Validate transition, apply status change, ghi history + outbox.

## 3. Scope

**In Scope:**

- Admin override `ShipmentStatus` cho shipment `carrier = GHN` (uu tien MVP).
- Ho tro override cho `MANUAL` / `SELF_DELIVERY` khi seller khong the tu cap nhat (tuy chon cung endpoint).
- Bat buoc `reason` (audit).
- Ghi `shipment_status_history` voi `raw_status = admin_override`.
- Cap nhat `order_items` gan shipment theo mapping hien co (`GhnShipmentStatusPolicy` / `ManualShipmentStatusPolicy`).
- Publish outbox `COMMERCE_SHIPMENT_STATUS_CHANGED` (+ lifecycle events neu co).
- Ghi `admin_action_logs` action `SHIPMENT_STATUS_OVERRIDE`.

**Out of Scope:**

- Dong bo nguoc len GHN (`switch-status`, webhook gia).
- Refund/dispute/payout reversal tu dong.
- Override `cod_amount`, `shipping_fee`, tracking tren GHN.
- Seller tu cap nhat GHN shipment (da bi chan — giu nguyen).
- Bulk override nhieu shipment.

## 4. API Contract

### 4.1 Admin Service (gateway)

**Endpoint:** `PATCH /admin/api/v1/support/shipments/{shipmentId}/status`

**Auth:** Required, permission `SHIPMENT_SUPPORT_WRITE`.

**Request body:**

```json
{
  "status": "DELIVERED",
  "reason": "GHN webhook khong ve sau 48h, xac nhan giao hang qua hotline GHN",
  "force": false
}
```

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `status` | string | Yes | `ShipmentStatus` enum (UPPER_SNAKE) |
| `reason` | string | Yes | Ly do audit, min 10 ky tu, max 500 |
| `force` | boolean | No | Default `false`. `true` can permission `SHIPMENT_SUPPORT_FORCE_WRITE` (Super Admin) de chuyen tu terminal status |

**Success (200):** envelope chuan + `data` shipment summary sau override.

### 4.2 Commerce Service (owner mutation)

**Endpoint:** `PATCH /commerce/api/v1/admin/support/shipments/{shipmentId}/status`

**Auth:** Bearer JWT — permission `SHIPMENT_SUPPORT_WRITE` (claims admin).

Request/response body tuong tu Admin gateway. Admin Service forward Bearer token khi `admin.integrations.commerce.enabled=true`.

## 5. Business Rules

### 5.1 Permission & audit

- `admin_id` lay tu JWT — khong nhan tu body.
- `reason` bat buoc; reject neu trong hoac < 10 ky tu.
- Admin Service ghi `admin_action_logs`:
  - `action_type`: `SHIPMENT_STATUS_OVERRIDE`
  - `target_type`: `SHIPMENT`
  - `target_id`: `shipmentId`
  - `request_payload`: `{ "status", "reason", "force" }` (redact neu can)
- Commerce khong ghi `admin_action_logs` — Admin Service chiu trach nhiem audit.

### 5.2 Carrier & preconditions

- Shipment phai ton tai.
- Order lien quan thuong o `PROCESSING` (khong reject neu da `COMPLETED` — chi log warning neu can).
- **Khong** goi GHN truoc/sau override.

### 5.3 Status transition policy

Ap dung `GhnShipmentStatusPolicy.canTransition(current, target)` cho `carrier = GHN`:

| From | Allowed to |
|------|------------|
| `PENDING` | `PICKING_UP`, `READY_TO_SHIP`, `SHIPPED`, `CANCELLED`, `FAILED` |
| `PICKING_UP` | `READY_TO_SHIP`, `SHIPPED`, `CANCELLED`, `FAILED` |
| `READY_TO_SHIP` | `SHIPPED`, `CANCELLED`, `FAILED` |
| `SHIPPED` | `DELIVERED`, `FAILED`, `RETURNED` |
| `DELIVERED` | `RETURNED` |
| `FAILED`, `CANCELLED`, `RETURNED` | (khong cho phep, tru khi `force=true`) |

Ap dung `ManualShipmentStatusPolicy` cho `MANUAL` / `SELF_DELIVERY` neu endpoint ho tro ca hai carrier.

- `force = false` + shipment da terminal (`DELIVERED`, `CANCELLED`, `RETURNED`, `FAILED`) → reject `409`.
- `force = true` → can `SHIPMENT_SUPPORT_FORCE_WRITE`; van ghi history + audit day du.

### 5.4 Side effects (giong webhook/sync)

Khi status doi:

1. Update `shipments.status`, `shipped_at` / `delivered_at` neu policy hien co ap dung.
2. Insert `shipment_status_history` (`old_status`, `new_status`, `raw_status = admin_override`, `occurred_at`).
3. Update `order_items` theo mapping:
   - `PICKING_UP` / `READY_TO_SHIP` / `SHIPPED` → `SHIPPED`
   - `DELIVERED` → `DELIVERED`
   - `FAILED` → `FAILED`
   - `RETURNED` → `RETURNED`
4. Insert outbox `COMMERCE_SHIPMENT_STATUS_CHANGED`.
5. **Khong** auto-complete order / order items → `COMPLETED` (buyer confirm hoac job 7 ngay).

### 5.5 COD & tai chinh

- Override `DELIVERED` **khong** dong nghia GHN da thu COD ve tai khoan san.
- Admin chiu trach nhiem xac minh ngoai he thong truoc khi override len `DELIVERED` voi don COD.
- Tham chieu: `docs/business_flow/commerce_business_flow/seller-finance-cod-payout-flow.md`.

### 5.6 Idempotency

- Request cung `status` da hien tai → `200` no-op (khong duplicate history/outbox).

## 6. Database Impact

**Commerce Service:**

- Read/update `shipments`
- Update `order_items` (status)
- Insert `shipment_status_history`
- Insert `outbox_events`

**Admin Service:**

- Insert `admin_action_logs`
- Khong mutate Commerce DB truc tiep

## 7. Transaction

- Commerce: `@Transactional` tren use case write — lock shipment row truoc transition.
- Admin: transaction local cho audit log; goi Commerce HTTP sau khi authorize.

## 8. Security

- Permission `SHIPMENT_SUPPORT_WRITE` bat buoc.
- `SHIPMENT_SUPPORT_FORCE_WRITE` cho `force=true` tu terminal status.
- Khong log full JWT / secret.
- Chi admin role co shipment support write.

## 9. Failure Cases

| Case | HTTP | Code |
|------|------|------|
| Thieu JWT | 401 | `ADMIN-401` / `COMMERCE-401` |
| Thieu permission write | 403 | `ADMIN-403` / `COMMERCE-403` |
| Shipment khong ton tai | 404 | `ADMIN-404` / `COMMERCE-404-SHIPMENT` |
| `reason` thieu / qua ngan | 400 | `ADMIN-400-VALIDATION` / `COMMERCE-400-VALIDATION` |
| Status enum khong hop le | 400 | `COMMERCE-400` |
| Transition khong hop le | 409 | `COMMERCE-409-SHIPMENT-STATUS` |
| Terminal status, `force=false` | 409 | `COMMERCE-409-SHIPMENT-STATUS` |
| Commerce integration tat | 503 | `ADMIN-503` |

## 10. Acceptance Criteria

- Admin co `SHIPMENT_SUPPORT_WRITE` co the override status hop le voi `reason`.
- `shipment_status_history` co `raw_status = admin_override`.
- `order_items` duoc cap nhat dung mapping.
- Outbox event duoc ghi khi status thay doi.
- Admin action log duoc ghi voi `SHIPMENT_STATUS_OVERRIDE`.
- GHN khong duoc goi trong luong override.
- Order item khong tu dong `COMPLETED` khi shipment `DELIVERED`.
- Request trung status hien tai → no-op thanh cong.

## 11. Related Documents

- `docs/business_flow/commerce_business_flow/shipping-lifecycle-flow.md` (section Admin Override Exception Path)
- `docs/feature_requirements/admin/FR_ViewShipmentSupportDetail.md`
- `docs/feature_requirements/commerce/FR_ProcessGHNWebhook.md`
- `docs/api_fe_behavior/admin_api_fe_behavior/AdminOverrideShipmentStatus-api-and-behavior.md`
- `docs/business_flow/admin_business_flow/order-support-flow.md`
