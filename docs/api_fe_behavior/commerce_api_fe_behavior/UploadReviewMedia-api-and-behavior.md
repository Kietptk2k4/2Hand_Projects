# Upload Review Media – API & Behavior

## 1. Business Goal

Buyer upload anh/video dinh kem review cua minh. File luu tren MinIO bucket `2hands-commerce-review`; metadata luu `review_media`. Khong thay doi snapshot order/shipment.

## 2. API Contract

- **Method:** POST
- **URL:** `/commerce/api/v1/reviews/{reviewId}/media`
- **Auth:** Bearer JWT (buyer)
- **Content-Type:** `multipart/form-data`
- **Part name:** `files` (co the gui nhieu file trong mot request)

### Policy

| Rule | Gia tri |
|------|---------|
| Toi da media / review | 10 |
| Image types | `image/jpeg`, `image/png`, `image/webp` |
| Video types | `video/mp4`, `video/webm` |
| Max image size | 5 MB |
| Max video size | 50 MB |
| Review status | Chi `VISIBLE` |

Can bat `COMMERCE_MINIO_ENABLED=true` va MinIO shared chay (xem `docs/engineering_rules/commerce-object-storage.md`).

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Upload media danh gia thanh cong.",
  "data": {
    "media": [
      {
        "id": "aa0e8400-e29b-41d4-a716-446655440010",
        "url": "http://localhost:9000/2hands-commerce-review/reviews/550e8400-e29b-41d4-a716-446655440000/990e8400-e29b-41d4-a716-446655440004/abc.jpg",
        "type": "IMAGE"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-21T16:00:01Z"
}
```

`media` la danh sach file **vua upload** trong request (khong phai toan bo media cua review).

## 4. Server behavior

1. JWT `user_id` = buyer; tim review theo `reviewId` + `buyer_id`.
2. Review phai `VISIBLE`.
3. Kiem tra `count(review_media) + files.size() <= 10`.
4. Validate tung file (type, size, khong rong).
5. Upload len MinIO (`reviews/{buyerId}/{reviewId}/{uuid}.{ext}`).
6. Insert `review_media` trong transaction.
7. Neu insert fail sau khi upload: xoa object orphan (best effort).

**Luong chinh (MVP khuyen nghi):** FE presigned upload truc tiep MinIO, sau do API khac luu URL — endpoint nay la **luong phu** multipart proxy.

## 5. FE Behavior

- FormData: append tung file voi key `files`.
- Tao review truoc (`POST /reviews`), roi upload media.
- Khong upload khi review `HIDDEN`.
- Hien thi loi 409 khi da du 10 media.

## 6. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT |
| 404 | `COMMERCE-404-REVIEW` | Review khong ton tai / khong thuoc buyer |
| 409 | `COMMERCE-409-REVIEW-VISIBLE` | Review `HIDDEN` |
| 409 | `COMMERCE-409-REVIEW-MEDIA` | Vuot 10 media / review |
| 400 | `COMMERCE-400-VALIDATION` | Khong co file / file rong |
| 400 | `COMMERCE-400-MEDIA-TYPE` | Content-Type khong hop le |
| 400 | `COMMERCE-400-MEDIA-SIZE` | Vuot gioi han dung luong |
| 503 | `COMMERCE-503-MINIO` | MinIO tat hoac upload/DB fail |

## 7. Related

- FR: `docs/feature_requirements/commerce/FR_UploadReviewMedia.md`
- Create review: `CreateProductReview-api-and-behavior.md`
- Object storage: `docs/engineering_rules/commerce-object-storage.md`
