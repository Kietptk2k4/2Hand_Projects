# Commerce Service Database Schema

Trái tim của hệ thống 2Hands, quản lý luồng mua bán, sản phẩm và kho hàng. Sử dụng **PostgreSQL**.

## 1. PRODUCTS
- `id`: UUID (PK)
- `shop_id`: UUID (FK -> SHOPS)
- `category_id`: UUID (FK -> CATEGORIES)
- `name`: String
- `slug`: String (UNIQUE)
- `description`: Text
- `base_price`: Decimal
- `condition`: Enum (NEW, LIKE_NEW, USED_GOOD, USED_FAIR)
- `status`: Enum (DRAFT, ACTIVE, HIDDEN, SOLD_OUT, DELETED)
- `is_approved`: BOOLEAN
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 2. PRODUCT_IMAGES
- `id`: UUID (PK)
- `product_id`: UUID (FK -> PRODUCTS)
- `url`: String
- `is_main`: BOOLEAN
- `sort_order`: Integer

## 3. PRODUCT_INVENTORIES
- `product_id`: UUID (PK, FK -> PRODUCTS)
- `stock_quantity`: Integer
- `sold_quantity`: Integer
- `reserved_quantity`: Integer (Số lượng tạm giữ khi đang checkout)
- `updated_at`: Timestamp

## 4. CARTS & CART_ITEMS
### CARTS
- `id`: UUID (PK)
- `user_id`: UUID (UNIQUE)
- `created_at`, `updated_at`: Timestamp
### CART_ITEMS
- `id`: UUID (PK)
- `cart_id`: UUID (FK -> CARTS)
- `product_id`: UUID (FK -> PRODUCTS)
- `seller_id`: UUID
- `quantity`: Integer
- `status`: Enum (ACTIVE, OUT_OF_STOCK, REMOVED, INVALID_PRODUCT)

## 5. ORDERS
- `id`: UUID (PK)
- `order_code`: String (UNIQUE - hiển thị cho user)
- `buyer_id`: UUID
- `seller_id`: UUID
- `total_item_price`: Decimal
- `shipping_fee`: Decimal
- `total_amount`: Decimal
- `status`: Enum (CREATED, AWAITING_PAYMENT, PROCESSING, SHIPPING, COMPLETED, CANCELLED, REFUNDED)
- `payment_method`: Enum (COD, PAYOS)
- `created_at`, `updated_at`: Timestamp

## 6. ORDER_ITEMS
- `id`: UUID (PK)
- `order_id`: UUID (FK -> ORDERS)
- `product_id`: UUID (FK -> PRODUCTS)
- `price_at_purchase`: Decimal
- `quantity`: Integer

## 7. SHIPMENTS
- `id`: UUID (PK)
- `order_id`: UUID (FK -> ORDERS)
- `seller_id`: UUID
- `carrier`: Enum (GHN, MANUAL, SELF_DELIVERY)
- `tracking_number`: String (UNIQUE)
- `ghn_order_code`: String
- `status`: Enum (PENDING, PICKING, DELIVERING, DELIVERED, RETURNED, FAILED)
- `estimated_delivery_at`: Timestamp
- `shipped_at`, `delivered_at`: Timestamp

## 8. PAYMENTS
- `id`: UUID (PK)
- `order_id`: UUID (FK -> ORDERS)
- `payos_order_code`: Long (Dùng map với payOS)
- `payment_link_id`: String
- `checkout_url`: String
- `amount`: Decimal
- `status`: Enum (PENDING, PAID, CANCELLED, FAILED, EXPIRED)
- `paid_at`: Timestamp

## 9. USER_ADDRESSES
Lưu sổ địa chỉ của người dùng.
- `id`, `user_id`, `receiver_name`, `phone`, `province_code`, `district_code`, `ward_code`, `address_detail`, `is_default`

## 10. PRODUCT_CATEGORIES
- `id`: UUID (PK)
- `name`: String
- `slug`: String (UNIQUE)
- `parent_id`: UUID (Nullable - Cây danh mục)
- `level`: Integer

## 11. OUTBOX_EVENTS
- `id`, `event_type`, `payload`, `status`, `created_at`