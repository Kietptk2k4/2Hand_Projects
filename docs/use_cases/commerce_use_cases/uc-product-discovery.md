# UC - Product Discovery

## 1. Overview

Use case nay mo ta cac nghiep vu buyer dung de kham pha san pham trong Commerce Service: xem danh sach san pham, xem chi tiet, search, filter theo category, xem san pham theo shop, xem gia active, ton kho kha dung, attributes va review. Product Discovery la read-only flow, nhung phai ap dung nghiem ngat visibility rule de khong lo product/shop khong hop le.

## 2. Actors

- **Buyer:** Xem, tim kiem va loc san pham.
- **Guest:** Co the xem public product neu API policy cho phep; MVP uu tien Buyer da dang nhap.
- **System:** Cung cap rating summary, inventory summary va product availability da sync.

## 3. Related Data

- `products`
- `product_categories`
- `product_media`
- `product_prices`
- `product_inventories`
- `product_attributes`
- `seller_shops`
- `shop_settings`
- `reviews`
- `review_media`
- `review_replies`

## 4. Business Rules

- Buyer discovery chi hien thi product co `products.status IN (ACTIVE, OUT_OF_STOCK)`.
- Product `DRAFT`, `PAUSED`, `ARCHIVED`, `REMOVED` khong duoc hien thi cho buyer.
- Shop phai co `seller_shops.status = ACTIVE`.
- Category phai `is_active = true`.
- Product `OUT_OF_STOCK` co the hien thi nhung phai duoc mark unavailable va khong checkout duoc.
- Active price la price co `start_at <= now` va (`end_at IS NULL OR end_at > now`).
- Neu `sale_price` ton tai thi `sale_price <= price`.
- Discovery khong lock inventory va khong reserve stock.
- Checkout phai revalidate product/shop/price/stock, khong tin tuyet doi vao discovery response.

## 5. Sub-Use Cases

### 5.1. View Product List

**Goal:** Buyer xem danh sach product hop le de mua.

**Preconditions:**

- Product data ton tai trong Commerce Service.
- Product/shop/category thoa visibility rule.

**Main Flow:**

1. Buyer gui request xem product list voi pagination va optional sort/filter.
2. System validate pagination params.
3. System query `products` theo visibility rule.
4. System load media chinh, active price, inventory summary, shop summary va rating summary.
5. System tra ve product cards va pagination metadata.

**Exception Flow:**

- Pagination invalid -> 400.
- DB timeout -> 500.

**Postconditions:**

- Khong thay doi database.
- Buyer nhan danh sach product buyer-visible.

### 5.2. View Product Detail

**Goal:** Buyer xem day du thong tin san pham truoc khi add cart/checkout.

**Preconditions:**

- Product ton tai.
- Product buyer-visible hoac duoc phep xem theo policy.

**Main Flow:**

1. Buyer request product detail bang `product_id` hoac slug.
2. System load product, shop va category.
3. System apply visibility rule.
4. System load media, active price, inventory, attributes va review page.
5. System tra detail response.

**Exception Flow:**

- Product not found -> 404.
- Product ton tai nhung khong buyer-visible -> 404.
- Active price missing -> tra unavailable state hoac 409 theo API contract.

**Postconditions:**

- Khong thay doi database.

### 5.3. Search Product

**Goal:** Buyer tim san pham theo keyword.

**Preconditions:**

- Keyword hop le.

**Main Flow:**

1. Buyer nhap keyword.
2. System normalize keyword.
3. System reject keyword rong/qua ngan.
4. System search theo `title`, `description`, optional category/shop context.
5. System apply visibility rule.
6. System enrich ket qua voi price/media/inventory/rating.

**Exception Flow:**

- Keyword invalid -> 400.
- Search query timeout -> 500.

**Postconditions:**

- Khong thay doi product/order/cart.

### 5.4. Filter Products By Category

**Goal:** Buyer xem san pham trong category hoac category subtree.

**Preconditions:**

- Category id/slug hop le.
- Category active.

**Main Flow:**

1. Buyer chon category.
2. System load category.
3. System check `is_active = true`.
4. System lay descendant category ids bang `path` hoac recursive relation.
5. System query visible products trong category subtree.
6. System tra product page.

**Exception Flow:**

- Category not found -> 404.
- Category inactive -> empty result hoac 404 theo API policy.

**Postconditions:**

- Khong thay doi database.

### 5.5. View Products By Shop

**Goal:** Buyer xem product public cua mot shop.

**Preconditions:**

- Shop ton tai va `ACTIVE`.

**Main Flow:**

1. Buyer mo shop page.
2. System load shop.
3. System check shop public visibility.
4. System query products cua shop theo visibility rule.
5. System include vacation mode/message neu co.
6. System tra shop product page.

**Exception Flow:**

- Shop not found -> 404.
- Shop `SUSPENDED/CLOSED` -> 404 hoac unavailable response.

**Postconditions:**

- Khong thay doi database.

## 6. Response Guidance

Product card nen co:

- `product_id`
- `title`
- `thumbnail_url`
- `shop_id`
- `shop_name`
- `category_id`
- `condition`
- `status`
- `price`
- `sale_price`
- `effective_price`
- `in_stock`
- `low_stock`
- `rating_avg`
- `rating_count`

Product detail nen co them:

- `description`
- `weight_gram`
- `media`
- `attributes`
- `inventory_summary`
- `reviews`
- `shop_vacation`
- `vacation_message`

## 7. Security

- Public discovery co the cho guest neu business cho phep.
- Protected discovery can require JWT.
- Khong tra ve internal fields: `reserved_quantity`, provider response, moderation details.

## 8. Acceptance Criteria

- Buyer khong thay product `DRAFT`, `PAUSED`, `ARCHIVED`, `REMOVED`.
- Product cua shop `SUSPENDED/CLOSED` khong hien thi.
- Listing/detail co active price va inventory summary.
- Search va category filter ton trong visibility rule.
- Discovery la read-only va khong reserve stock.

