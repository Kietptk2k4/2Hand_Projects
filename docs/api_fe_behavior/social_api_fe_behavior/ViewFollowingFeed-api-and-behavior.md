# View Following Feed - API and Behavior

## 1. Muc tieu nghiep vu
- Cho phep user da dang nhap xem feed tu nhung tai khoan ma user dang follow.
- Dam bao chi tra ve bai viet hop le theo status, privacy/visibility va quan he follow hop le.

## 2. API Contract
- **Method:** `GET`
- **URL:** `/api/v1/social/feed/following`
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
  "message": "Lay following feed thanh cong.",
  "data": {
    "items": [
      {
        "postId": "507f1f77bcf86cd799439012",
        "authorId": "a5af3d91-b53a-4b82-9eef-1dfa15e1fbb5",
        "caption": "following post",
        "media": [
          {
            "url": "https://cdn.2hands.vn/post/2.jpg",
            "type": "IMAGE"
          }
        ],
        "visibility": "FOLLOWERS",
        "likeCount": 4,
        "replyCount": 1,
        "hashtags": ["social"],
        "productTags": [
          {
            "productId": "c1000000-0000-4000-8000-000000000001",
            "price": 199000
          }
        ],
        "allowComments": true,
        "createdAt": "2026-05-18T10:16:30Z",
        "updatedAt": "2026-05-18T10:20:40Z"
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
  "timestamp": "2026-05-18T11:00:00Z"
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
      "field": "page",
      "reason": "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0"
    }
  ],
  "timestamp": "2026-05-18T11:00:00Z"
}
```

## 5. Business rules (following-only, visibility, pagination, sorting)
- Chi lay follow relation co `status = ACCEPTED` trong `follows`.
- Chi lay bai viet co `author_id` nam trong danh sach followee cua requester.
- Chi lay bai viet `status = ACTIVE`.
- Visibility hop le cho following feed:
  - `PUBLIC`: hien thi.
  - `FOLLOWERS`: hien thi khi ton tai relation follow hop le (da duoc dam bao boi filter followee IDs).
- Khong tra post `DRAFT`/`DELETED` (soft-delete filtering).
- Sort theo `created_at DESC`.
- Pagination theo `page/size` voi metadata ro rang cho FE.

## 6. Edge cases
- Requester chua follow ai: tra `items = []`, `totalElements = 0`.
- Follow relation `PENDING`: khong duoc tinh vao following feed.
- Neu mot user da unfollow: bai viet cua user do khong con xuat hien trong following feed.
- Du lieu post da soft-delete (`status = DELETED`) hoac khong hop le: khong tra ve.
- `page` out-of-range: tra ket qua rong voi `200`, metadata phan trang van day du.
- `size` vuot gioi han cho phep: tra `400`.

## 7. Phu thuoc du lieu (Mongo/Postgre)
- **PostgreSQL `follows`**:
  - Query `followee_id` theo `follower_id` va `status = ACCEPTED`.
  - Index su dung: `idx_follows_follower_created`.
- **MongoDB `posts`**:
  - Query theo `status = ACTIVE`, `author_id IN followee_ids`, `visibility IN (PUBLIC, FOLLOWERS)`.
  - Sort `created_at DESC`.
  - Index lien quan: `idx_posts_author_status_created_desc`.

## 8. Ghi chu FE integration (`frontend-api-integration.md`)
- FE phai doc du lieu tai `data` va check `success` truoc khi render.
- FE nen map field-level error tu `errors[]` cho tham so pagination.
- FE nen unwrap response wrapper tai API layer, khong parse trong component.
- FE nen su dung query key rieng cho following feed (vd: `feedKeys.following(page,size)`), invalidate khi follow/unfollow.
