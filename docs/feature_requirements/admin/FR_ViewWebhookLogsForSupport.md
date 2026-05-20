# Functional Requirement - View Webhook Logs For Support

## 1. Feature Overview

Cho phep support/admin xem webhook logs lien quan den payment/shipment de debug provider callback va idempotency.

## 2. Actors

- **Support Admin:** Inspect webhook log.
- **Commerce Service:** Owns PayOS/GHN webhook records.

## 3. Scope

**In Scope:**

- View webhook event type, provider id, processing status, retry count and timestamps.
- Show sanitized payload summary.

**Out of Scope:**

- Replaying webhook from Admin MVP.
- Editing webhook logs.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/support/webhook-logs`

**Auth:** Required, permission `WEBHOOK_SUPPORT_VIEW`.

**Query params:**

- `provider`
- `reference_id`
- `status`
- `from`
- `to`
- `page`
- `size`

## 5. Business Rules

- Raw signatures/secrets must be hidden.
- Logs are read-only.
- Provider idempotency key should be visible if safe.

## 6. Database Impact

- Read Commerce support API result.
- Optional insert `admin_action_logs`.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Payload must be sanitized.

## 9. Failure Cases

- Invalid filters -> 400.
- Commerce unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Support can troubleshoot webhook processing.
- Sensitive raw data is protected.
- Webhook logs are immutable through this feature.

