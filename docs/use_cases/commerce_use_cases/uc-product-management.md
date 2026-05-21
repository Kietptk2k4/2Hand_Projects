# UC - Product Management

## 1. Overview

Use case nay mo ta nghiep vu seller quan ly product catalog: tao draft, cap nhat thong tin, media, price, attributes, publish, pause, resume, archive va admin remove. Product Management la seller-facing; buyer-facing discovery nam o `uc-product-discovery.md`.

## 2. Actors

- **Seller:** Quan ly product cua shop minh.
- **Admin:** Remove product vi pham.
- **System:** Sync out-of-stock/cart invalidation.

## 3. Related Data

- `products`
- `product_categories`
- `product_media`
- `product_prices`
- `product_attributes`
- `product_inventories`
- `seller_shops`
- `cart_items`
- `outbox_events`

## 4. Business Rules

- Product belongs to one seller shop.
- Seller chi quan ly product cua shop minh.
- Product starts as `DRAFT`.
- Product can publish only if shop `ACTIVE`, category active, active price exists, inventory exists, required fields valid.
- `REMOVED` product cannot be republished by seller.
- `ARCHIVED` is seller soft delete.
- Checkout always revalidates product status live.

## 5. Sub-Use Cases

### 5.1. Create Product Draft

**Main Flow:**

1. Seller submit product core fields.
2. System validate seller shop exists and active.
3. System insert product with `status = DRAFT`.
4. System optionally create initial inventory/attributes/media.
5. System writes event if needed.

**Exception Flow:** Seller has no shop -> 409; shop suspended -> 403/409.

### 5.2. Update Product

**Main Flow:**

1. Seller updates title, description, category, condition, weight, brand/product type.
2. System validates ownership.
3. System validates category and fields.
4. System updates product.

**Exception Flow:** Product `REMOVED` -> 409; seller not owner -> 403/404.

### 5.3. Manage Product Media

**Object Storage (MinIO):** Bucket `2hands-commerce-product` (MinIO shared). MVP: FE presigned upload → MinIO → API luu `product_media.media_url`. Chi tiet: `docs/engineering_rules/commerce-object-storage.md`.

**Main Flow:**

1. Seller uploads file len MinIO (presigned) hoac gui `media_url` da upload.
2. System validates product ownership va URL bucket/domain.
3. System inserts/updates `product_media` (URL + `media_type` + `sort_order`).
4. System orders media by `sort_order`.

**Exception Flow:** Invalid media type -> 400; invalid URL -> 400; MinIO/storage failure -> 503; DB fail after upload -> cleanup orphan object khi co the.

### 5.4. Manage Product Price

**Main Flow:**

1. Seller submits price/sale price and effective time.
2. System validates `price >= 0`, `sale_price <= price`.
3. System closes previous active price if needed.
4. System inserts new `product_prices`.

**Postconditions:** Future cart/checkout uses new active price; existing order snapshots unchanged.

### 5.5. Manage Product Attributes

**Main Flow:**

1. Seller submits attribute name/value list.
2. System validates ownership.
3. System upserts attributes by unique `(product_id, attribute_name)`.

### 5.6. Publish Product

**Preconditions:** Product draft/paused/out-of-stock owned by seller.

**Main Flow:**

1. Seller requests publish.
2. System validates shop active, category active, required fields, price, inventory, media policy.
3. If stock > 0, set `ACTIVE`.
4. If stock = 0, set `OUT_OF_STOCK`.
5. System writes outbox event.

**Exception Flow:** Missing price/inventory/required fields -> 409.

### 5.7. Pause, Resume, Archive Product

**Main Flow:**

1. Seller requests status action.
2. System validates ownership and current status.
3. System updates status:
   - pause: `ACTIVE/OUT_OF_STOCK -> PAUSED`
   - resume: `PAUSED -> ACTIVE/OUT_OF_STOCK`
   - archive: seller soft delete to `ARCHIVED`
4. System writes event and cart sync can mark invalid items.

### 5.8. Admin Remove Product

**Main Flow:**

1. Admin requests remove product with reason.
2. System checks permission.
3. System sets `status = REMOVED`.
4. System marks cart items invalid or emits event for sync.

## 6. Acceptance Criteria

- Seller can manage only own products.
- Product cannot publish without required readiness.
- `PAUSED`, `ARCHIVED`, `REMOVED` products are not buyer-visible and cannot checkout.
- Product price changes do not alter existing order snapshots.
- Admin removed product cannot be restored by seller.

