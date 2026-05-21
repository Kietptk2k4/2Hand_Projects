# Commerce Service Database Schema

Commerce Service su dung PostgreSQL lam source-of-truth cho shop, product, inventory, cart, order, payment, shipment va review. Tai lieu nay mo ta schema logic cho MVP, gom bang, enum, constraint, relationship va index quan trong.

## 1. Naming And Type Conventions

- Primary key uu tien UUID.
- Ten bang dung `snake_case` so nhieu.
- Ten cot dung `snake_case`.
- FK den bang khac trong Commerce Service dung UUID.
- FK den user tu Auth Service chi luu `user_id`, `buyer_id`, `seller_id`, `payer_id` dang UUID reference logic; khong cross-DB FK vat ly neu khac database/service.
- Money fields dung decimal/numeric, khong dung float.
- Timestamps dung timezone-aware timestamp neu DB convention cho phep.
- JSON fields dung JSONB.

## 1.1 Object Storage (MinIO) — URL Columns

Commerce **khong** luu binary media trong PostgreSQL. Cac cot URL/text duoi day tro toi object tren **MinIO shared** (bucket theo entity). Chi tiet upload/validation: `docs/engineering_rules/commerce-object-storage.md`.

| Column | Bucket (MVP) |
|--------|----------------|
| `product_media.media_url` | `2hands-commerce-product` |
| `review_media.url` | `2hands-commerce-review` |
| `seller_shops.avatar_url`, `cover_url` | `2hands-commerce-shop` |
| `order_items.image_snapshot` | Snapshot URL (copy tu product media tai checkout) |

## 2. Core Enums

### `cart_item_status`

- `ACTIVE`
- `OUT_OF_STOCK`
- `REMOVED`
- `INVALID_PRODUCT`

### `product_status`

- `DRAFT`
- `ACTIVE`
- `OUT_OF_STOCK`
- `PAUSED`
- `ARCHIVED`
- `REMOVED`

### `shop_status`

- `ACTIVE`
- `CLOSED`
- `SUSPENDED`

### `review_status`

- `VISIBLE`
- `HIDDEN`

### `payment_method`

- `COD`
- `PAYOS`

### `order_status`

- `CREATED`
- `AWAITING_PAYMENT`
- `PROCESSING`
- `COMPLETED`
- `CANCELLED`

### `payment_status`

- `PENDING`
- `PAID`
- `FAILED`
- `CANCELLED`
- `EXPIRED`

Note: user input ban dau co `PENDING`, `PAID`, `FAILED`, `CANCELLED`; schema MVP them `EXPIRED` de phuc vu job expire payment/payOS link.

### `order_item_status`

- `PENDING`
- `PROCESSING`
- `SHIPPED`
- `DELIVERED`
- `COMPLETED`
- `CANCELLED`
- `FAILED`
- `RETURNED`

### `shipment_carrier`

- `GHN`
- `MANUAL`
- `SELF_DELIVERY`

### `shipment_type`

- `STANDARD`
- `EXPRESS`
- `SAME_DAY`

GHN mapping:

- `STANDARD` -> GHN `SWITCH_XTEAM`
- `EXPRESS` -> GHN `EXPRESS`
- `SAME_DAY` -> GHN `FAST`

### `shipment_status`

- `PENDING`
- `PICKING_UP`
- `READY_TO_SHIP`
- `SHIPPED`
- `DELIVERED`
- `FAILED`
- `CANCELLED`
- `RETURNED`

### `shipment_created_by_source`

- `SYSTEM`
- `SELLER`
- `ADMIN`

### `outbox_status`

- `PENDING`
- `PROCESSING`
- `PUBLISHED`
- `FAILED`

## 3. Tables

### 3.1 `carts`

Moi user co toi da mot cart.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | NOT NULL, UNIQUE, logical reference to Auth user |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

### 3.2 `cart_items`

Item trong cart, khong reserve stock.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `cart_id` | UUID | NOT NULL, FK -> `carts.id` |
| `product_id` | UUID | NOT NULL, FK -> `products.id` |
| `seller_id` | UUID | NOT NULL, logical seller user id |
| `quantity` | integer | NOT NULL, CHECK `quantity > 0` |
| `status` | cart_item_status | NOT NULL, default `ACTIVE` |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

