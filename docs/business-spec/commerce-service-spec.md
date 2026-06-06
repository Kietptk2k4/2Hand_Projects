# Commerce Service Business Specification (MVP)

Commerce Service la service so huu toan bo nghiep vu mua ban trong 2Hands: shop, product, inventory, cart, checkout, order, payment, shipment va review. Tai lieu nay la source-of-truth de AI/engineer co the doc va tu hieu domain, logic trang thai, boundary va cac rule quan trong truoc khi code.

**MVP product vertical:** thoi trang second-hand C2C (closet marketplace). Doc truoc khi implement catalog/seller/FE mock:

- `docs/product-vision/fashion-secondhand-vertical.md` — quyet dinh business
- `docs/database/commerce-catalog-seed.md` — UUID category/brand seed

## 1. Service Ownership

Commerce Service own cac aggregate va data sau:

- Seller shop va shop settings.
- Product catalog: brand, category, product, media, price, attributes.
- Inventory va stock reservation.
- Cart va cart item.
- Buyer address book va shipping address snapshot.
- Order, order item va order status history.
- Payment, payment webhook log va payment status history.
- Shipment, GHN webhook log va shipment status history.
- Review, review media va seller reply.
- Commerce outbox events.

Commerce Service khong duoc truy cap truc tiep database cua service khac. Neu can thong tin user/profile/permission thi lay tu JWT claim, local projection, internal API hoac event integration theo contract rieng.

## 1.1 Object Storage (MinIO)

Commerce media (product, shop, review) dung **MinIO shared** — khong tao instance MinIO rieng cho `commerce-service`. Chi tiet: `docs/engineering_rules/commerce-object-storage.md`.

### Infrastructure

- MinIO trong `Infrastructure/docker-compose.yml` (API `:9000`, Console `:9001`).
- Production: endpoint S3-compatible, cung mo hinh URL.

### Commerce buckets (MVP)

| Bucket | Entity |
|--------|--------|
| `2hands-commerce-product` | `product_media` |
| `2hands-commerce-review` | `review_media` |
| `2hands-commerce-shop` | `seller_shops.avatar_url`, `cover_url` |

Auth avatar dung bucket riêng `2hands-avatar` — khong gop voi commerce.

### Persistence

- PostgreSQL chi luu URL + metadata; file binary tren MinIO.
- Checkout: `order_items.image_snapshot` copy URL media chinh tu `product_media` tai thoi diem mua.

### Upload flows

1. **Luong chinh (uu tien MVP, giong Auth):** FE lay presigned URL (Commerce API hoac gateway) → upload truc tiep MinIO → goi Commerce API voi `media_url` / `avatar_url` / `cover_url` → validate URL + ghi DB.
2. **Luong phu:** `FR_UploadReviewMedia` — multipart qua Commerce, proxy len bucket `2hands-commerce-review`, insert `review_media`.

### Rules

- Validate URL thuoc bucket/domain cho phep; gioi han type/size/count.
- Khong log presigned secret.
- Storage OK + DB fail → cleanup orphan object khi co the.
- Out of scope: quan ly bucket policy IaC chi tiet; khong doi schema Postgres (giu cot URL hien tai).

## 1.2 MVP Vertical - Fashion Second-hand (tom tat)

Kien truc Commerce van generic multi-vendor. Vertical duoc cau hinh bang data + rule:

| Domain | Quy uoc MVP |
|--------|-------------|
| Category | Chi cay **Thoi trang** (seed V3); seller chon leaf category khi listing |
| Brand | Bang `brands` (V2); `products.brand_id`; fallback **Khac** |
| Condition | `LIKE_NEW`, `GOOD`, `FAIR`, `USED` — khong `NEW` mac dinh |
| Product type | `PHYSICAL` |
| Inventory | `stock_quantity` 0 hoac 1 (1 listing = 1 mon unique) |
| Attributes | Chuan hoa: `size`, `color`, `gender`, `material`, `defects`, ... |
| Discovery | Product list/search/category + Social tag san pham |

Chi tiet day du: `docs/product-vision/fashion-secondhand-vertical.md`.

## 2. Actors

### Buyer

Nguoi mua san pham. Buyer co the:

