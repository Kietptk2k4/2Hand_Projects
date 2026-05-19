# Edit Post – API & Behavior

## 1. Business Goal
Cho phép tác giả cập nhật nội dung bài viết đã tạo (caption, media, hashtags, visibility, allowComments, productTags) theo chính sách patch: field không gửi lên giữ nguyên.

## 2. API Contract

- **Method:** PUT
- **URL:** `/api/v1/social/posts/{postId}`
- **Auth:** Bearer JWT (required)

### Path Parameters

| Field    | Type   | Required | Mô tả                          |
|----------|--------|----------|--------------------------------|
| `postId` | String | yes      | ID bài viết (MongoDB ObjectId). |

### Request Body (application/json)

Tất cả field đều **optional** (patch). Chỉ field có trong body mới được cập nhật.

| Field              | Type                      | Mô tả                                                                 |
|--------------------|---------------------------|-----------------------------------------------------------------------|
| `caption`          | String (max 2000)         | Nội dung chữ; không được chứa script/HTML nguy hiểm.                  |
| `media`            | Array\<MediaItem\> (max 10) | Thay thế toàn bộ danh sách media (không merge từng phần).          |
| `media[].url`      | String                    | URL media.                                                            |
| `media[].type`     | `IMAGE` hoặc `VIDEO`       | Loại media.                                                           |
| `productTags`      | Array\<ProductTag\> (max 10) | Thay thế toàn bộ product tags.                                     |
| `productTags[].productId` | String (UUID)      | ID sản phẩm (chỉ validate format).                                    |
| `productTags[].price`     | Decimal              | Giá hiển thị (>= 0).                                                  |
| `visibility`       | `PUBLIC` hoặc `FOLLOWERS` | Quyền xem post.                                                       |
| `allowComments`    | Boolean                   | Cho phép comment hay không.                                           |
| `hashtags`         | Array\<String\> (max 30)  | Thay thế toàn bộ danh sách hashtag.                                   |

### Ví dụ Request (chỉ sửa caption)

```json
{
  "caption": "Đã bán — cập nhật mô tả"
}
```

## 3. Response – Success

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat bai viet thanh cong.",
  "data": {
    "postId": "507f1f77bcf86cd799439011",
    "authorId": "550e8400-e29b-41d4-a716-446655440001",
    "caption": "Đã bán — cập nhật mô tả",
    "media": [
      { "url": "https://cdn.2hands.vn/img/abc.jpg", "type": "IMAGE" }
    ],
    "productTags": [],
    "status": "ACTIVE",
    "visibility": "PUBLIC",
    "allowComments": true,
    "hashtags": ["thoidai"],
    "createdAt": "2026-05-19T02:54:00Z",
    "updatedAt": "2026-05-19T10:30:00Z"
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string              | Mô tả                                                                 |
|------|--------------------------|-----------------------------------------------------------------------|
| 401  | `SOCIAL-401`             | Không có hoặc JWT không hợp lệ.                                       |
| 400  | `SOCIAL-400-VALIDATION`  | Payload không hợp lệ (caption quá dài, media type sai, v.v.).         |
| 403  | `SOCIAL-403`             | Không phải tác giả bài viết.                                          |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản bị SUSPENDED/DELETED.                                       |
| 404  | `SOCIAL-404`             | Post không tồn tại hoặc đã bị xóa mềm (`DELETED`).                    |
| 500  | `SOCIAL-500`             | Lỗi server.                                                           |

### Ví dụ Error 403

```json
{
  "code": 403,
  "success": false,
  "message": "Ban khong co quyen chinh sua bai viet nay.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 5. Business Rules

- Chỉ `author_id` (lấy từ JWT) mới được sửa post.
- Post có `status = DELETED` không được sửa → HTTP 404.
- Field **không** có trong request body giữ nguyên (patch).
- Field **có** trong body (kể cả `[]` hoặc `null` cho caption) được áp dụng: mảng rỗng xóa hết phần tử; `caption: null` xóa caption.
- `visibility` nếu gửi phải là `PUBLIC` hoặc `FOLLOWERS`.
- User `SUSPENDED`/`DELETED` không được thực hiện hành động → HTTP 403.
- `updated_at` được cập nhật mỗi lần sửa thành công.
- `status` (DRAFT/ACTIVE) **không** đổi qua API Edit; dùng flow publish riêng nếu có.
- `like_count`, `reply_count` không thay đổi khi edit.

## 6. Edge Cases

- **Body rỗng `{}`:** Post không đổi nội dung; `updated_at` vẫn được cập nhật.
- **Chỉ gửi `hashtags: []`:** Xóa toàn bộ hashtag.
- **Gửi `media` mới:** Thay thế hoàn toàn media cũ (không append).
- **User khác sửa post:** HTTP 403.
- **Post đã soft-delete:** HTTP 404, message gợi ý post đã xóa.
- **Caption chứa `<script>`:** HTTP 400 validation.
- **Draft post:** Tác giả vẫn sửa được (DRAFT → ACTIVE qua flow khác nếu cần).

## 7. Data Dependencies

| Storage | Collection/Table | Action                                      |
|---------|------------------|---------------------------------------------|
| MongoDB | `posts`          | Read by id; update fields + `updated_at`.   |
| MongoDB | `user_projections` | Read-only: kiểm tra status user.          |

## 8. FE Integration Notes

- **Patch semantics:** Chỉ gửi field cần đổi; không gửi field giữ nguyên.
- **Replace arrays:** `media`, `hashtags`, `productTags` là thay thế toàn bộ, không merge.
- **Token refresh:** 401 → refresh token rồi retry.
- **Field mapping:** `postId`, `authorId`, `allowComments`, `createdAt`, `updatedAt`, `productId` (camelCase trong response).
- **Publish draft:** Sau khi tạo draft (`CreatePost` với `publish: false`), FE có thể gọi Edit để cập nhật nội dung; chuyển DRAFT → ACTIVE cần API publish riêng (nếu có) hoặc mở rộng sau.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