Constraints:

- UNIQUE (`cart_id`, `product_id`)

### 3.3 `user_addresses`

Dia chi mutable cua buyer.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | NOT NULL, logical reference to Auth user |
| `receiver_name` | varchar | NOT NULL |
| `phone` | varchar | NOT NULL |
| `province_code` | varchar | NOT NULL |
| `district_code` | varchar | NOT NULL |
| `ward_code` | varchar | NOT NULL |
| `address_detail` | text | NOT NULL |
| `is_default` | boolean | NOT NULL, default false |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

Recommended constraint:

- Partial unique index de moi user chi co mot default address: UNIQUE (`user_id`) WHERE `is_default = true`.

### 3.4 `shipping_address_snapshots`

Snapshot dia chi giao hang tai thoi diem tao shipment. Khong thay doi khi user update address book.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `shipment_id` | UUID | NOT NULL, UNIQUE, FK -> `shipments.id` |
| `receiver_name` | varchar | NOT NULL |
| `phone` | varchar | NOT NULL |
| `province_code` | varchar | NOT NULL |
| `district_code` | varchar | NOT NULL |
| `ward_code` | varchar | NOT NULL |
| `address_detail` | text | NOT NULL |
| `full_address` | text | NOT NULL |
| `created_at` | timestamp | NOT NULL |

### 3.5 `orders`

Order tong cua buyer.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `buyer_id` | UUID | NOT NULL, logical reference to Auth user |
| `total_amount` | numeric | NOT NULL, CHECK `total_amount >= 0` |
| `final_amount` | numeric | NOT NULL, CHECK `final_amount >= 0` |
| `payment_method` | payment_method | NOT NULL |
| `status` | order_status | NOT NULL |
| `payment_status` | payment_status | NOT NULL, default `PENDING` |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |
| `completed_at` | timestamp | NULLABLE |

Business constraints:

- Order created -> `payment_status = PENDING`.
- Order completed iff all order items are `COMPLETED` and `payment_status = PAID`.
- For COD, payment becomes `PAID` when buyer confirms receipt after delivery.
- For payOS, payment becomes `PAID` from valid webhook success.

### 3.6 `order_items`

Snapshot tung product trong order, gan voi seller va optional shipment.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `order_id` | UUID | NOT NULL, FK -> `orders.id` |
| `shipment_id` | UUID | NULLABLE, FK -> `shipments.id` |
| `product_id` | UUID | NOT NULL, FK -> `products.id` |
| `seller_id` | UUID | NOT NULL, logical seller user id |
| `quantity` | integer | NOT NULL, CHECK `quantity > 0` |
| `unit_price_snapshot` | numeric | NOT NULL, CHECK `unit_price_snapshot >= 0` |
| `final_price` | numeric | NOT NULL, CHECK `final_price >= 0` |
| `sku_snapshot` | varchar | NULLABLE |
| `product_name_snapshot` | varchar | NOT NULL |
| `image_snapshot` | text | NULLABLE, snapshot URL tu MinIO `2hands-commerce-product` tai checkout |
| `attributes_snapshot` | jsonb | NULLABLE |
| `completed_at` | timestamp | NULLABLE |
| `shipping_fee_allocated` | numeric | NOT NULL, CHECK `shipping_fee_allocated >= 0` |
| `shop_name_snapshot` | varchar | NOT NULL |
| `status` | order_item_status | NOT NULL |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

Business constraints:

- `DELIVERED` means carrier delivered but buyer has not necessarily confirmed.
- `COMPLETED` means buyer confirmed or system auto-completed.
- Review is allowed only after `COMPLETED`.

### 3.7 `shipments`

