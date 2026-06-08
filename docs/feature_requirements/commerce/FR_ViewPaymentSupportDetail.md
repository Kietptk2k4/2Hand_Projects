# Functional Requirement - View Payment Support Detail (Commerce)

## 1. Feature Overview

Commerce Service expose chi tiet payment read-only cho support admin, gom timeline va webhook summary an toan.

## 2. Actors

- **Support Admin:** Tra cuu payment (qua Admin Service).
- **Commerce Service:** Own payment records.

## 3. Scope

**In Scope:**

- Payment status, amount, provider refs, reconciliation status.
- Safe webhook event metadata.

**Out of Scope:**

- Refund execution.
- Provider secret exposure.

## 4. API Contract

**Endpoint:** `GET /commerce/api/v1/admin/support/payments/{paymentId}`

**Auth:** Required — `PAYMENT_SUPPORT_READ`

## 5. Business Rules

- Read-only.
- Hide raw provider payload and secrets.
- Reconciliation status computed by domain policy.

## 6. Database Impact

- Read `payments`, histories, webhook logs summary.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Sanitized webhook metadata only.

## 9. Failure Cases

- Payment not found -> 404.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Support can inspect payment state with permission.
- No secrets in response.
- Admin Service proxies without owning payment DB.

## 11. Related

- API: `docs/api_fe_behavior/commerce_api_fe_behavior/ViewPaymentSupportDetail-api-and-behavior.md`
- Admin FR: `docs/feature_requirements/admin/FR_ViewPaymentSupportDetail.md`