- Xem/search/filter product.
- Quan ly cart.
- Quan ly dia chi giao hang.
- Checkout va tao order.
- Thanh toan COD hoac payOS.
- Theo doi order/shipment/payment.
- Xac nhan da nhan hang.
- Review san pham sau khi order item completed.

### Seller

Chu closet / shop nho (1 shop/user). Seller co the:

- Tao/cap nhat shop.
- Quan ly vacation mode.
- Tao/cap nhat/publish/pause/archive product.
- Cap nhat gia, ton kho, thuoc tinh, media.
- Xem va xu ly order item cua shop minh.
- Tao/cap nhat shipment cho order cua shop minh.
- Phan hoi review.

### Admin

Admin/Moderator co quyen tu Auth Service. Admin co the:

- Remove product vi pham.
- Suspend/close shop.
- Hide/remove review.
- Kiem tra shipment/order support trong MVP.

### System

Background worker/job trong Commerce Service:

- Sync cart item voi product/inventory.
- Expire unpaid payment/order.
- Xu ly payOS webhook.
- Xu ly GHN webhook.
- Auto complete delivered order sau timeout.
- Retry outbox events.
- Dong bo shipment tracking neu co polling.

## 3. Core Domain Concepts

### Product

Product la mat hang seller dang ban. Product co lifecycle rieng:

```text
DRAFT -> ACTIVE -> OUT_OF_STOCK
ACTIVE -> PAUSED
ACTIVE/PAUSED/OUT_OF_STOCK -> ARCHIVED
ACTIVE/PAUSED/OUT_OF_STOCK -> REMOVED
PAUSED -> ACTIVE
OUT_OF_STOCK -> ACTIVE
```

Meaning:

- `DRAFT`: seller tao nhung chua publish.
- `ACTIVE`: dang ban va co the them vao cart/checkout neu stock hop le.
- `OUT_OF_STOCK`: het hang, khong checkout duoc.
- `PAUSED`: seller tam dung ban.
- `ARCHIVED`: seller ngung ban, soft delete theo nghiep vu.
- `REMOVED`: admin/system remove do vi pham.

**Vertical fields (fashion second-hand):**

- `product_type`: `PHYSICAL` (MVP).
- `condition`: `LIKE_NEW` | `GOOD` | `FAIR` | `USED` (enforce o create/update/publish).
- `brand_id`: optional FK -> `brands` (sau migration V2).
- `category_id`: leaf category trong cay thoi trang (seed V3).
- `product_attributes`: dictionary chuan — xem vertical doc (`size`, `color`, `defects`, ...).

### Inventory

Inventory duoc quan ly theo product. MVP can phan biet:

- `stock_quantity`: so luong co the ban/con lai trong kho.
- `reserved_quantity`: so luong dang bi giu trong checkout/order chua finalize.
- `low_stock_threshold`: nguong canh bao het hang.

Reserve inventory la invariant quan trong cua checkout:

```text
Checkout:
stock_quantity -= quantity
reserved_quantity += quantity

Payment success:
reserved_quantity -= quantity

Payment failed/expired/cancelled:
reserved_quantity -= quantity
stock_quantity += quantity
```

Neu dung ten business `available_stock`, no tuong ung voi `stock_quantity` trong schema MVP.

**Vertical second-hand:** moi listing thuong co `stock_quantity` in {0, 1}. Publish mac dinh `stock_quantity = 1`, `low_stock_threshold = 0`. Khong ban so luong > 1 cho cung listing (application validation).

### Cart

Moi user co mot cart. Cart item chi la y dinh mua, khong phai reservation. Cart item co status:

- `ACTIVE`: item hop le.
- `OUT_OF_STOCK`: product khong du ton kho.
- `REMOVED`: user xoa khoi cart.
- `INVALID_PRODUCT`: product da bi disable, pause, archive, remove hoac khong con hop le.

Cart item unique theo `(cart_id, product_id)` de tranh duplicate product trong cung cart.

### Order

Order dai dien cho giao dich mua hang cua buyer. Order co the gom nhieu seller; order item gan voi seller va co the duoc gom shipment theo seller.

Order status:

```text
CREATED -> AWAITING_PAYMENT -> PROCESSING -> COMPLETED
CREATED/AWAITING_PAYMENT -> CANCELLED
PROCESSING -> CANCELLED only when all shipments still PENDING and business explicitly allows
```

