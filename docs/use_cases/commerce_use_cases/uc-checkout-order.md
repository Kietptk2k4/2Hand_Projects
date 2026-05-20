# UC - Checkout And Order

## 1. Overview

Use case nay mo ta nghiep vu checkout tu cart va quan ly order cua buyer trong Commerce Service. Checkout la write use case quan trong nhat vi phai revalidate cart/product/shop/price/stock, reserve inventory, tao order/order items/payment, snapshot du lieu mua hang va dam bao idempotency.

## 2. Actors

- **Buyer:** Checkout, xem order, cancel order, confirm received.
- **Seller:** Xu ly order items sau khi order vao `PROCESSING`.
- **System:** Expire unpaid order, auto complete delivered order.

## 3. Related Data

- `carts`
- `cart_items`
- `user_addresses`
- `shipping_address_snapshots`
- `products`
- `product_prices`
- `product_inventories`
- `seller_shops`
- `orders`
- `order_items`
- `payments`
- `shipments`
- `order_status_history`
- `payment_status_history`
- `outbox_events`

## 4. Business Rules

- Checkout khong tin client price/cart status.
- Checkout phai revalidate product, shop, category, price, stock va address.
- Cart khong reserve stock; checkout moi reserve stock.
- Checkout reserve inventory:
  - `stock_quantity -= quantity`
  - `reserved_quantity += quantity`
- Order item phai snapshot product/shop/price data.
- Moi order co mot payment.
- Order `COMPLETED` iff all order items `COMPLETED` va payment status `PAID`.
- Shipment khong duoc tao neu order chua `PROCESSING`.
- Cancel allowed khi order `CREATED/AWAITING_PAYMENT` va shipment chua start.

## 5. Sub-Use Cases

### 5.1. Checkout From Cart

**Goal:** Buyer tao order tu selected cart items.

**Preconditions:**

- Buyer da dang nhap.
- Buyer co cart.
- Buyer chon it nhat mot cart item.
- Buyer chon address va payment method.

**Main Flow:**

1. Buyer gui selected `cart_item_ids`, `address_id`, `payment_method`, shipping option neu co.
2. System load cart theo `buyer_id`.
3. System load selected cart items, bo qua `REMOVED`.
4. System load product, shop, category, active price va inventory.
5. System lock inventory rows theo product ids.
6. System validate product `ACTIVE`, shop `ACTIVE`, active price exists, stock du.
7. System load selected address by `buyer_id`.
8. System tinh total amount, shipping fee, final amount.
9. System reserve inventory.
10. System tao `orders`.
11. System tao `order_items` voi snapshots.
12. System tao `payments`.
13. System ghi status history va outbox events.
14. System commit transaction.
15. System tra order/payment instruction.

**Exception Flow:**

- Cart item invalid/not owned -> 400/404.
- Product inactive/removed -> 409.
- Shop inactive/vacation-blocked -> 409.
- Active price missing -> 409.
- Stock insufficient -> 409.
- Address not found/not owned -> 404.
- Payment method invalid -> 400.
- Concurrent stock conflict -> 409.

**Postconditions:**

- Order, order items va payment duoc tao.
- Inventory da reserve.
- Outbox events duoc ghi.

### 5.2. Calculate Order Total

**Goal:** System tinh tong tien order truoc khi tao order.

**Preconditions:**

- Selected items hop le.
- Active prices ton tai.

**Main Flow:**

1. System tinh unit price = sale price neu co, nguoc lai price.
2. System tinh item final price = unit price * quantity.
3. System tinh shipping fee theo seller group.
4. System tinh `total_amount`.
5. System tinh `final_amount = total_amount + shipping_fee`.

**Exception Flow:**

- Price invalid -> 409.
- Shipping fee provider fail -> fallback mock neu configured, nguoc lai 503.

**Postconditions:**

- Amounts duoc dung de tao order/payment.

### 5.3. View Order List

**Goal:** Buyer xem lich su order cua minh.

**Preconditions:**

- Buyer da dang nhap.

**Main Flow:**

1. Buyer request order list.
2. System query `orders` by `buyer_id`.
3. System load payment/shipment/order item summary.
4. System tra paginated order list.

**Exception Flow:**

- Khong co order -> empty list.

**Postconditions:**

- Khong thay doi database.

### 5.4. View Order Detail

**Goal:** Buyer xem chi tiet order, payment, shipment va items.

**Preconditions:**

- Order ton tai va thuoc buyer.

**Main Flow:**

1. Buyer request order detail.
2. System load order by `order_id` va `buyer_id`.
3. System load order items, payment, shipments, snapshots.
4. System tra detail response.

**Exception Flow:**

- Order not found/not owned -> 404.

**Postconditions:**

- Khong thay doi database.

### 5.5. Cancel Order

**Goal:** Buyer huy order khi chua bat dau fulfillment.

**Preconditions:**

- Buyer da dang nhap.
- Order thuoc buyer.
- Order con cancellable.

**Main Flow:**

1. Buyer request cancel order.
2. System load order, payment, order items, shipments.
3. System validate `orders.status IN (CREATED, AWAITING_PAYMENT)`.
4. System validate shipment chua ton tai hoac tat ca shipment `PENDING`.
5. System set order `CANCELLED`.
6. System set pending order items `CANCELLED`.
7. System cancel pending payment.
8. System release reserved inventory neu payment chua success.
9. System ghi status histories va outbox events.
10. System tra success.

**Exception Flow:**

- Order not found/not owned -> 404.
- Order not cancellable -> 409.
- Shipment already picking/shipped/delivered -> 409.

**Postconditions:**

- Order cancelled.
- Pending reservation released.

### 5.6. Confirm Received

**Goal:** Buyer xac nhan da nhan hang va hoan tat delivered items.

**Preconditions:**

- Buyer da dang nhap.
- Order thuoc buyer.
- Order items da `DELIVERED`.

**Main Flow:**

1. Buyer request confirm received.
2. System load order/items/payment.
3. System validate delivered items.
4. System mark delivered order items `COMPLETED`.
5. Neu payment method `COD`, mark payment/order payment status `PAID`.
6. System complete order neu all items completed va payment paid.
7. System ghi histories va outbox events.
8. System tra updated order.

**Exception Flow:**

- Order not found/not owned -> 404.
- Items not delivered -> 409.
- Payment state invalid -> 409.

**Postconditions:**

- Order items completed.
- COD payment paid.
- Order completed neu du dieu kien.

## 6. Transaction Rules

Bat buoc transaction cho:

- Checkout.
- Cancel order.
- Confirm received.
- Auto complete delivered order.

Checkout transaction phai gom:

- Reserve inventory.
- Create order.
- Create order items.
- Create payment.
- Status histories.
- Outbox events.

## 7. Security

- JWT required.
- Buyer chi xem/cancel/confirm order cua minh.
- Seller order management la use case rieng, khong dung buyer endpoints.

## 8. Acceptance Criteria

- Checkout reject product/shop/price/stock/address invalid.
- Checkout atomically reserve inventory va create order/payment.
- Order items co snapshot.
- Cancel order release reserved stock khi payment chua success.
- Confirm received complete delivered items va set COD paid.
- Order completed chi khi all items completed va payment paid.

