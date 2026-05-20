# Functional Requirement - View Payment Support Detail

## 1. Feature Overview

Cho phep support admin xem thong tin payment lien quan den order de ho tro issue thanh toan, webhook va reconciliation.

## 2. Actors

- **Support Admin:** View payment detail.
- **Commerce Service/Payment Provider:** Own payment/payment gateway records.

## 3. Scope

**In Scope:**

- View payment method, amount, status, provider reference and timestamps.
- View safe webhook/reconciliation status.

**Out of Scope:**

- Refund execution.
- Payment state mutation.
- Direct gateway secret exposure.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/support/payments/{paymentId}`

**Auth:** Required, permission `PAYMENT_SUPPORT_VIEW`.

## 5. Business Rules

- Admin reads Commerce payment data through internal API.
- Provider secrets/signatures must be hidden.
- Failed/cancelled/successful states should be clear for support.

## 6. Database Impact

- Optional insert `admin_action_logs`.
- No payment DB mutation.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Mask provider raw payload when it contains sensitive data.

## 9. Failure Cases

- Payment not found -> 404.
- Commerce unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Payment support details are visible to authorized support.
- No secrets are exposed.
- Admin Service does not change payment status.