Meaning:

- `CREATED`: order vua tao, chua bat dau payment flow ro rang.
- `AWAITING_PAYMENT`: doi thanh toan, ap dung cho payOS hoac COD chua vao processing.
- `PROCESSING`: da du dieu kien xu ly hang/shipment.
- `COMPLETED`: order hoan tat.
- `CANCELLED`: order bi huy.

Order chi duoc `COMPLETED` khi:

- Tat ca `order_items.status = COMPLETED`.
- `orders.payment_status = PAID`.

### Order Item

Order item la don vi xu ly fulfillment theo seller/product. Status:

```text
PENDING -> PROCESSING -> SHIPPED -> DELIVERED -> COMPLETED
PENDING/PROCESSING -> CANCELLED
PROCESSING/SHIPPED/DELIVERED -> FAILED/RETURNED when delivery problem happens
```

Meaning:

- `PENDING`: vua tao, chua seller xu ly.
- `PROCESSING`: seller dang chuan bi hang.
- `SHIPPED`: da giao cho van chuyen/dang giao.
- `DELIVERED`: carrier bao da giao, buyer chua confirm.
- `COMPLETED`: buyer confirm hoac system auto complete sau thoi gian cho.
- `CANCELLED`: item bi huy truoc fulfillment.
- `FAILED`: giao that bai.
- `RETURNED`: hang bi tra ve.

Review chi duoc tao khi order item da `COMPLETED`.

### Payment

Payment nam trong Commerce Service, thay the Payment Service rieng trong MVP. Moi order co mot payment.

Payment method:

- `COD`: tien duoc thu khi giao hang. Khi buyer confirm da nhan hang, `payment_status = PAID`.
- `PAYOS`: tao payment link, nhan redirect/webhook. Khi webhook success hop le, `payment_status = PAID`.

Payment status:

```text
PENDING -> PAID
PENDING -> FAILED
PENDING -> CANCELLED
PENDING -> EXPIRED
```

Order payment status:

- Order created: `payment_status = PENDING`.
- payOS webhook success: `payment_status = PAID`.
- COD shipment delivered + buyer confirm: `payment_status = PAID`.
- Payment failed/cancelled/expired: `payment_status = FAILED/CANCELLED` va co the cancel order neu order con du dieu kien.

COD rule:

- Shipment COD amount = order final amount.
- `payments.payment_method = COD`.
- `payments.status` co the pending den khi buyer confirm/auto settlement theo rule MVP.

payOS rule:

- `payments.payment_method = PAYOS`.
- `shipments.cod_amount = 0`.
- Payment link co `checkout_url_expired_at`/`expired_at`.
- Webhook phai verify signature truoc khi update status.
- Webhook log phai luu day du payload de debug.

### Shipment

Shipment dai dien cho qua trinh giao hang. Shipment chi duoc tao khi order da san sang xu ly fulfillment.

Rule quan trong:

- Khong tao shipment neu order chua `PROCESSING`.
- Seller tao shipment sau khi order da `PAID` voi payOS.
- Với COD, order co the vao `PROCESSING` va shipment co `cod_amount = order.final_amount`.
- Mot order co the co nhieu shipment theo seller.
- Shipment co 1 shipping address snapshot.
- Shipment `DELIVERED` khong tu dong complete order ngay; can buyer confirm hoac background job sau 7 ngay.

Shipment status:

```text
PENDING -> PICKING_UP -> READY_TO_SHIP -> SHIPPED -> DELIVERED
PENDING/PICKING_UP/READY_TO_SHIP/SHIPPED -> FAILED
PENDING -> CANCELLED
SHIPPED/DELIVERED -> RETURNED
```

Carrier:

- `GHN`: tich hop GHN API/webhook.
- `MANUAL`: seller/admin cap nhat tracking thu cong.
- `SELF_DELIVERY`: seller tu giao.

GHN mapping MVP:

- GHN `picking` -> `PICKING_UP`.
- GHN delivering status -> `SHIPPED`.
- GHN delivered -> `DELIVERED`.
- GHN failure/return statuses -> `FAILED` hoac `RETURNED` tuy raw status.

### Review

Review gan voi `order_item_id`, buyer va seller. Mot order item chi co toi da mot review.

Rule:

