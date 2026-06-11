# View Global Feed - API and Behavior

## 1. Muc tieu nghiep vu
- Cho phep user da dang nhap xem danh sach bai viet cong khai moi nhat tren toan he thong.
- Dam bao ket qua feed tuan thu visibility, status va soft-delete rule.

## 2. API Contract
- **Method:** `GET`
- **URL:** `/api/v1/social/feed/global`
- **Auth:** Bat buoc JWT Bearer token.
- **Headers:**
  - `Authorization: Bearer <access_token>`
  - `Content-Type: application/json` (optional voi GET)
- **Query params:**
  - `page` (optional, default `0`, min `0`)
  - `size` (optional, default `20`, min `1`, max `50`)
- **Request body:** Khong co.

## 3. Response schema + vi du thanh cong
- Response wrapper chuan:

```json
{
  "code": 200,
  "success": true,
  "message": "Lay global feed thanh cong.",
  "data": {
    "items": [
      {
        "postId": "507f1f77bcf86cd799439011",
        "authorId": "d7548df7-8b14-4a35-86cc-3f3e6adcbaf3",
        "caption": "hello world",
        "media": [
          {
            "url": "https://cdn.2hands.vn/post/1.jpg",
            "type": "IMAGE"
          }
        ],
        "visibility": "PUBLIC",
        "likeCount": 12,
        "replyCount": 3,
        "hashtags": ["spring", "java"],
        "productTags": [
          {
            "productId": "c1000000-0000-4000-8000-000000000001",
            "price": 199000,
            "name": "iPhone 14",
            "imageUrl": "https://cdn.2hands.vn/commerce/p1.jpg",
            "category": "Dien thoai",
            "available": true
          }
        ],
        "allowComments": true,
        "createdAt": "2026-05-18T10:15:30Z",
        "updatedAt": "2026-05-18T10:20:30Z"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1,
      "hasNext": false
    }
  },
  "errors": null,
  "timestamp": "2026-05-18T10:30:00Z"
}
```

## 4. Danh sach ma loi + vi du loi
- **401 Unauthorized**: Thieu token hoac token khong hop le.
- **400 Bad Request**: Pagination khong hop le (`page < 0`, `size < 1`, `size > 50`).
- **500 Internal Server Error**: Loi he thong khong mong muon.

Vi du loi pagination:

```json
{
  "code": 400,
  "success": false,
  "message": "Tham so pagination khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "size",
      "reason": "MUST_BE_BETWEEN_1_AND_50"
    }
  ],
  "timestamp": "2026-05-18T10:30:00Z"
}
```

## 5. Business rules (visibility/filtering/pagination/sorting)
- Chi lay post co `status = ACTIVE`.
- Chi lay post co `visibility = PUBLIC`.
- Khong tra post `DRAFT` hoac `DELETED` (soft-delete filtering).
- Sort theo `created_at DESC` (moi nhat truoc).
- Pagination theo `page/size` (offset paging qua Spring Data Page).

## 6. Edge cases
- Feed rong: tra `items = []`, `totalElements = 0`, `hasNext = false`.
- User co token hop le nhung khong co post cong khai: van tra 200 voi list rong.
- Tham so `size` vuot gioi han cho phep: tra 400 va chi ro field sai.

## 7. Phu thuoc du lieu (Mongo/Postgre)
- **MongoDB `posts` collection**:
  - Doc voi dieu kien `status=ACTIVE`, `visibility=PUBLIC`.
  - Sort `created_at DESC`.
- **PostgreSQL**:
  - Khong can truy van trong use case Global Feed MVP.
- **Index lien quan**:
  - `posts(status, visibility, created_at desc)` theo migration mongo init.

## 8. Ghi chu FE integration (`frontend-api-integration.md`)
- FE phai parse payload tai `data`.
- FE phai check `success` truoc khi render feed.
- FE nen map errors field-level tu `errors[]` cho pagination params.
- FE nen gui token qua `Authorization` header qua HTTP interceptor.
- FE layer API nen unwrap wrapper truoc khi tra du lieu cho UI components.
