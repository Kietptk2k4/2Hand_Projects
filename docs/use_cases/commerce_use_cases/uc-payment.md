# UC - Payment

## 1. Overview

Use case nay mo ta nghiep vu thanh toan trong Commerce Service cho MVP. Commerce Service own payment, thay the Payment Service rieng. Payment ho tro `COD` va `PAYOS`, xu ly checkout URL, redirect, webhook, payment status, expiration va dong bo order/inventory.

## 2. Actors

- **Buyer:** Chon payment method, thanh toan payOS, xem payment status.
- **System:** Xu ly payOS webhook, expire payment, retry webhook.
- **payOS:** External payment provider.

## 3. Related Data

- `orders`
- `order_items`
- `payments`
- `payment_webhook_logs`
- `payment_status_history`
- `product_inventories`
- `outbox_events`

## 4. Business Rules

- Moi order co mot payment.
- Payment amount phai bang `orders.final_amount`.
- `payments.status` bat dau la `PENDING`.
- payOS payment paid chi tu valid webhook success, khong tu redirect.
- COD payment paid khi buyer confirm received hoac auto complete theo policy.
- payOS failed/cancelled/expired phai cancel awaiting order va release reserved stock.
- Payment webhook phai verify signature.
- Webhook processing phai idempotent.
- Payment success khong tru `stock_quantity` lan nua; checkout da reserve stock.

## 5. Sub-Use Cases

### 5.1. Create Payment

**Goal:** Tao payment record cho order trong checkout.

**Preconditions:**

- Order dang duoc tao trong checkout.
- Buyer/payment method hop le.

**Main Flow:**

1. Checkout use case tao order.
2. System tao `payments` voi `order_id`, `payer_id`, `amount`, `currency`, `payment_method`, `status = PENDING`.
3. System ghi `payment_status_history`.
4. System ghi outbox event `COMMERCE_PAYMENT_CREATED`.

**Exception Flow:**

- Amount <= 0 -> checkout fail.
- Duplicate payment for order -> 409/data conflict.

**Postconditions:**

- Order co one pending payment.

### 5.2. Create payOS Checkout URL

**Goal:** Tao URL de buyer thanh toan online qua payOS.

**Preconditions:**

- Buyer da dang nhap.
- Order thuoc buyer.
- Payment method `PAYOS`.
- Payment status `PENDING`.
- Order status `AWAITING_PAYMENT`.

**Main Flow:**

1. Buyer request payOS checkout URL.
2. System load order/payment by buyer.
3. System validate payment pending.
4. Neu payment da co checkout URL con han, return URL cu.
5. System goi payOS create payment link.
6. System luu `payos_order_code`, `payos_checkout_url`, `checkout_url_expired_at`, `provider_response`.
7. System tra checkout URL.

**Exception Flow:**

- Order/payment not found -> 404.
- Payment not pending -> 409.
- Payment method not PAYOS -> 400/409.
- payOS unavailable -> 502/503, payment van pending.

**Postconditions:**

- Payment co checkout URL de buyer redirect.

### 5.3. Handle payOS Redirect

**Goal:** Frontend xu ly buyer quay ve tu payOS success/cancel URL.

**Preconditions:**

- Buyer da quay ve frontend tu provider.

**Main Flow:**

1. Frontend nhan redirect success/cancel.
2. Frontend goi API xem payment/order status.
3. System tra status hien tai trong DB.
4. Neu webhook chua den, frontend hien thi waiting/pending.

**Exception Flow:**

- Payment not found -> 404.

**Postconditions:**

- Redirect khong truc tiep thay doi payment state.

### 5.4. Process payOS Webhook

**Goal:** Cap nhat payment/order/inventory theo webhook payOS hop le.

**Preconditions:**

- payOS gui webhook payload.

**Main Flow:**

