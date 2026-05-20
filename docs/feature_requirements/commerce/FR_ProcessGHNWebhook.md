# Functional Requirement - Process GHN Webhook

## 1. Feature Overview

Xu ly webhook tu GHN de cap nhat shipment status va attached order item statuses. Webhook phai idempotent va luu raw payload de debug.

## 2. Actors

- **GHN:** Gui webhook tracking status.
- **System:** Log webhook, map status, update shipment/order items.

## 3. Scope

**In Scope:**

- Log GHN webhook.
- Resolve shipment by `ghn_order_code`.
- Map raw status to domain shipment status.
- Update shipment status/history.
- Update attached order item statuses.
- Publish outbox events.

**Out of Scope:**

- Refund/dispute when failed.
- Manual reconciliation.

## 4. API Contract

**Endpoint:** `POST /commerce/api/v1/shipments/webhooks/ghn`

**Auth:** Provider verification/signature/IP policy if available.

## 5. Business Rules

- Always insert `ghn_webhook_logs`.
- Duplicate status is idempotent.
- GHN delivered -> shipment `DELIVERED`, order items `DELIVERED`.
- Shipment delivered does not set order item `COMPLETED`.
- Raw status stored in `shipment_status_history.raw_status`.

## 6. Database Impact

- Insert `ghn_webhook_logs`.
- Read/update `shipments`.
- Update `order_items`.
- Insert `shipment_status_history`.
- Insert outbox events.

## 7. Transaction

- Required for domain status update.
- Lock shipment row before transition.

## 8. Security

- Verify provider source if GHN supports.
- Do not expose raw payload publicly.

## 9. Failure Cases

- Unknown `ghn_order_code` -> log unprocessed.
- Duplicate webhook -> no-op.
- Out-of-order status -> ignore or log warning according transition policy.

## 10. Acceptance Criteria

- GHN webhook updates shipment status idempotently.
- Delivered shipment marks order items delivered only.
- Raw payload/status are logged.
- Unknown shipment does not crash endpoint.

