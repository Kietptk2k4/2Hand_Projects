# UC - Order Support

## 1. Overview

Use case mo ta support MVP-lite cho order/payment/shipment: read-only tra cuu va **exception path** ghi de trang thai shipment noi bo. Admin Service khong thuc hien real refund/dispute/payout reversal.

## 2. Actors

- **Support Admin:** Xem support data; override shipment status (co quyen write).
- **Super Admin:** Force override tu terminal status.
- **Commerce Service:** Owner order/payment/shipment data va mutation shipment status.
- **Admin Service:** Gateway, permission, audit.

## 3. Related Data

- Commerce support APIs.
- `admin_action_logs` (read audit + `SHIPMENT_STATUS_OVERRIDE` critical audit).

## 4. Business Rules

- Support read requires permission.
- Override chi mutate Commerce qua HTTP API — Admin khong truy cap Commerce DB.
- Override khong goi GHN API.
- Sensitive provider payloads redacted.
- `reason` bat buoc khi override (10–500 ky tu).

## 5. Sub-Use Cases

### 5.1. View Order Support Detail

**Main Flow:** Admin checks permission, calls Commerce support API and returns order/status/item snapshots.

### 5.2. View Payment Support Detail

**Main Flow:** Return payment status, method, amount, webhook summary, no provider secrets.

### 5.3. View Shipment Support Detail

**Main Flow:** Return shipment status, tracking, provider response summary, status history, masked address.

### 5.4. View Webhook Logs

**Main Flow:** Return payment/GHN webhook logs for support troubleshooting.

### 5.5. Admin Override Shipment Status

**Goal:** Cap nhat trang thai shipment noi bo khi webhook/sync GHN khong du (exception path).

**Preconditions:**

- Admin co `SHIPMENT_SUPPORT_WRITE`.
- Shipment ton tai tren Commerce.
- `admin.integrations.commerce.enabled=true` (Admin gateway).

**Main Flow:**

1. Admin mo tab Chi tiet van chuyen (shipment UUID).
2. Admin chon trang thai moi, nhap ly do (>= 10 ky tu).
3. (Tuy chon) Bat `force` neu co `SHIPMENT_SUPPORT_FORCE_WRITE`.
4. Admin xac nhan dialog va submit.
5. Admin Service authorize, forward PATCH sang Commerce.
6. Commerce validate transition, update shipment/order_items/history/outbox.
7. Admin Service ghi critical audit `SHIPMENT_STATUS_OVERRIDE`.
8. FE refresh detail; timeline hien `raw_status: admin_override`.

**Alternate Flows:**

- Status khong doi → 200 no-op.
- Transition khong hop le → 409.
- Terminal status, `force=false` → 409.
- Thieu permission → 403.

**Postconditions:**

- `shipments.status` cap nhat (neu khac truoc).
- `shipment_status_history` co ban ghi moi voi `raw_status = admin_override`.
- `order_items` cap nhat theo mapping policy.
- Outbox event khi status thay doi.

**Out of Scope:**

- Goi GHN `switch-status` hoac webhook gia.
- Auto-complete order khi `DELIVERED`.

## 6. Acceptance Criteria

- Support doc read-only details voi permission.
- Override thanh cong ghi audit + history `admin_override`.
- Khong refund/dispute mutation.
- Sensitive provider data redacted.
- Force override chi voi `SHIPMENT_SUPPORT_FORCE_WRITE`.

## 7. Related Documents

- `docs/business_flow/admin_business_flow/order-support-flow.md`
- `docs/feature_requirements/admin/FR_AdminOverrideShipmentStatus.md`
- `frontend/src/fe-module/features/auth/admin/orderSupport/CHECKLIST.md`
