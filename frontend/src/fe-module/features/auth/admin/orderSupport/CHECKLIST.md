# Order Support Admin UI — Checklist

## Routing & shell
- [x] `adminTabs.js` — section `orderSupport`
- [x] `adminUrlParams.js` — `orderId`, `paymentId`, `shipmentId`, webhook filters
- [x] `AdminPage.jsx` — tab map + `onNotify` + `onNavigate`
- [x] `AdminSupportTargetBar.jsx` — tra cuu UUID

## Permissions (auth V3 + JWT re-login)
- [x] `SHIPMENT_SUPPORT_READ` — xem chi tiet van chuyen
- [x] `SHIPMENT_SUPPORT_WRITE` — hien form ghi de
- [x] `SHIPMENT_SUPPORT_FORCE_WRITE` — checkbox force
- [x] `orderSupportPermissions.js` + `useOrderSupportPermissions.js`

## Shipment support detail (read)
- [x] `getShipmentSupportDetail` — GET `/admin/api/v1/support/shipments/{id}`
- [x] `ShipmentSupportDetailTab` — status, tracking, address masked, history, webhook events
- [x] Link sang order detail / webhook logs GHN
- [x] Error states: 401, 403, 404, 503

## Shipment status override (exception path)
- [x] `overrideShipmentStatus` — PATCH `/admin/api/v1/support/shipments/{id}/status`
- [x] `ShipmentStatusOverrideCard` — dropdown, reason, force, confirm
- [x] Transition filter GHN / MANUAL (`shipmentOverrideConstants.js`)
- [x] Client validation reason 10–500 ky tu
- [x] Refresh detail sau success
- [x] Toast qua `onNotify`
- [ ] MSW handler PATCH override (local mock — optional)

## Backend (Phases 0–2)
- [x] Auth seed permissions `SHIPMENT_SUPPORT_WRITE`, `SHIPMENT_SUPPORT_FORCE_WRITE`
- [x] Commerce `PATCH .../commerce/api/v1/admin/support/shipments/{id}/status`
- [x] Admin gateway forward + audit `SHIPMENT_STATUS_OVERRIDE`
- [x] `AdminActionLogPolicy` critical action type
- [x] Unit + integration tests (admin-service)

## Manual QA — Read
- [ ] Admin co `SHIPMENT_SUPPORT_READ` mo duoc shipment detail
- [ ] Admin thieu READ → 403 / forbidden state
- [ ] UUID khong ton tai → 404
- [ ] Commerce down → 503 unavailable state
- [ ] PII masked (`contact_fields_masked`)

## Manual QA — Override happy path
- [ ] Shipment GHN `SHIPPED` → override `DELIVERED` + reason >= 10 ky tu → 200
- [ ] UI refresh: `internal_status` = `DELIVERED`
- [ ] `status_history` co entry `raw_status: admin_override`
- [ ] Admin audit log `SHIPMENT_STATUS_OVERRIDE` (critical payload co before/after)
- [ ] Commerce DB: `order_items` status cap nhat (vd. `DELIVERED`)
- [ ] **Khong** co outbound HTTP sang GHN khi override (log/monitor)

## Manual QA — Validation & errors
- [ ] Reason < 10 ky tu → 400 validation (FE + API)
- [ ] Transition khong hop le (vd. `PENDING` → `DELIVERED` GHN) → 409
- [ ] Cung status → 200 unchanged message, khong duplicate history
- [ ] Thieu `SHIPMENT_SUPPORT_WRITE` → 403, form an
- [ ] Session het han → 401, session expired flow

## Manual QA — Force override
- [ ] Shipment terminal `DELIVERED`, `force=false` → 409
- [ ] Cung shipment, `force=true` + `SHIPMENT_SUPPORT_FORCE_WRITE` → 200
- [ ] `force=true` nhung thieu FORCE_WRITE → 403

## Manual QA — Integration
- [ ] `admin.integrations.commerce.enabled=false` → 503 khi override
- [ ] Commerce `enabled=true`, Bearer forward dung permission Commerce
- [ ] Re-login admin sau auth migration V3 de co permission moi

## Build
- [x] `npm run build` passes (frontend)
- [x] `commerce-service` tests pass (override use case)
- [ ] Full `admin-service` test suite (integration env)

## Docs
- [x] `docs/business_flow/admin_business_flow/order-support-flow.md`
- [x] `docs/use_cases/admin_use_cases/uc-order-support.md` (UC 5.5)
- [x] `docs/api_fe_behavior/admin_api_fe_behavior/AdminOverrideShipmentStatus-api-and-behavior.md`
- [x] `docs/business_flow/commerce_business_flow/shipping-lifecycle-flow.md` §10

## Stitch alignment
- [ ] Visual pass vs `frontend/stitch/ViewShipmentSupportDetail/` (override section)
