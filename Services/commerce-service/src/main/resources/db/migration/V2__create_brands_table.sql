-- =====================================================================
-- COMMERCE SERVICE - BRANDS TABLE (MVP vertical: fashion second-hand)
-- PostgreSQL
-- Ref: docs/database/commerce-catalog-seed.md
-- =====================================================================

CREATE TABLE brands (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_brands_active_slug ON brands(is_active, slug);

ALTER TABLE products
    ADD CONSTRAINT fk_products_brand
        FOREIGN KEY (brand_id) REFERENCES brands(id);