- `UNIQUE(order_item_id)`.
- Buyer chi review order item thuoc order cua minh.
- Chi review khi `order_items.status = COMPLETED`.
- Rating tu 1 den 5.
- Review status:
  - `VISIBLE`: hien thi binh thuong.
  - `HIDDEN`: bi admin/seller policy an.

Seller co the reply review mot lan trong MVP.

## 4. Buyer Business Flows

### 4.1 Product Discovery

Business goal: Cho buyer tim va danh gia san pham truoc khi mua.

Main flows:

1. Buyer xem danh sach san pham.
2. He thong chi tra ve product `ACTIVE`, shop `ACTIVE`, shop khong vacation-block neu policy yeu cau.
3. Buyer co the search theo keyword, filter category, xem theo shop.
4. He thong tra ve gia active, media chinh, ton kho kha dung, rating/review summary.
5. Buyer xem detail de thay description, attributes, price, inventory va review.

Business rules:

- Khong hien thi product `DRAFT`, `PAUSED`, `ARCHIVED`, `REMOVED`.
- Product `OUT_OF_STOCK` co the hien thi trong discovery nhung khong checkout duoc.
- Gia hien thi la active price theo `product_prices.start_at/end_at`.
- Neu co `sale_price`, `sale_price <= price`.

Failure cases:

- Category khong ton tai.
- Shop bi suspend/closed.
- Product bi remove giua luc buyer xem va checkout.

### 4.2 Cart Management

Business goal: Cho buyer gom san pham truoc checkout.

Main flows:

1. Buyer them product vao cart.
2. He thong tao cart neu user chua co cart.
3. He thong validate product `ACTIVE`, shop hop le, quantity > 0.
4. Neu item da ton tai, cap nhat quantity thay vi tao duplicate.
5. Buyer xem cart, he thong re-check status item, gia va stock.

Business rules:

- Cart khong reserve stock.
- Quantity phai > 0.
- Khong them product `DRAFT/PAUSED/ARCHIVED/REMOVED`.
- Neu stock khong du, item thanh `OUT_OF_STOCK`.
- Neu product invalid, item thanh `INVALID_PRODUCT`.
- Xoa item la soft state `REMOVED` trong MVP de audit UX, khong bat buoc hard delete.

Events:

- MVP khong bat buoc publish cart event.

### 4.3 Address Management

Business goal: Quan ly dia chi giao hang cua buyer.

Main flows:

1. Buyer them dia chi voi receiver name, phone, province/district/ward, address detail.
2. Buyer cap nhat/xoa dia chi.
3. Buyer chon default address.
4. Checkout dung address hien tai de tao shipping snapshot.

Business rules:

- Moi user co the co nhieu address.
- Chi mot address default moi user.
- Order/shipment khong tham chieu mutable address truc tiep; phai dung `shipping_address_snapshots`.
- Xoa/cap nhat address sau checkout khong lam thay doi dia chi cua shipment da tao.

### 4.4 Checkout & Order

Business goal: Chuyen cart hop le thanh order co payment va shipment plan.

Main flow:

1. Buyer chon cart items de checkout.
2. Buyer chon shipping address.
3. Buyer chon payment method `COD` hoac `PAYOS`.
4. He thong validate cart item, product, shop, price va stock.
5. He thong tinh total amount, shipping fee, final amount.
6. He thong reserve inventory trong transaction.
7. He thong tao order, order items, payment.
8. He thong tao shipment theo seller neu business flow can tao som, hoac tao shipment sau khi order `PROCESSING`.
9. Neu payOS, he thong tao checkout URL va tra ve cho client redirect.
10. Neu COD, order vao flow xu ly COD.

Transaction boundary:

- Checkout la write use case, bat buoc nam o application layer va co transaction.
- Reserve inventory, tao order, order items, payment va outbox event phai atomic trong cung transaction neu cung DB.
- External call payOS/GHN khong nen nam trong DB transaction dai. Nen tao local state truoc, commit, sau do call provider hoac dung saga/outbox worker.

Business rules:

- Checkout fail neu product khong active, shop khong active, stock khong du.
- Snapshot phai luu product name, SKU, image, attributes, shop name va price tai thoi diem mua.
- `total_amount >= 0`, `final_amount >= 0`.
- Multi-seller order phai co order items tach seller ro rang.
- Shipping fee can allocate ve `order_items.shipping_fee_allocated` de tinh seller/order detail.

