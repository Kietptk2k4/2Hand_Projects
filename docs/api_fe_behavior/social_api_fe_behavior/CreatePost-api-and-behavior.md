# Create Post – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập tạo bài viết mới với caption, media, hashtag, product tags, visibility, và trạng thái DRAFT hoặc ACTIVE.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/posts`
- **Auth:** Bearer JWT (required)

### Request Body (application/json)

| Field              | Type                      | Required | Mô tả                                                                 |
|--------------------|---------------------------|----------|-----------------------------------------------------------------------|
| `caption`          | String (max 2000)         | optional | Nội dung chữ của bài viết.                                            |
| `media`            | Array\<MediaItem\> (max 10) | optional | Danh sách media đính kèm.                                            |
| `media[].url`      | String                    | required (nếu có media) | URL của media item.                                    |
| `media[].type`     | `IMAGE` hoặc `VIDEO`      | required (nếu có media) | Loại media.                                            |
| `productTags`      | Array\<ProductTag\> (max 10) | optional | Danh sách sản phẩm gắn vào post.                                  |
| `productTags[].productId` | String (UUID)      | required (nếu có productTag) | ID sản phẩm từ Commerce Service (chỉ validate format). |
| `productTags[].price`     | Decimal              | optional | Giá hiển thị trên post (>= 0).                                 |
| `visibility`       | `PUBLIC` hoặc `FOLLOWERS` | **required** | Quyền xem post.                                              |
| `allowComments`    | Boolean                   | optional (default: false) | Cho phép comment hay không.                         |
| `hashtags`         | Array\<String\> (max 30, mỗi hashtag max 100 ký tự) | optional | Danh sách hashtag. |
| `publish`          | Boolean                   | optional (default: false) | `true` → tạo với status `ACTIVE`; `false` → `DRAFT`. |

### Ví dụ Request

```json
{
  "caption": "Bán áo thun cũ, còn mới 90%",
  "media": [
    { "url": "https://cdn.2hands.vn/img/abc.jpg", "type": "IMAGE" }
  ],
  "productTags": [
    { "productId": "550e8400-e29b-41d4-a716-446655440000", "price": 150000 }
  ],
  "visibility": "PUBLIC",
  "allowComments": true,
  "hashtags": ["thoidai", "2hands"],
  "publish": true
}
```

## 3. Response – Success

**HTTP 201 Created**

```json
{
  "code": 201,
  "success": true,
  "message": "Tao bai viet thanh cong.",
  "data": {
    "postId": "507f1f77bcf86cd799439011",
    "authorId": "550e8400-e29b-41d4-a716-446655440001",
    "caption": "Bán áo thun cũ, còn mới 90%",
    "media": [
      { "url": "https://cdn.2hands.vn/img/abc.jpg", "type": "IMAGE" }
    ],
    "productTags": [
      { "productId": "550e8400-e29b-41d4-a716-446655440000", "price": 150000 }
    ],
    "status": "ACTIVE",
    "visibility": "PUBLIC",
    "allowComments": true,
    "hashtags": ["thoidai", "2hands"],
    "createdAt": "2026-05-19T02:54:00Z",
    "updatedAt": "2026-05-19T02:54:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-19T02:54:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string              | Mô tả                                                                 |
|------|--------------------------|-----------------------------------------------------------------------|
| 401  | `SOCIAL-401`             | Không có hoặc JWT không hợp lệ.                                       |
| 400  | `SOCIAL-400-VALIDATION`  | Payload không hợp lệ (visibility sai, caption quá dài, media type sai, productId không phải UUID, v.v.). |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản bị SUSPENDED hoặc DELETED, không thể tạo post.             |
| 500  | `SOCIAL-500`             | Lỗi server.                                                           |

### Ví dụ Error 400

```json
{
  "code": 400,
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": [
    { "field": "visibility", "reason": "Visibility chi chap nhan PUBLIC hoac FOLLOWERS." }
  ],
  "timestamp": "2026-05-19T02:54:00.123Z"
}
```

### Ví dụ Error 403

```json
{
  "code": 403,
  "success": false,
  "message": "Tai khoan bi dinh chi, khong the thuc hien hanh dong nay.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-19T02:54:00.123Z"
}
```

## 5. Business Rules

- `author_id` được lấy từ JWT token, **không nhận** từ request body.
- `visibility` bắt buộc là `PUBLIC` hoặc `FOLLOWERS`.
- `publish = true` → status `ACTIVE`; `publish = false` hoặc không truyền → status `DRAFT`.
- User có status `SUSPENDED` hoặc `DELETED` (theo local projection đồng bộ từ Auth) sẽ bị từ chối với HTTP 403.
- Nếu chưa có local projection của user (projection chưa được đồng bộ), hệ thống sẽ cho phép tạo post (best-effort, dựa trên tính hợp lệ của JWT).
- `productTags[].productId` chỉ validate định dạng UUID, **không** query trực tiếp DB của Commerce Service.
- `caption` có thể null (post chỉ có media), nhưng không được vượt quá 2000 ký tự.
- `like_count` và `reply_count` khởi tạo bằng `0`.

## 6. Edge Cases

- **Gửi `publish = false`:** Post được tạo với status `DRAFT`, không hiển thị trên feed của người khác.
- **Không truyền `media`:** Post hợp lệ nếu có `caption`.
- **`productId` không phải UUID:** Trả về lỗi 400 field `productTags[].product_id`.
- **Hơn 10 media items:** Trả về lỗi 400 field `media`.
- **Hơn 30 hashtags:** Trả về lỗi 400 field `hashtags`.
- **`caption` dài hơn 2000 ký tự:** Trả về lỗi 400 field `caption`.
- **JWT hợp lệ nhưng user bị SUSPENDED trong local projection:** Trả về lỗi 403.
- **Request body không có `visibility`:** Trả về lỗi 400 ngay ở tầng validation (`@NotBlank`).

## 7. Data Dependencies

| Storage    | Collection/Table   | Action                                    |
|------------|--------------------|-------------------------------------------|
| MongoDB    | `posts`            | Insert document mới với đầy đủ fields.   |
| MongoDB    | `user_projections` | Read-only: kiểm tra status user (best-effort). |

## 8. FE Integration Notes

- **Pagination:** Không áp dụng (endpoint tạo post).
- **Token Refresh:** Nếu nhận 401, FE cần gọi Refresh Token API và retry request.
- **Field mapping (snake_case → camelCase):**
  - `post_id` → `postId`
  - `author_id` → `authorId`
  - `allow_comments` → `allowComments`
  - `created_at` → `createdAt`
  - `updated_at` → `updatedAt`
  - `product_id` → `productId`
- **DRAFT flow:** FE có thể lưu draft (`publish: false`) và publish sau bằng Edit Post API.
- **Media upload:** Media `url` phải là URL đã upload lên CDN trước. Social Service không xử lý upload file.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
