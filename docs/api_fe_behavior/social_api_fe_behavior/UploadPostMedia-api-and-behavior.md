# Upload Post Media – API & Behavior

## 1. Business Goal

Cấp **presigned PUT URL** để FE upload ảnh/video lên MinIO trước khi gọi Create/Edit Post. Sau khi upload xong, dùng `media_url` trong `media[]` của post.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/posts/media/upload-url`
- **Auth:** Bearer JWT (required)

### Request body

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `content_type` | string | yes | MIME whitelist (image/jpeg, image/png, image/webp, video/mp4) |
| `file_size_bytes` | long | yes | Kích thước file dự kiến (phải ≤ max theo `media_kind`) |
| `media_kind` | string | yes | `IMAGE` hoặc `VIDEO` — khớp `media[].type` khi tạo post |

### Response 200

| Field | Mô tả |
|-------|-------|
| `upload_url` | Presigned PUT URL (TTL ngắn, không log full URL ở client production) |
| `object_key` | `posts/{user_id}/{uuid}.{ext}` |
| `media_url` | URL CDN/public dùng trong `media[].url` khi create/edit |
| `media_kind` | `IMAGE` / `VIDEO` |
| `expires_at` | ISO-8601 UTC |
| `max_file_size_bytes` | Giới hạn theo kind (image 10MB, video 100MB mặc định) |
| `allowed_content_types` | Danh sách MIME được phép |

**Message:** `Tao link upload media thanh cong.`

## 3. Luồng FE

1. `POST .../media/upload-url` với `content_type`, `file_size_bytes`, `media_kind`.
2. `PUT` binary lên `upload_url` (header `Content-Type` khớp request).
3. `POST /api/v1/social/posts` với `media: [{ "url": "<media_url>", "type": "<media_kind>" }]`.

## 4. Errors

| HTTP | code | Tình huống |
|------|------|------------|
| 400 | SOCIAL-400-VALIDATION | MIME / size / media_kind không hợp lệ |
| 401 | SOCIAL-401 | Thiếu JWT |
| 403 | SOCIAL-403-SUSPENDED | User suspended / không write được |
| 429 | SOCIAL-429 | Vượt rate limit presign (mặc định 30/phút/user) |
| 503 | SOCIAL-503-MINIO | MinIO tắt hoặc không khả dụng |

## 5. Config (ops)

- `SOCIAL_MINIO_ENABLED=true`
- `MINIO_SOCIAL_POST_BUCKET=2hands-social-post`
- `SOCIAL_MINIO_PUBLIC_URL` — CDN base (vd `https://cdn.2hands.vn`)
- Bucket phải tồn tại trên MinIO (docker/init).

## 6. Related

- `FR_CreatePost`, `FR_EditPost` — validate `media[].url` thuộc prefix CDN của user khi MinIO bật
- `FR_EnforceUserStatusOnWrite` — guard trước khi cấp URL
- `auth/FR_AvatarUpload` — mẫu presigned tương tự