Fulfillment unit theo seller/order.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `order_id` | UUID | NOT NULL, FK -> `orders.id` |
| `seller_id` | UUID | NOT NULL, logical seller user id |
| `carrier` | shipment_carrier | NOT NULL |
| `ghn_order_code` | varchar | NULLABLE, UNIQUE |
| `ghn_shop_id` | varchar | NULLABLE |
| `tracking_number` | varchar | NULLABLE, UNIQUE |
| `shipping_fee` | numeric | NOT NULL, CHECK `shipping_fee >= 0` |
| `shipping_fee_origin` | numeric | NULLABLE, CHECK `shipping_fee_origin >= 0` |
| `estimated_delivery_date` | date | NULLABLE |
| `shipment_type` | shipment_type | NOT NULL |
| `weight_gram` | integer | NULLABLE, CHECK `weight_gram > 0` when not null |
| `cod_amount` | numeric | NOT NULL, default 0, CHECK `cod_amount >= 0` |
| `status` | shipment_status | NOT NULL |
| `external_provider_response` | jsonb | NULLABLE |
| `created_by_source` | shipment_created_by_source | NOT NULL |
| `shipped_at` | timestamp | NULLABLE |
| `delivered_at` | timestamp | NULLABLE |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

Business constraints:

- Cannot create shipment unless order is `PROCESSING`.
- For GHN shipment, `tracking_number` should equal `ghn_order_code`.
- For COD payment, `cod_amount = orders.final_amount` for relevant shipment policy.
- Shipment `DELIVERED` does not automatically complete order.

### 3.8 `ghn_webhook_logs`

Audit log cho webhook tu GHN.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `ghn_order_code` | varchar | NOT NULL |
| `status` | varchar | NOT NULL, raw GHN status |
| `payload` | jsonb | NOT NULL |
| `processed` | boolean | NOT NULL, default false |
| `created_at` | timestamp | NOT NULL |

### 3.9 `seller_shipping_profiles`

Dia chi lay hang cua seller.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `shop_id` | UUID | PK, FK -> `seller_shops.id` |
| `pickup_name` | varchar | NOT NULL |
| `phone` | varchar | NOT NULL |
| `province_code` | varchar | NOT NULL |
| `district_code` | varchar | NOT NULL |
| `ward_code` | varchar | NOT NULL |
| `address_detail` | text | NOT NULL |

### 3.10 `product_categories`

Cay category.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `name` | varchar | NOT NULL |
| `slug` | varchar | NOT NULL, UNIQUE |
| `parent_id` | UUID | NULLABLE, self FK -> `product_categories.id` |
| `is_active` | boolean | NOT NULL, default true |
| `level` | integer | NOT NULL, CHECK `level >= 0` |
| `path` | text | NOT NULL |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

### 3.11 `products`

San pham seller dang ban.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `seller_id` | UUID | NOT NULL, logical seller user id |
| `shop_id` | UUID | NOT NULL, FK -> `seller_shops.id` |
| `product_type` | varchar | NOT NULL |
| `category_id` | UUID | NOT NULL, FK -> `product_categories.id` |
| `brand_id` | UUID | NULLABLE |
| `condition` | varchar | NOT NULL |
| `title` | varchar | NOT NULL |
| `description` | text | NOT NULL |
| `weight_gram` | integer | NOT NULL, CHECK `weight_gram > 0` |
| `status` | product_status | NOT NULL |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

### 3.12 `product_media`

Media cua product. File tren MinIO bucket `2hands-commerce-product`; DB luu `media_url`.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `product_id` | UUID | NOT NULL, FK -> `products.id` |
| `media_url` | text | NOT NULL |
| `media_type` | varchar | NOT NULL |
| `sort_order` | integer | NOT NULL, default 0 |
| `created_at` | timestamp | NOT NULL |

Recommended constraint:

- UNIQUE (`product_id`, `sort_order`) neu UI can thu tu duy nhat.

### 3.13 `product_prices`

Gia va sale price theo thoi gian.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `product_id` | UUID | NOT NULL, FK -> `products.id` |
| `price` | numeric | NOT NULL, CHECK `price >= 0` |
| `sale_price` | numeric | NULLABLE, CHECK `sale_price <= price` when not null |
| `start_at` | timestamp | NOT NULL |
| `end_at` | timestamp | NULLABLE |
| `created_at` | timestamp | NOT NULL |

Business rules:

- Active price la record co `start_at <= now` va (`end_at is null` hoac `end_at > now`).
- Nen tranh active price overlap cho cung product.

### 3.14 `product_attributes`

Thuoc tinh san pham.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `product_id` | UUID | NOT NULL, FK -> `products.id` |
| `attribute_name` | varchar | NOT NULL |
| `attribute_value` | varchar | NOT NULL |

Constraints:

- UNIQUE (`product_id`, `attribute_name`)

### 3.15 `product_inventories`

