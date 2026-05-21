-- =====================================================================
-- COMMERCE SERVICE DATABASE SCHEMA (MVP)
-- PostgreSQL
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- ENUMS
-- =====================================================================

CREATE TYPE cart_item_status AS ENUM ('ACTIVE', 'OUT_OF_STOCK', 'REMOVED', 'INVALID_PRODUCT');
CREATE TYPE product_status AS ENUM ('DRAFT', 'ACTIVE', 'OUT_OF_STOCK', 'PAUSED', 'ARCHIVED', 'REMOVED');
CREATE TYPE shop_status AS ENUM ('ACTIVE', 'CLOSED', 'SUSPENDED');
CREATE TYPE review_status AS ENUM ('VISIBLE', 'HIDDEN');
CREATE TYPE payment_method AS ENUM ('COD', 'PAYOS');
CREATE TYPE order_status AS ENUM ('CREATED', 'AWAITING_PAYMENT', 'PROCESSING', 'COMPLETED', 'CANCELLED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'EXPIRED');
CREATE TYPE order_item_status AS ENUM ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'FAILED', 'RETURNED');
CREATE TYPE shipment_carrier AS ENUM ('GHN', 'MANUAL', 'SELF_DELIVERY');
CREATE TYPE shipment_type AS ENUM ('STANDARD', 'EXPRESS', 'SAME_DAY');
CREATE TYPE shipment_status AS ENUM ('PENDING', 'PICKING_UP', 'READY_TO_SHIP', 'SHIPPED', 'DELIVERED', 'FAILED', 'CANCELLED', 'RETURNED');
CREATE TYPE shipment_created_by_source AS ENUM ('SYSTEM', 'SELLER', 'ADMIN');
CREATE TYPE outbox_status AS ENUM ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED');

-- =====================================================================
-- CORE TABLES (dependency order)
-- =====================================================================

CREATE TABLE product_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    parent_id UUID REFERENCES product_categories(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    level INTEGER NOT NULL CHECK (level >= 0),
    path TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE seller_shops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL UNIQUE,
    shop_name VARCHAR(255) NOT NULL,
    description TEXT,
    avatar_url TEXT,
    cover_url TEXT,
    status shop_status NOT NULL,
    rating_avg NUMERIC NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE shop_settings (
    shop_id UUID PRIMARY KEY REFERENCES seller_shops(id),
    is_vacation BOOLEAN NOT NULL DEFAULT FALSE,
    vacation_message TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE seller_shipping_profiles (
    shop_id UUID PRIMARY KEY REFERENCES seller_shops(id),
    pickup_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    province_code VARCHAR(50) NOT NULL,
    district_code VARCHAR(50) NOT NULL,
    ward_code VARCHAR(50) NOT NULL,
    address_detail TEXT NOT NULL
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL,
    shop_id UUID NOT NULL REFERENCES seller_shops(id),
    product_type VARCHAR(100) NOT NULL,
    category_id UUID NOT NULL REFERENCES product_categories(id),
    brand_id UUID,
    condition VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    weight_gram INTEGER NOT NULL CHECK (weight_gram > 0),
    status product_status NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    media_url TEXT NOT NULL,
    media_type VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    price NUMERIC NOT NULL CHECK (price >= 0),
    sale_price NUMERIC CHECK (sale_price IS NULL OR sale_price <= price),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product_attributes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    attribute_name VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(500) NOT NULL,
    CONSTRAINT uq_product_attributes_name UNIQUE (product_id, attribute_name)
);

CREATE TABLE product_inventories (
    product_id UUID PRIMARY KEY REFERENCES products(id),
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    low_stock_threshold INTEGER NOT NULL CHECK (low_stock_threshold >= 0),
    reserved_quantity INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id),
    product_id UUID NOT NULL REFERENCES products(id),
    seller_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status cart_item_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cart_items_cart_product UNIQUE (cart_id, product_id)
);

CREATE TABLE user_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    receiver_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    province_code VARCHAR(50) NOT NULL,
    district_code VARCHAR(50) NOT NULL,
    ward_code VARCHAR(50) NOT NULL,
    address_detail TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id UUID NOT NULL,
    total_amount NUMERIC NOT NULL CHECK (total_amount >= 0),
    final_amount NUMERIC NOT NULL CHECK (final_amount >= 0),
    payment_method payment_method NOT NULL,
    status order_status NOT NULL,
    payment_status payment_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id),
    payer_id UUID NOT NULL,
    amount NUMERIC NOT NULL CHECK (amount > 0),
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    payment_method payment_method NOT NULL,
    checkout_url_expired_at TIMESTAMP,
    status payment_status NOT NULL,
    payos_order_code VARCHAR(255),
    payos_checkout_url TEXT,
    payos_transaction_id VARCHAR(255),
    provider_response JSONB,
    idempotency_key VARCHAR(255) UNIQUE,
    paid_at TIMESTAMP,
    expired_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    seller_id UUID NOT NULL,
    carrier shipment_carrier NOT NULL,
    ghn_order_code VARCHAR(255) UNIQUE,
    ghn_shop_id VARCHAR(255),
    tracking_number VARCHAR(255) UNIQUE,
    shipping_fee NUMERIC NOT NULL CHECK (shipping_fee >= 0),
    shipping_fee_origin NUMERIC CHECK (shipping_fee_origin IS NULL OR shipping_fee_origin >= 0),
    estimated_delivery_date DATE,
    shipment_type shipment_type NOT NULL,
    weight_gram INTEGER CHECK (weight_gram IS NULL OR weight_gram > 0),
    cod_amount NUMERIC NOT NULL DEFAULT 0 CHECK (cod_amount >= 0),
    status shipment_status NOT NULL,
    external_provider_response JSONB,
    created_by_source shipment_created_by_source NOT NULL,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE shipping_address_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL UNIQUE REFERENCES shipments(id),
    receiver_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    province_code VARCHAR(50) NOT NULL,
    district_code VARCHAR(50) NOT NULL,
    ward_code VARCHAR(50) NOT NULL,
    address_detail TEXT NOT NULL,
    full_address TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    shipment_id UUID REFERENCES shipments(id),
    product_id UUID NOT NULL REFERENCES products(id),
    seller_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_snapshot NUMERIC NOT NULL CHECK (unit_price_snapshot >= 0),
    final_price NUMERIC NOT NULL CHECK (final_price >= 0),
    sku_snapshot VARCHAR(255),
    product_name_snapshot VARCHAR(500) NOT NULL,
    image_snapshot TEXT,
    attributes_snapshot JSONB,
    completed_at TIMESTAMP,
    shipping_fee_allocated NUMERIC NOT NULL CHECK (shipping_fee_allocated >= 0),
    shop_name_snapshot VARCHAR(255) NOT NULL,
    status order_item_status NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE ghn_webhook_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ghn_order_code VARCHAR(255) NOT NULL,
    status VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id UUID NOT NULL UNIQUE REFERENCES order_items(id),
    seller_id UUID NOT NULL,
    buyer_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status review_status NOT NULL DEFAULT 'VISIBLE'
);

