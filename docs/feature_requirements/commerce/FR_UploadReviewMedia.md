# Functional Requirement - Upload Review Media

## 1. Feature Overview

Cho phep buyer upload media cho review cua minh. Media co the la image/video theo policy va duoc luu vao `review_media` sau khi file storage thanh cong.

## 2. Actors

- **Buyer:** Upload media cho review.
- **System:** Validate ownership, store media and attach metadata.
- **MinIO:** Object storage shared (bucket `2hands-commerce-review`).

## 3. MinIO Context

- MinIO shared tu `Infrastructure/docker-compose.yml` — **khong** instance rieng commerce-service.
- Bucket: `2hands-commerce-review`.
- **Luong chinh:** FE presigned upload → MinIO → API nhan `url` (neu policy cho phep).
- **Luong phu (endpoint nay):** multipart → Commerce proxy upload MinIO → insert `review_media`.
- URL vi du local: `http://localhost:9000/2hands-commerce-review/{object-key}`.

## 4. Scope

**In Scope:**

- Upload media for existing review.
- Validate media type/size/count.
- Store media URL and type.

**Out of Scope:**

- Create review.
- Media moderation/scan nang cao.

## 5. API Contract

**Endpoint:** `POST /commerce/api/v1/reviews/{reviewId}/media`

**Auth:** Required (JWT)

**Request:** multipart file(s) or pre-uploaded media URL command theo storage policy.

**Response data:**

- `media[]`
  - `id`
  - `url`
  - `type`

## 6. Business Rules

- Buyer can upload media only to own review.
- Review should be `VISIBLE` unless policy allows hidden edit.
- Media type must be allowed.
- Media count per review should be limited by API policy.
- If DB insert fails after storage upload, cleanup orphan file if possible.

## 7. Database Impact

- Read `reviews`.
- Insert `review_media`.

## 8. Transaction

- DB transaction required for metadata insert.
- External storage operation cannot be fully transactional with DB; cleanup on failure.

## 9. Security

- JWT required.
- Ownership check by `reviews.buyer_id`.
- Validate file type/size.

## 10. Failure Cases

- Review not found/not owned -> 404.
- Invalid media type/size -> 400.
- Media count exceeded -> 409.
- Storage failure -> 503.

## 11. Acceptance Criteria

- Buyer uploads media to own review.
- Other buyer cannot attach media.
- Invalid media is rejected.
- Media metadata is saved and returned.