Ton kho theo product.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `product_id` | UUID | PK, FK -> `products.id` |
| `stock_quantity` | integer | NOT NULL, CHECK `stock_quantity >= 0` |
| `low_stock_threshold` | integer | NOT NULL, CHECK `low_stock_threshold >= 0` |
| `reserved_quantity` | integer | NOT NULL, default 0, CHECK `reserved_quantity >= 0` |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

Business rules:

- Checkout: `stock_quantity -= quantity`, `reserved_quantity += quantity`.
- Payment success: `reserved_quantity -= quantity`.
- Payment fail/expire/cancel: `reserved_quantity -= quantity`, `stock_quantity += quantity`.
- Use row lock or optimistic locking in checkout to avoid oversell.

### 3.16 `seller_shops`

Shop cua seller.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `seller_id` | UUID | NOT NULL, UNIQUE, logical reference to Auth user |
| `shop_name` | varchar | NOT NULL |
| `description` | text | NULLABLE |
| `avatar_url` | text | NULLABLE, URL object MinIO `2hands-commerce-shop` |
| `cover_url` | text | NULLABLE, URL object MinIO `2hands-commerce-shop` |
| `status` | shop_status | NOT NULL |
| `rating_avg` | numeric | NOT NULL, default 0 |
| `rating_count` | integer | NOT NULL, default 0 |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

### 3.17 `shop_settings`

Setting cua shop.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `shop_id` | UUID | PK, FK -> `seller_shops.id` |
| `is_vacation` | boolean | NOT NULL, default false |
| `vacation_message` | text | NULLABLE |
| `updated_at` | timestamp | NOT NULL |

### 3.18 `reviews`

Review cua buyer cho order item/product/shop.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `order_item_id` | UUID | NOT NULL, UNIQUE, FK -> `order_items.id` |
| `seller_id` | UUID | NOT NULL, logical seller user id |
| `buyer_id` | UUID | NOT NULL, logical buyer user id |
| `rating` | integer | NOT NULL, CHECK `rating BETWEEN 1 AND 5` |
| `comment` | text | NULLABLE |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |
| `status` | review_status | NOT NULL, default `VISIBLE` |

Business rules:

- Cannot review unless `order_items.status = COMPLETED`.
- `buyer_id` must match `orders.buyer_id`.

### 3.19 `review_replies`

Seller reply cho review.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `review_id` | UUID | NOT NULL, FK -> `reviews.id` |
| `seller_id` | UUID | NOT NULL, logical seller user id |
| `content` | text | NOT NULL |
| `created_at` | timestamp | NOT NULL |

Recommended constraint:

- UNIQUE (`review_id`) neu MVP chi cho mot reply moi review.

### 3.20 `review_media`

Media dinh kem review. File tren MinIO bucket `2hands-commerce-review`; DB luu `url`.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `review_id` | UUID | NOT NULL, FK -> `reviews.id` |
| `url` | text | NOT NULL |
| `type` | varchar | NOT NULL |

### 3.21 `payments`

Payment cua order, ho tro COD va payOS.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `order_id` | UUID | NOT NULL, UNIQUE, FK -> `orders.id` |
| `payer_id` | UUID | NOT NULL, logical reference to Auth user |
| `amount` | numeric | NOT NULL, CHECK `amount > 0` |
| `currency` | varchar | NOT NULL, default `VND` |
| `payment_method` | payment_method | NOT NULL |
| `checkout_url_expired_at` | timestamp | NULLABLE |
| `status` | payment_status | NOT NULL |
| `payos_order_code` | varchar | NULLABLE |
| `payos_checkout_url` | text | NULLABLE |
| `payos_transaction_id` | varchar | NULLABLE |
| `provider_response` | jsonb | NULLABLE |
| `idempotency_key` | varchar | NULLABLE, UNIQUE |
| `paid_at` | timestamp | NULLABLE |
| `expired_at` | timestamp | NULLABLE |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

Business rules:

- payOS fields nullable when payment method is COD.
- For payOS, `payos_order_code` should be unique when present.

### 3.22 `payment_webhook_logs`

