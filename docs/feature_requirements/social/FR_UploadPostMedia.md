# Functional Requirement (FR) - Upload Post Media (MinIO)

## 1. Feature Overview

Cho phep user lay **presigned URL** de upload anh/video len MinIO **truoc** khi tao hoac sua post. File upload xong → FE truyen `url` + `type` vao `FR_CreatePost` / `FR_EditPost` (khong stream binary qua Social API trong MVP).

## 2. Actors

- **User:** Chu noi dung da dang nhap.
- **Social Service:** Cap presigned URL, validate intent.
- **MinIO:** Object storage dung chung ha tang.

## 3. MinIO Context

- Ha tang: MinIO trong `Infrastructure/docker-compose.yml` (API port `9000` local).
- Bucket khuyen nghi: `2hands-social-post` (env `MINIO_SOCIAL_POST_BUCKET`).
- Object key pattern: `posts/{user_id}/{uuid}.{ext}`.
- URL public/CDN vi du: `https://cdn.2hands.vn/social/posts/{user_id}/{uuid}.jpg`.
- Social Service **khong** luu binary trong HTTP body MVP (giong `auth/FR_AvatarUpload`).

## 4. Scope

- **In Scope:**
  - Cap presigned PUT URL cho 1 file (image hoac video).
  - Whitelist content-type va gioi han kich thuoc.
  - TTL ngan (5–15 phut).
  - Tra `object_key`, `media_url`, `expires_at` de FE dung trong `media[]` khi create/edit post.
- **Out of Scope:**
  - Transcode video, thumbnail generation.
  - Virus scan / AI moderation file.
  - Xoa object khi user xoa post (async job sau).
  - Upload avatar (Auth `FR_AvatarUpload`).

## 5. Preconditions

- JWT hop le.
- User `ACTIVE` trong projection (`FR_EnforceUserStatusOnWrite`).
- MinIO bucket da tao (init script / ops).

## 6. API Contract

**Endpoint:** `POST /api/v1/social/posts/media/upload-url`

**Auth:** Required (JWT)

**Request body:**

```json
{
  "content_type": "image/jpeg",
  "file_size_bytes": 2097152,
  "media_kind": "IMAGE"
}
```

| Field | Type | Required | Mo ta |
|-------|------|----------|-------|
| `content_type` | string | yes | MIME whitelist |
| `file_size_bytes` | long | yes | Kich thuoc file du kien |
| `media_kind` | string | yes | `IMAGE` hoac `VIDEO` — khop `media[].type` khi create post |

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao link upload media thanh cong.",
  "data": {
    "upload_url": "https://minio.../presigned-put-url",
    "object_key": "posts/{user_id}/{uuid}.jpg",
    "media_url": "https://cdn.2hands.vn/social/posts/{user_id}/{uuid}.jpg",
    "media_kind": "IMAGE",
    "expires_at": "2026-05-21T10:15:00Z",
    "max_file_size_bytes": 10485760,
    "allowed_content_types": ["image/jpeg", "image/png", "image/webp", "video/mp4"]
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 7. Business Rules

### 7.1 Policy gioi han (align `CreatePostUseCase`)

| Rule | Gia tri |
|------|---------|
| Max media items per post | 10 (validate luc create/edit, khong phai luc presign) |
| Max image size | 10 MB (configurable) |
| Max video size | 100 MB (configurable) |
| Image MIME | `image/jpeg`, `image/png`, `image/webp` |
| Video MIME | `video/mp4` (MVP) |

- `user_id` tu JWT; object key **bat buoc** chua dung `user_id` de tranh user A ghi de file user B.
- Presigned URL chi cho PUT dung `object_key` da cap.
- Rate limit: so URL / user / phut (vi du 30).
- URL tra ve phai thuoc bucket/path duoc phep khi `FR_CreatePost` validate `media[].url` (prefix whitelist hoac signed URL pattern).

### 7.2 Luong FE

1. Goi `POST .../media/upload-url`.
2. `PUT` file len MinIO bang `upload_url`.
3. Goi `POST /api/v1/social/posts` voi `media: [{ "url": "media_url", "type": "IMAGE" }]`.

### 7.3 Orphan objects

- Upload thanh cong nhung khong tao post → object orphan; job cleanup theo prefix + age (out of scope MVP).

## 8. Database Impact

- Khong bat buoc ghi DB o buoc cap URL.
- Optional audit log (PostgreSQL) neu can trace abuse.

## 9. Transaction

- Khong transaction domain.

## 10. Security

- JWT bat buoc + `FR_EnforceUserStatusOnWrite`.
- Presigned TTL ngan; khong log full presigned URL production.
- Validate `file_size_bytes` <= max truoc khi cap URL.
- Chi expose CDN URL public, khong expose MinIO credential.

## 11. Failure Cases

| HTTP | code | Tinh huong |
|------|------|------------|
| 400 | SOCIAL-400-VALIDATION | MIME / size / media_kind khong hop le |
| 401 | SOCIAL-401 | Thieu JWT |
| 403 | SOCIAL-403-SUSPENDED | User suspended |
| 503 | SOCIAL-500 | MinIO khong kha dung |

## 12. Acceptance Criteria

- **AC1:** User active nhan presigned URL hop le cho image.
- **AC2:** User suspended bi chan → 403.
- **AC3:** Content-type ngoai whitelist → 400.
- **AC4:** File vuot max size khai bao → 400.
- **AC5:** Sau upload, create post voi `media_url` hop le → 201.

## 13. Related

| FR / Tai lieu | Muc dich |
|---------------|----------|
| `FR_CreatePost`, `FR_EditPost` | Gan media URL vao post |
| `auth/FR_AvatarUpload` | Mau presigned pattern |
| `commerce/FR_UploadReviewMedia` | Mau bucket MinIO commerce |
| `docs/engineering_rules/commerce-object-storage.md` | Quy tac object storage chung (neu co social section sau) |
| `docs/database/social-schema.md` | `POSTS.media[]` |

## 14. Implementation Notes (hien trang)

- `CreatePost` nhan `media[].url` truc tiep — **chua co** API presign.
- Can config MinIO client trong `social-service` infrastructure (tuong tu auth/commerce khi implement).
- Bucket `2hands-social-post` can tao trong docker/init ops.
