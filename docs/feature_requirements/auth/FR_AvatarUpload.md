# Functional Requirement (FR) - Avatar Upload (Presigned URL)

## 1. Feature Overview

Cho phep user lay **presigned URL** (hoac upload policy) de upload file avatar len MinIO, sau do goi `FR_UpdateAvatar` de luu `avatar_url` vao `USER_PROFILES`. FR nay cover **buoc upload file**; `FR_UpdateAvatar` cover **buoc cap nhat URL** sau upload.

## 2. Actors

- **User:** Chu tai khoan da dang nhap.
- **Auth Service:** Cap presigned URL / validate upload intent.
- **MinIO:** Object storage (bucket avatar).

## 3. MinIO Context

- Ha tang: MinIO trong `Infrastructure/docker-compose.yml` (API `:9000`).
- Bucket khuyen nghi: `2hands-avatar` (hoac theo env `MINIO_AVATAR_BUCKET`).
- Object key pattern: `avatars/{user_id}/{uuid}.{ext}`.
- Auth Service **khong** stream binary trong HTTP request MVP; FE upload truc tiep len MinIO.

## 4. Scope

- **In Scope:**
  - Cap presigned PUT URL (hoac POST policy) cho 1 file avatar.
  - Gioi han content-type (`image/jpeg`, `image/png`, `image/webp`).
  - Gioi han kich thuoc file (vi du max 5MB).
  - TTL ngan (vi du 5–15 phut).
  - Tra ve `object_key` / `public_url` de FE goi `FR_UpdateAvatar`.
- **Out of Scope:**
  - Crop/resize anh.
  - Xoa object MinIO khi soft-delete user (co the async job sau).
  - Upload media post/social (Social/Commerce service).

## 5. Preconditions

- User da dang nhap (JWT).
- MinIO configured va bucket ton tai.

## 6. API Contract

**Endpoint:** `POST /api/v1/users/me/avatar/upload-url`

**Auth:** Required (JWT)

**Request body:**

```json
{
  "content_type": "image/png",
  "file_size_bytes": 1048576
}
```

**Response - 200 OK:**

```json
{
  "code": 200,
  "success": true,
  "message": "Tao link upload avatar thanh cong.",
  "data": {
    "upload_url": "https://minio.../presigned-put-url",
    "object_key": "avatars/{user_id}/{uuid}.png",
    "avatar_url": "https://cdn.2hands.vn/avatars/{user_id}/{uuid}.png",
    "expires_at": "2026-05-21T10:15:00Z",
    "max_file_size_bytes": 5242880,
    "allowed_content_types": ["image/jpeg", "image/png", "image/webp"]
  },
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

## 7. Business Rules

- `user_id` lay tu JWT, khong tu body.
- Chi cho phep content-type whitelist.
- `file_size_bytes` phai <= max (vi du 5MB); presigned URL enforce policy neu MinIO ho tro.
- Sau khi FE upload thanh cong len MinIO → goi `PATCH /api/v1/users/me/avatar` (`FR_UpdateAvatar`) voi `avatar_url`.
- Khong overwrite avatar OAuth da set neu user chua tu upload (policy `FR_UpdateAvatar`).
- Rate limit so lan cap upload URL / user / gio.

## 8. Database Impact

- Khong ghi DB bat buoc o buoc cap URL (optional: audit log).
- `USER_PROFILES` chi update khi `FR_UpdateAvatar`.

## 9. Transaction

- Read-only hoac ghi audit optional.

## 10. Security

- Presigned URL chi cho PUT 1 object key cu the.
- TTL ngan.
- Khong log presigned URL day du trong production log (hoac mask).
- Validate content-type/size truoc khi cap URL.

## 11. FE Behavior

1. User chon anh.
2. FE goi `POST .../avatar/upload-url`.
3. FE `PUT` file len `upload_url`.
4. FE goi `PATCH .../avatar` voi `avatar_url` tra ve.
5. Hien thi avatar moi tren profile.

## 12. Acceptance Criteria

- **AC1:** User hop le nhan presigned URL hop le.
- **AC2:** File vuot size / sai content-type → `400`.
- **AC3:** Sau upload MinIO + update avatar, profile hien thi URL moi.
- **AC4:** Flow khong gui binary qua Auth API.

## 13. Related

- `FR_UpdateAvatar.md`
- `docs/business-spec/auth-service-spec.md` (VIII. Object Storage Note)
- `docs/engineering_rules/` (object storage policy neu co)