Webhook log cua payOS.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `provider` | varchar | NOT NULL, default `PAYOS` |
| `event_type` | varchar | NOT NULL |
| `payos_order_code` | varchar | NOT NULL |
| `payload` | jsonb | NOT NULL |
| `signature_valid` | boolean | NOT NULL |
| `processed` | boolean | NOT NULL, default false |
| `created_at` | timestamp | NOT NULL |

Constraints:

- UNIQUE (`provider`, `payos_order_code`, `event_type`)

### 3.23 `seller_payout_accounts`

Tai khoan payout cua seller. MVP chua can nghiep vu payout that, nhung luu cau truc de mo rong.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `seller_id` | UUID | NOT NULL, logical seller user id |
| `bank_name` | varchar | NOT NULL |
| `bank_account_name` | varchar | NOT NULL |
| `bank_account_number` | varchar | NOT NULL |
| `is_default` | boolean | NOT NULL, default false |
| `created_at` | timestamp | NOT NULL |
| `updated_at` | timestamp | NOT NULL |

### 3.24 `order_status_history`

Audit order status change.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `order_id` | UUID | NOT NULL, FK -> `orders.id` |
| `old_status` | order_status | NULLABLE for initial state |
| `new_status` | order_status | NOT NULL |
| `changed_by` | varchar | NOT NULL, user id string or `SYSTEM` |
| `note` | text | NULLABLE |
| `created_at` | timestamp | NOT NULL |

### 3.25 `payment_status_history`

Audit payment status change.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `payment_id` | UUID | NOT NULL, FK -> `payments.id` |
| `old_status` | payment_status | NULLABLE for initial state |
| `new_status` | payment_status | NOT NULL |
| `payload` | jsonb | NULLABLE |
| `created_at` | timestamp | NOT NULL |

### 3.26 `shipment_status_history`

Audit shipment status change.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `shipment_id` | UUID | NOT NULL, FK -> `shipments.id` |
| `old_status` | shipment_status | NULLABLE for initial state |
| `new_status` | shipment_status | NOT NULL |
| `raw_status` | varchar | NULLABLE, raw provider status |
| `created_at` | timestamp | NOT NULL |

### 3.27 `outbox_events`

Outbox pattern cho event-driven integration.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `event_type` | varchar | NOT NULL |
| `event_key` | varchar | NOT NULL |
| `aggregate_id` | UUID | NOT NULL |
| `source` | varchar | NOT NULL, example `commerce`, `payment`, `shipment` |
| `payload` | jsonb | NOT NULL |
| `status` | outbox_status | NOT NULL, default `PENDING` |
| `retry_count` | integer | NOT NULL, default 0 |
| `last_error` | text | NULLABLE |
| `created_at` | timestamp | NOT NULL |
| `published_at` | timestamp | NULLABLE |

Recommended constraints:

- UNIQUE (`event_key`) neu producer tao event key idempotent.

## 4. Relationships

- Users 1 - N `user_addresses`.
- Users 1 - 1 `carts`.
- Users 1 - 0..1 `seller_shops`.
- Users buyer 1 - N `orders`.
- `carts` 1 - N `cart_items`.
- `cart_items` N - 1 `products`.
- `cart_items` N - 1 `seller_shops` logically via `seller_id`/shop ownership.
- `product_categories` 1 - N `product_categories` via `parent_id`.
- `product_categories` 1 - N `products`.
- `seller_shops` 1 - 1 `shop_settings`.
- `seller_shops` 1 - 1 `seller_shipping_profiles`.
- `seller_shops` 1 - N `products`.
- `products` 1 - 1 `product_inventories`.
- `products` 1 - N `product_prices`.
- `products` 1 - N `product_attributes`.
- `products` 1 - N `product_media`.
- `orders` 1 - N `order_items`.
- `orders` 1 - N `shipments`.
- `orders` 1 - 1 `payments`.
- `order_items` N - 1 `products`.
- `order_items` N - 1 `shipments` nullable.
- `order_items` N - 1 `seller_shops` logically via seller/shop.
- `shipments` N - 1 `seller_shops` logically via seller/shop.
- `shipments` 1 - 1 `shipping_address_snapshots`.
- `shipments` 1 - N `ghn_webhook_logs` logically via `ghn_order_code`.
- `payments` 1 - N `payment_webhook_logs` logically via `payos_order_code`.
- `payments` 1 - N `payment_status_history`.
- `orders` 1 - N `order_status_history`.
- `shipments` 1 - N `shipment_status_history`.
- `order_items` 1 - 0..1 `reviews`.
- `reviews` N - 1 Users via `buyer_id`.
- `reviews` N - 1 `seller_shops` logically via seller/shop.
- `reviews` 1 - N `review_media`.
- `reviews` 1 - 0..1 `review_replies`.