1. Webhook endpoint nhan raw payload va headers.
2. System verify signature.
3. System insert `payment_webhook_logs` voi `signature_valid`.
4. Neu signature invalid, system khong update domain state.
5. System resolve payment by `payos_order_code`.
6. System check idempotency/current payment status.
7. System map provider event sang payment status.
8. Neu success:
   - set payment `PAID`.
   - set order `payment_status = PAID`.
   - set order `status = PROCESSING`.
   - settle reservation: `reserved_quantity -= quantity`.
9. Neu failed/cancelled:
   - set payment `FAILED`/`CANCELLED`.
   - cancel order neu dang awaiting payment.
   - release reserved stock.
10. System ghi histories va outbox events.
11. System mark webhook log processed.

**Exception Flow:**

- Invalid signature -> log, no state change.
- Payment not found -> log webhook, no domain update.
- Duplicate webhook -> no-op/idempotent success.
- Payment terminal conflict -> no-op hoac log warning.

**Postconditions:**

- Payment/order/inventory reflect provider result.

### 5.5. Pay COD

**Goal:** Hoan tat payment COD khi buyer da nhan hang.

**Preconditions:**

- Order payment method `COD`.
- Shipment/order items da `DELIVERED`.
- Buyer confirm received hoac auto complete policy kich hoat.

**Main Flow:**

1. Buyer confirm received hoac system auto-complete delivered order.
2. System load COD payment.
3. System set payment `PAID`.
4. System set order `payment_status = PAID`.
5. System complete order neu all items completed.
6. System ghi histories va outbox events.

**Exception Flow:**

- Order items not delivered -> 409.
- Payment not COD -> 409.
- Payment already paid -> idempotent success.

**Postconditions:**

- COD payment paid.

### 5.6. View Payment Status

**Goal:** Buyer xem trang thai thanh toan cua order.

**Preconditions:**

- Buyer da dang nhap.
- Order thuoc buyer.

**Main Flow:**

1. Buyer request payment status.
2. System load order by `buyer_id`.
3. System load payment by `order_id`.
4. System tra payment status response.

**Exception Flow:**

- Order/payment not found -> 404.

**Postconditions:**

- Khong thay doi database.

### 5.7. Expire Payment

**Goal:** System expire payOS payment pending qua han.

**Preconditions:**

- Payment method `PAYOS`.
- Payment status `PENDING`.
- `expired_at` hoac `checkout_url_expired_at` < now.

**Main Flow:**

1. Job tim expired pending payOS payments.
2. System lock payment/order rows.
3. System set payment `EXPIRED`.
4. System cancel order neu order dang `CREATED/AWAITING_PAYMENT`.
5. System release reserved inventory.
6. System ghi histories va outbox events.

**Exception Flow:**

- Payment da `PAID` do webhook success den truoc -> no-op.
- Order khong con cancellable -> log/support.

**Postconditions:**

- Pending payOS payment expired.
- Awaiting order cancelled.
- Reserved stock released.

## 6. Provider Event Mapping

| Provider/domain event | Payment status | Order effect | Inventory effect |
|---|---|---|---|
| `PAYMENT_SUCCESS` | `PAID` | `payment_status = PAID`, `status = PROCESSING` | `reserved_quantity -= quantity` |
| `PAYMENT_FAILED` | `FAILED` | cancel awaiting order | release reserved stock |
| `PAYMENT_CANCELLED` | `CANCELLED` | cancel awaiting order | release reserved stock |
| Expiration job | `EXPIRED` | cancel awaiting order | release reserved stock |

## 7. Security

- Buyer chi xem/payment action tren order cua minh.
- Webhook endpoint verify signature.
- Khong log secret/signature key/token.
- Seller khong xem provider payment details cua buyer.

## 8. Acceptance Criteria

- Payment created one-to-one with order.
- payOS checkout URL chi tao cho pending payOS payment cua buyer.
- Redirect khong mark payment paid.
- Valid webhook success marks payment/order paid and settles reserved stock.
- Failed/cancelled/expired payment releases reserved stock.
- COD payment paid only after delivered confirmation/auto-complete.
- Duplicate webhook/job khong double update inventory.

