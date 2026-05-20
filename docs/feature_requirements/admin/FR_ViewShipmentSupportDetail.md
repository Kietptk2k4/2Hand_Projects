# Functional Requirement - View Shipment Support Detail

## 1. Feature Overview

Cho phep support admin xem chi tiet shipment va tracking de ho tro giao hang, khieu nai va dieu tra van chuyen.

## 2. Actors

- **Support Admin:** View shipment detail.
- **Commerce Service/GHN:** Own shipment and tracking data.

## 3. Scope

**In Scope:**

- View shipment status, tracking code, carrier, fees and timestamps.
- View delivery milestones when available.

**Out of Scope:**

- Manual shipment status mutation.
- Direct GHN admin operations.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/support/shipments/{shipmentId}`

**Auth:** Required, permission `SHIPMENT_SUPPORT_VIEW`.

## 5. Business Rules

- Shipment data is retrieved from Commerce/internal carrier integration.
- Address/contact data should be masked unless full access is needed.
- Support view must distinguish internal status from carrier status.

## 6. Database Impact

- Optional insert `admin_action_logs`.
- No Commerce shipment mutation.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Protect buyer/seller addresses and phone numbers.

## 9. Failure Cases

- Shipment not found -> 404.
- Commerce/carrier unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized support admin can view shipment details.
- Shipment status and carrier tracking are clear.
- Admin Service does not mutate shipment.