Cancel rule:

Allowed:

- `orders.status in (CREATED, AWAITING_PAYMENT)`.
- Shipment chua tao hoac shipment `PENDING`.

Not allowed:

- Shipment `PICKING_UP`, `SHIPPED`, `DELIVERED`.

When cancel:

- Release reserved inventory neu payment chua success.
- Update order/payment/order item status.
- Ghi status history.
- Publish event qua outbox neu can.

### 4.5 Buyer Payment

Business goal: Thu tien theo COD hoac payOS va dong bo payment status chinh xac.

payOS main flow:

1. Buyer checkout voi `PAYOS`.
2. He thong tao payment record `PENDING`.
3. He thong goi payOS create payment link.
4. Luu `payos_order_code`, `payos_checkout_url`, `checkout_url_expired_at`, provider response.
5. Client redirect buyer den checkout URL.
6. payOS redirect ve success/cancel page.
7. payOS webhook gui event.
8. He thong verify signature, log webhook, update payment/order status.
9. Payment success thi release reserved quantity va order vao `PROCESSING`.

COD main flow:

1. Buyer checkout voi `COD`.
2. He thong tao payment `PENDING`.
3. Shipment co `cod_amount = order.final_amount`.
4. Khi shipment delivered, buyer confirm da nhan hang.
5. He thong set payment/order payment status `PAID`.
6. Order item chuyen `COMPLETED`, order completed neu tat ca item completed.

Failure handling:

- payOS failed/cancelled/expired: release inventory, cancel order neu du dieu kien.
- Webhook duplicate: xu ly idempotent theo unique `(provider, payos_order_code, event_type)` va payment current status.
- Signature invalid: log `signature_valid = false`, khong update payment state.

### 4.6 Buyer Shipping Tracking

Business goal: Cho buyer theo doi giao hang.

Main flows:

1. Buyer xem shipment cua order.
2. He thong tra tracking number, carrier, status, estimated delivery date.
3. Buyer xem shipping address snapshot.
4. GHN webhook/system sync cap nhat shipment status.
5. Khi shipment `DELIVERED`, buyer co the confirm received.

Business rules:

- Buyer chi xem shipment cua order minh.
- Shipment delivered khong auto complete order ngay.
- Auto complete sau 7 ngay neu buyer khong dispute/confirm, trong MVP co the background job chuyen `DELIVERED -> COMPLETED`.

### 4.7 Buyer Review

Business goal: Cho buyer danh gia san pham sau mua.

Main flow:

1. Buyer chon order item da `COMPLETED`.
2. He thong validate buyer la owner cua order.
3. Buyer tao review voi rating/comment/media.
4. He thong tao review status `VISIBLE`.
5. Cap nhat rating summary cua seller/product/shop neu co denormalized counter.

Business rules:

- Khong review neu order item chua `COMPLETED`.
- `buyer_id` phai match `orders.buyer_id`.
- Mot order item chi co mot review.
- Rating 1..5.

## 5. Seller Business Flows

### 5.1 Shop Management

Business goal: Cho user mo va quan ly shop.

Main flows:

1. Seller tao shop.
2. Seller cap nhat shop name, description, avatar, cover.
3. Seller bat/tat vacation mode.
4. Seller cap nhat vacation message.

Business rules:

- Moi user toi da 1 shop trong MVP.
- Shop status:
  - `ACTIVE`: hoat dong.
  - `CLOSED`: shop dong.
  - `SUSPENDED`: bi admin suspend.
- Shop `SUSPENDED/CLOSED` khong duoc publish product moi va khong checkout product.
- Vacation mode co the van cho xem product nhung checkout co the bi chan tuy policy MVP; mac dinh nen chan checkout de tranh don moi.

### 5.2 Product Management

Business goal: Seller quan ly catalog cua shop minh.

Main flows:

1. Seller tao product draft.
2. Seller cap nhat title, description, category, condition, weight.
3. Seller upload media, attributes.
4. Seller cap nhat price va inventory.
5. Seller publish product khi du dieu kien.
6. Seller pause/archive product.

Publish preconditions:

