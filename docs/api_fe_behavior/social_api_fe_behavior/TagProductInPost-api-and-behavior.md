# Tag Product In Post – API & Behavior

## 1. Business Goal
Cho phép tác giả gắn sản phẩm Commerce vào bài viết qua `productTags`, liên kết nội dung social với ngữ cảnh mua bán. Tag được lưu cùng transaction khi **tạo** hoặc **sửa** post (không có endpoint riêng).

## 2. API Contract

Feature này dùng chung contract của **Create Post** và **Edit Post**.

### 2.1 Tạo post kèm product tags

- **Method:** POST
- **URL:** `/api/v1/social/posts`
- **Auth:** Bearer JWT (required)

### 2.2 Sửa product tags trên post hiện có

- **Method:** PUT
- **URL:** `/api/v1/social/posts/{postId}`
- **Auth:** Bearer JWT (required)
- **Patch semantics:** Gửi `productTags` → **thay thế toàn bộ** danh sách; không gửi field → giữ nguyên tags cũ.

### Request body – `productTags`

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `productTags` | Array\<ProductTag\> (max 10) | optional | Danh sách sản phẩm gắn vào post. |
| `productTags[].productId` | String (UUID) | required (nếu có phần tử) | Tham chiếu `product_id` Commerce; chỉ validate định dạng UUID. |
| `productTags[].price` | Decimal | optional | Giá hiển thị trên post (>= 0). |

### Ví dụ (create)

```json
{
  "caption": "Áo thun size M",
  "productTags": [
    { "productId": "550e8400-e29b-41d4-a716-446655440000", "price": 150000 }
  ],
  "visibility": "PUBLIC",
  "allowComments": true,
  "publish": true
}
```

### Ví dụ (edit – chỉ đổi tags)

```json
{
  "productTags": [
    { "productId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8", "price": 120000 }
  ]
}
```

## 3. Response – Success

**Create:** HTTP **201** — `data.productTags` trả về danh sách đã lưu (cùng envelope Create Post).

**Edit:** HTTP **200** — `data.productTags` là danh sách sau cập nhật (cùng envelope Edit Post).

```json
{
  "code": 201,
  "success": true,
  "message": "Tao bai viet thanh cong.",
  "data": {
    "postId": "507f1f77bcf86cd799439011",
    "productTags": [
      { "productId": "550e8400-e29b-41d4-a716-446655440000", "price": 150000 }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string | Field (gợi ý) | Mô tả |
|------|-------------|-----------------|-------|
| 400 | `SOCIAL-400` | `productTags` | Quá 10 tag hoặc trùng `productId` trong cùng request. |
| 400 | `SOCIAL-400` | `productTags[].product_id` | `productId` rỗng hoặc không phải UUID. |
| 400 | `SOCIAL-400` | `productTags[].price` | `price` < 0. |
| 401 | `SOCIAL-401` | — | Thiếu / JWT không hợp lệ. |
| 403 | `SOCIAL-403` | — | Edit post: không phải author. |
| 404 | `SOCIAL-404` | — | Edit: post không tồn tại hoặc đã xóa. |

Lỗi validation từ `@Valid` trên DTO (ví dụ thiếu `productId` khi có object trong mảng) cũng trả **400** với envelope chuẩn.

## 5. Business Rules

- Tối đa **10** `productTags` mỗi post.
- Mỗi `productId` chỉ xuất hiện **một lần** trong cùng request.
- `productId` phải là UUID hợp lệ; **không** gọi trực tiếp DB Commerce — xác thực sản phẩm còn tồn tại (nếu cần) theo integration API sau này.
- `price` optional; nếu có thì **>= 0**.
- Lưu trong MongoDB field `product_tags` (snake_case) cùng transaction create/edit post.
- Chỉ **author** mới sửa được `productTags` qua PUT.

## 6. Edge Cases

- **`productTags: []` trên create:** Post không có tag sản phẩm.
- **`productTags: []` trên edit:** Xóa hết tag (thay thế bằng mảng rỗng).
- **Không gửi `productTags` trên edit:** Giữ nguyên tags hiện tại.
- **Trùng `productId` trong một request:** HTTP 400.
- **UUID hợp lệ nhưng product không tồn tại ở Commerce:** Hiện tại vẫn lưu (chỉ validate format); FE có thể gọi Commerce để preview/confirm trước khi post.

## 7. Data Dependencies

| Storage | Collection/Table | Action |
|---------|------------------|--------|
| MongoDB | `posts` | `product_tags[]` insert (create) / replace (edit) |

## 8. FE Integration Notes

- Dùng **Create Post** / **Edit Post**; không có route `/products/tag` riêng.
- Map JSON **camelCase** (`productId`, `price`); DB lưu `product_id` / `price`.
- Khi edit, gửi **full list** tags mới nếu muốn thay đổi (không merge từng item).
- Hiển thị giá: dùng `price` từ response; có thể đồng bộ với Commerce catalog khi mở chi tiết sản phẩm.
- Tham chiếu thêm: `CreatePost-api-and-behavior.md`, `EditPost-api-and-behavior.md`, `docs/engineering_rules/frontend-api-integration.md`.