CREATE TABLE review_replies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL UNIQUE REFERENCES reviews(id),
    seller_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE review_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL REFERENCES reviews(id),
    url TEXT NOT NULL,
    type VARCHAR(50) NOT NULL
);

CREATE TABLE payment_webhook_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(50) NOT NULL DEFAULT 'PAYOS',
    event_type VARCHAR(100) NOT NULL,
    payos_order_code VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    signature_valid BOOLEAN NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_payment_webhook_logs UNIQUE (provider, payos_order_code, event_type)
);

CREATE TABLE seller_payout_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    bank_account_name VARCHAR(255) NOT NULL,
    bank_account_number VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    old_status order_status,
    new_status order_status NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE payment_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(id),
    old_status payment_status,
    new_status payment_status NOT NULL,
    payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE shipment_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL REFERENCES shipments(id),
    old_status shipment_status,
    new_status shipment_status NOT NULL,
    raw_status VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(255) NOT NULL,
    event_key VARCHAR(255) NOT NULL UNIQUE,
    aggregate_id UUID NOT NULL,
    source VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status outbox_status NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP
);

-- =====================================================================
-- INDEXES
-- =====================================================================

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_seller ON order_items(seller_id);
CREATE INDEX idx_order_items_status ON order_items(status);
CREATE INDEX idx_order_items_shipment ON order_items(shipment_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_order_items_order_created ON order_items(order_id, created_at DESC);

CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_seller ON shipments(seller_id);
CREATE INDEX idx_shipments_seller_status ON shipments(seller_id, status);
CREATE UNIQUE INDEX idx_shipments_tracking ON shipments(tracking_number) WHERE tracking_number IS NOT NULL;
CREATE INDEX idx_shipments_ghn_code ON shipments(ghn_order_code);

CREATE INDEX idx_inventory_stock ON product_inventories(stock_quantity);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payos_order_code ON payments(payos_order_code);

CREATE INDEX idx_webhook_logs_processed ON payment_webhook_logs(processed, created_at);
CREATE INDEX idx_ghn_webhook_processed ON ghn_webhook_logs(processed, created_at);

CREATE INDEX idx_payout_accounts_seller ON seller_payout_accounts(seller_id);

CREATE INDEX idx_order_history ON order_status_history(order_id, created_at DESC);
CREATE INDEX idx_payment_history ON payment_status_history(payment_id, created_at DESC);
CREATE INDEX idx_shipment_history ON shipment_status_history(shipment_id, created_at DESC);

CREATE INDEX idx_outbox_pending ON outbox_events(status, created_at) WHERE status = 'PENDING';

CREATE INDEX idx_products_shop_status ON products(shop_id, status);
CREATE INDEX idx_products_category_status ON products(category_id, status);
CREATE INDEX idx_products_seller_status ON products(seller_id, status);

CREATE INDEX idx_product_prices_product_time ON product_prices(product_id, start_at DESC, end_at);

CREATE INDEX idx_cart_items_cart_status ON cart_items(cart_id, status);

CREATE INDEX idx_orders_buyer_created ON orders(buyer_id, created_at DESC);
CREATE INDEX idx_orders_status_created ON orders(status, created_at);

CREATE INDEX idx_reviews_seller_created ON reviews(seller_id, created_at DESC);
CREATE INDEX idx_reviews_order_item ON reviews(order_item_id);

CREATE UNIQUE INDEX idx_user_default_address ON user_addresses(user_id) WHERE is_default = TRUE;
