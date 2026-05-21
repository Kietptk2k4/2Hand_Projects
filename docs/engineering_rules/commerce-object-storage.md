# Commerce Object Storage (MinIO)

Version: 1.0  
Applies to: **Commerce Service** media (product, shop, review, order snapshots).

Tham chiếu: `docs/business-spec/commerce-service-spec.md` § 1.1, Auth pattern `docs/feature_requirements/auth/FR_UpdateAvatar.md`.

## 1. Infrastructure

- **MinIO shared** từ `Infrastructure/docker-compose.yml` — **không** tạo container/instance MinIO riêng cho `commerce-service`.
- Local: API `http://localhost:9000`, Console `http://localhost:9001` (credentials mẫu trong compose).
- Production: endpoint S3-compatible (cùng contract URL); bucket name giữ nguyên hoặc map qua env.

## 2. Commerce Buckets (MVP)

| Bucket | Nội dung | Bảng / cột PostgreSQL |
|--------|----------|------------------------|
| `2hands-commerce-product` | Ảnh/video sản phẩm | `product_media.media_url` |
| `2hands-commerce-review` | Media review | `review_media.url` |
| `2hands-commerce-shop` | Avatar/cover shop | `seller_shops.avatar_url`, `seller_shops.cover_url` |

Auth dùng bucket riêng `2hands-avatar` — **không** gộp với commerce.

**URL pattern (local ví dụ):** `http://localhost:9000/{bucket}/{object-key}`  
Production: public endpoint hoặc CDN trỏ cùng object key.

## 3. Persistence Model

- PostgreSQL **chỉ** lưu URL + metadata (`media_type`, `sort_order`, …).
- Binary file nằm trên MinIO.
- Checkout: `order_items.image_snapshot` copy URL media chính (`product_media`, `sort_order` thấp nhất) tại thời điểm mua — immutable sau đó.

## 4. Upload Flows

### 4.1 Luồng chính (ưu tiên MVP — giống Auth avatar)

1. Client gọi Commerce API lấy **presigned PUT URL** (hoặc upload gateway) cho bucket/object-key phù hợp.
2. Client upload trực tiếp lên MinIO.
3. Client gọi Commerce API với `media_url` / `avatar_url` / `cover_url` đã upload.
4. Commerce validate URL (domain/bucket prefix) → insert/update DB.

Out of scope endpoint lưu URL: xử lý binary stream multipart (trừ FR chỉ định luồng phụ).

### 4.2 Luồng phụ — Review multipart (`FR_UploadReviewMedia`)

- Commerce nhận `multipart` → proxy upload lên `2hands-commerce-review` → insert `review_media`.
- Vẫn dùng MinIO shared; cleanup orphan object nếu DB insert fail.

## 5. Security & Validation

- JWT + ownership (seller/product, buyer/review, shop).
- `media_url` / `url` / `avatar_url` / `cover_url` phải thuộc bucket/domain cho phép.
- Giới hạn `media_type`, file size, số file mỗi entity (theo API policy).
- Không log presigned secret, access key, password MinIO.
- Prod: khuyến nghị HTTPS public URL.

## 6. Failure Handling

- MinIO upload OK + DB fail → cleanup orphan object khi có thể (best effort).
- Storage fail → `503` / business error; không ghi metadata DB.

## 7. Env gợi ý (implementation sau)

```properties
COMMERCE_MINIO_ENABLED=false
COMMERCE_MINIO_ENDPOINT=http://localhost:9000
COMMERCE_MINIO_PUBLIC_URL=http://localhost:9000
COMMERCE_MINIO_BUCKET_PRODUCT=2hands-commerce-product
COMMERCE_MINIO_BUCKET_REVIEW=2hands-commerce-review
COMMERCE_MINIO_BUCKET_SHOP=2hands-commerce-shop
```

## 8. Out of Scope (docs)

- IaC bucket policy chi tiết.
- Thay đổi schema Postgres (giữ cột URL hiện tại).
- CDN/virus scan nâng cao.