- Shop `ACTIVE`.
- Product co category active.
- Product co title, description, weight.
- Product co price hop le.
- Product co inventory record.
- Product co it nhat 1 media neu UI yeu cau.

Business rules:

- Seller chi sua product cua shop minh.
- Archive la soft delete cho seller.
- Admin remove chuyen product sang `REMOVED`.
- Product price history khong nen overwrite gia cu neu can audit; tao record price moi voi effective window.

### 5.3 Inventory Management

Business goal: Dam bao ton kho dung khi checkout va fulfillment.

Main flows:

1. Seller cap nhat stock quantity.
2. System kiem tra low stock.
3. System danh dau product `OUT_OF_STOCK` khi stock ve 0.
4. Checkout reserve stock.
5. Payment fail/expire release stock.

Business rules:

- `stock_quantity >= 0`.
- `reserved_quantity >= 0`.
- Khong cho checkout neu `stock_quantity < quantity`.
- Cap nhat stock can dung optimistic/pessimistic lock de tranh oversell.

### 5.4 Seller Order Management

Business goal: Seller xu ly phan order thuoc shop minh.

Main flows:

1. Seller xem order/order items theo seller.
2. Seller xac nhan chuan bi hang.
3. Order item chuyen `PROCESSING`.
4. Seller tao shipment cho nhom order items cua minh.
5. Seller nhap weight/package info.
6. Neu GHN, he thong goi GHN create order va luu `ghn_order_code`.
7. Seller theo doi shipment.

Business rules:

- Seller chi xem va thao tac order items cua shop minh.
- Khong tao shipment neu order chua `PROCESSING`.
- Shipment phai gan dung `seller_id`.
- Order item khi gan shipment thi `shipment_id` khong null.

### 5.5 Seller Review Management

Business goal: Seller theo doi va phan hoi review.

Main flows:

1. Seller xem review cua shop.
2. Seller reply review.
3. He thong cap nhat rating average/count.

Business rules:

- Seller chi reply review thuoc shop minh.
- Mot review co toi da mot reply trong MVP.
- Seller khong duoc sua rating/comment cua buyer.

## 6. Admin Business Flows

### 6.1 Product Moderation

Admin co the remove product vi pham. Product chuyen `REMOVED`, khong hien thi discovery, khong add cart, khong checkout. Cart items lien quan se duoc background job chuyen `INVALID_PRODUCT`.

### 6.2 Shop Moderation

Admin co the:

- `SUSPENDED`: chan shop ban hang, publish product, checkout.
- `CLOSED`: dong shop theo policy.

System can update product/cart availability sau khi shop bi suspend/closed.

### 6.3 Review Moderation

Admin co the hide/remove review. MVP nen dung `HIDDEN` de soft moderation, tranh xoa vat ly.

## 7. System Jobs

### 7.1 Inventory & Cart Sync

Job chay dinh ky hoac event-driven:

- Cap nhat cart item `OUT_OF_STOCK` khi product stock khong du.
- Cap nhat cart item `INVALID_PRODUCT` khi product/shop invalid.
- Cleanup cart item `REMOVED` cu neu can.
- Danh dau product `OUT_OF_STOCK` khi stock ve 0.

### 7.2 Payment Expiration

Job tim payment `PENDING` da qua `expired_at`:

1. Mark payment `EXPIRED` hoac `CANCELLED` theo policy.
2. Mark order `CANCELLED` neu order con `CREATED/AWAITING_PAYMENT`.
3. Release reserved inventory.
4. Ghi payment/order status history.
5. Publish event qua outbox.

### 7.3 Order Lifecycle

Job:

- Auto cancel unpaid order.
- Auto complete delivered order sau 7 ngay.
- Recompute order status tu order item status khi can.

Order completed iff:

- All order items `COMPLETED`.
- Payment status `PAID`.

### 7.4 Payment Webhook Processing

Webhook endpoint chi nen:

1. Verify signature.
2. Ghi `payment_webhook_logs`.
3. Idempotently process event hoac enqueue job.
4. Update payment/order/inventory.

### 7.5 Shipment Webhook Processing

GHN webhook endpoint:

1. Ghi `ghn_webhook_logs`.
2. Map raw GHN status sang shipment status.
3. Update shipment status/history.
4. Update order item status tu shipment status.
5. Neu delivered, cho buyer confirm hoac auto complete later.

