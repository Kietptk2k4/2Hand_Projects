# Functional Requirement - View Order Support Detail (Commerce)

## 1. Feature Overview

Commerce Service expose read-only order detail cho admin/support co permission, phuc vu dieu tra tranh chap va van hanh. Admin Service la consumer chinh; Commerce FE khong dung.

## 2. Actors

- **Support Admin:** Tra cuu order (qua Admin Service).
- **Commerce Service:** Own order data va tra projection.

## 3. Scope

**In Scope:**

- View order header, items, payment summary, shipments, timeline.
- Permission gate `ORDER_SUPPORT_READ`.

**Out of Scope:**

- Order mutation/refund.
- Commerce buyer/seller UI.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/admin/support/orders/{orderId}`

**Auth:** Required — `ORDER_SUPPORT_READ`

## 5. Business Rules

- Read-only.
- Same data shape as buyer order detail, without buyer ownership check.
- Admin Service may mask PII before admin UI.

## 6. Database Impact

- Read `orders`, `order_items`, `payments`, `shipments`, histories.

## 7. Transaction

- Read-only.

## 8. Security

- Admin permission required in JWT.
- No direct DB access from Admin Service.

## 9. Failure Cases

- Order not found -> 404.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized support can read order detail via Commerce API.
- No order state mutation.
- Admin integration documented separately.

## 11. Related

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewOrderSupportDetail-api-and-behavior.md`
- Admin FR: `docs/feature_requirements/admin/FR_ViewOrderSupportDetail.md`
- UC: `docs/use_cases/commerce_use_cases/uc-commerce-support-read.md`