## 5. Indexes

Required indexes:

```sql
CREATE INDEX idx_order_items_order
ON order_items(order_id);

CREATE INDEX idx_order_items_seller
ON order_items(seller_id);

CREATE INDEX idx_order_items_status
ON order_items(status);

CREATE INDEX idx_order_items_shipment
ON order_items(shipment_id);

CREATE INDEX idx_shipments_order
ON shipments(order_id);

CREATE INDEX idx_shipments_seller
ON shipments(seller_id);

CREATE INDEX idx_order_items_product
ON order_items(product_id);

CREATE INDEX idx_inventory_stock
ON product_inventories(stock_quantity);

CREATE UNIQUE INDEX idx_shipments_tracking
ON shipments(tracking_number)
WHERE tracking_number IS NOT NULL;

CREATE INDEX idx_order_items_order_created
ON order_items(order_id, created_at DESC);

CREATE INDEX idx_shipments_seller_status
ON shipments(seller_id, status);

CREATE INDEX idx_payments_order
ON payments(order_id);

CREATE INDEX idx_payments_status
ON payments(status);

CREATE INDEX idx_payments_payos_order_code
ON payments(payos_order_code);

CREATE INDEX idx_shipments_ghn_code
ON shipments(ghn_order_code);

CREATE INDEX idx_webhook_logs_processed
ON payment_webhook_logs(processed, created_at);

CREATE INDEX idx_ghn_webhook_processed
ON ghn_webhook_logs(processed, created_at);

CREATE INDEX idx_payout_accounts_seller
ON seller_payout_accounts(seller_id);

CREATE INDEX idx_order_history
ON order_status_history(order_id, created_at DESC);

CREATE INDEX idx_payment_history
ON payment_status_history(payment_id, created_at DESC);

CREATE INDEX idx_shipment_history
ON shipment_status_history(shipment_id, created_at DESC);

CREATE INDEX idx_outbox_pending
ON outbox_events(status, created_at)
WHERE status = 'PENDING';
```

Additional recommended indexes:

```sql
CREATE INDEX idx_products_shop_status
ON products(shop_id, status);

CREATE INDEX idx_products_category_status
ON products(category_id, status);

CREATE INDEX idx_products_seller_status
ON products(seller_id, status);

CREATE INDEX idx_product_prices_product_time
ON product_prices(product_id, start_at DESC, end_at);

CREATE INDEX idx_cart_items_cart_status
ON cart_items(cart_id, status);

CREATE INDEX idx_orders_buyer_created
ON orders(buyer_id, created_at DESC);

CREATE INDEX idx_orders_status_created
ON orders(status, created_at);

CREATE INDEX idx_reviews_seller_created
ON reviews(seller_id, created_at DESC);

CREATE INDEX idx_reviews_order_item
ON reviews(order_item_id);

CREATE UNIQUE INDEX idx_user_default_address
ON user_addresses(user_id)
WHERE is_default = true;
```

## 6. State And Data Integrity Notes

- `cart_items` khong dai dien cho stock lock.
- `product_inventories.stock_quantity` la available stock trong MVP.
- `product_inventories.reserved_quantity` la stock dang bi giu sau checkout.
- Checkout phai update inventory va tao order trong transaction.
- Payment webhook phai idempotent.
- GHN webhook phai idempotent.
- Status history phai duoc ghi khi order/payment/shipment doi trang thai.
- Outbox event phai ghi cung transaction voi domain change.
- Snapshot fields trong `order_items` va `shipping_address_snapshots` khong update theo product/address mutable sau khi order tao.

## 7. MVP Out Of Scope Tables / Fields

Cac nghiep vu sau co the co cot/bang placeholder nhung chua can implement full:

- Seller payout thuc te tu `seller_payout_accounts`.
- Refund/dispute/return workflow day du.
- Voucher/promotion.
- Multi-warehouse inventory.
- Advanced reconciliation voi GHN/payOS.