### 7.6 Outbox Publishing

Tat ca event publish ra broker phai qua `outbox_events`.

State:

```text
PENDING -> PROCESSING -> PUBLISHED
PENDING/PROCESSING -> FAILED
FAILED -> PROCESSING -> PUBLISHED
```

Worker phai retry co gioi han, luu `last_error`, va xu ly idempotency.

## 8. Events

MVP event candidates:

- `COMMERCE_ORDER_CREATED`
- `COMMERCE_ORDER_CANCELLED`
- `COMMERCE_ORDER_COMPLETED`
- `COMMERCE_PAYMENT_CREATED`
- `COMMERCE_PAYMENT_PAID`
- `COMMERCE_PAYMENT_FAILED`
- `COMMERCE_PAYMENT_EXPIRED`
- `COMMERCE_SHIPMENT_CREATED`
- `COMMERCE_SHIPMENT_STATUS_CHANGED`
- `COMMERCE_INVENTORY_RESERVED`
- `COMMERCE_INVENTORY_RELEASED`
- `COMMERCE_PRODUCT_PUBLISHED`
- `COMMERCE_PRODUCT_REMOVED`
- `COMMERCE_REVIEW_CREATED`

Event payload toi thieu:

- `event_id`
- `event_type`
- `event_key`
- `aggregate_id`
- `occurred_at`
- `source = commerce`
- domain payload can thiet

## 9. Security And Authorization

Protected APIs bat buoc JWT authentication.

Authorization rules:

- Buyer chi xem/sua cart, address, order, payment, shipment cua minh.
- Buyer chi review order item cua order minh.
- Seller chi quan ly shop/product/order item/shipment/review reply cua shop minh.
- Admin action can role/permission tu Auth Service.
- Khong log token, secret, webhook signature secret, provider credential, MinIO presigned secret.

## 10. Transaction And Consistency Rules

Write use case bat buoc transaction o application layer:

- Checkout/create order.
- Cancel order.
- Payment status update.
- Shipment status update.
- Inventory update/reserve/release.
- Product publish/update.
- Review create/update.

External provider calls:

- payOS/GHN call can idempotency key.
- Khong giu DB transaction trong luc doi external API neu co the tranh.
- Provider response phai luu de debug va reconciliation.

Consistency:

- Inventory reservation phai atomic.
- Webhook processing phai idempotent.
- Status history phai ghi khi doi state quan trong.
- Outbox event phai ghi trong cung transaction voi domain change.

## 11. Data Snapshots

Order item phai snapshot:

- `unit_price_snapshot`
- `final_price`
- `sku_snapshot`
- `product_name_snapshot`
- `image_snapshot`
- `attributes_snapshot`
- `shop_name_snapshot`
- `shipping_fee_allocated`

Shipment phai co shipping address snapshot:

- receiver name
- phone
- province/district/ward code
- address detail
- full address

Snapshot giup order lich su khong bi thay doi khi product/shop/address sau nay thay doi.

## 12. Important Invariants

- Cart item khong reserve stock.
- Checkout moi reserve stock.
- Payment success khong tru stock lan nua; stock da bi tru khi reserve.
- Payment fail/expire/cancel phai release stock.
- Shipment khong duoc tao neu order chua du dieu kien processing.
- Shipment delivered khong auto complete order ngay.
- Order completed can all order items completed va payment paid.
- Review chi duoc tao sau order item completed.
- Moi order co mot payment.
- Moi shipment co mot shipping address snapshot.
- Moi user co toi da mot cart.
- Moi user co toi da mot seller shop trong MVP.
- Khong service nao doc/ghi truc tiep DB cua service khac.
- Fashion vertical: condition chi nhan 4 gia tri; inventory listing second-hand chi 0 hoac 1.

## 13. MVP Out Of Scope

- Refund/dispute day du.
- Seller payout thuc te.
- Advanced promotion/voucher.
- Multi-warehouse inventory.
- Realtime search engine rieng.
- Complex return/refund workflow.
- Full reconciliation voi GHN/payOS ngoai cac log/status MVP.
- Da nganh hang (dien tu, cong cu, xay dung, ...).
- Luxury authentication / size chart theo brand.
- Faceted search (loc size/color tren API search).

