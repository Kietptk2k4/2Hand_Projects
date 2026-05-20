# UC - Order Support

## 1. Overview

Use case nay mo ta support-read MVP-lite cho order/payment/shipment. Admin Service khong thuc hien real refund/dispute/payout reversal trong MVP.

## 2. Actors

- **Support Admin:** Xem order/payment/shipment support data.
- **Commerce Service:** Own order/payment/shipment data.

## 3. Related Data

- Commerce support APIs/projections.
- Optional `admin_action_logs` for support read audit.

## 4. Business Rules

- Support read requires permission.
- No mutation in MVP.
- Sensitive provider payloads should be redacted.
- Admin Service must not access Commerce DB directly.

## 5. Sub-Use Cases

### 5.1. View Order Support Detail

**Main Flow:** Admin checks permission, calls Commerce support API and returns order/status/item snapshots.

### 5.2. View Payment Support Detail

**Main Flow:** Return payment status, method, amount, webhook summary, no provider secrets.

### 5.3. View Shipment Support Detail

**Main Flow:** Return shipment status, tracking, provider response summary and history.

### 5.4. View Webhook Logs

**Main Flow:** Return payment/GHN webhook logs for support troubleshooting.

## 6. Acceptance Criteria

- Support can view read-only details with permission.
- No refund/dispute mutation exists in MVP.
- Sensitive provider data is redacted.

