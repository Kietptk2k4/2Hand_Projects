# Avatar Upload (Presigned URL) - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for issuing a presigned MinIO upload URL for the current user's avatar.

In scope:
- Validate upload intent (`content_type`, `file_size_bytes`).
- Return presigned PUT URL, `object_key`, and public `avatar_url` for follow-up `FR_UpdateAvatar`.
- Rate limit upload URL requests per user.

Out of scope:
- Streaming binary through Auth Service.
- Image crop/resize.
- Deleting old MinIO objects on profile update.

## 2. Source Docs
- `docs/feature_requirements/auth/FR_AvatarUpload.md`
- `docs/api_fe_behavior/auth_api_fe_behavior/update-avatar-api-and-behavior.md`
- `docs/engineering_rules/api-standard.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `POST`
- Path: `/api/v1/users/me/avatar/upload-url`
- Auth: Required JWT (`Authorization: Bearer <access_token>`)

### 3.2 Request Body
```json
{
  "content_type": "image/png",
  "file_size_bytes": 1048576
}
```

### 3.3 Validation Rules (BE Authoritative)
- `content_type`:
  - Required, non-blank.
  - Allowed: `image/jpeg`, `image/png`, `image/webp`.
- `file_size_bytes`:
  - Required, positive integer.
  - Must be `<= max_file_size_bytes` (default 5_242_880 / 5MB).

### 3.4 Success Response (200)
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

## 4. Error Handling
- `400 Bad Request`:
  - Invalid or disallowed `content_type`.
  - `file_size_bytes` missing, non-positive, or over max.
  - Malformed JSON.
- `401 Unauthorized`:
  - Missing/invalid JWT.
  - User not found or not active for account actions.
- `429 Too Many Requests`:
  - Upload URL rate limit exceeded (`AVATAR_UPLOAD_RATE_LIMITED`).
- `503 Service Unavailable`:
  - Object storage disabled or unavailable (`OBJECT_STORAGE_UNAVAILABLE`).

## 5. Backend Behavior (Authoritative)

### 5.1 Main Flow
1. Resolve `user_id` from JWT (ignore any user id in body).
2. Validate `content_type` and `file_size_bytes`.
3. Ensure user exists and is active.
4. Apply per-user rate limit.
5. If object storage is enabled, create presigned PUT for object key `avatars/{user_id}/{uuid}.{ext}`.
6. Return `upload_url`, `object_key`, `avatar_url`, `expires_at`, and policy hints.

### 5.2 Post-Upload Flow (FE)
1. `PUT` file bytes to `upload_url` with matching `Content-Type`.
2. Call `PATCH /api/v1/users/me/avatar` with `avatar_url` from this response.
3. Refetch profile (`GET /api/v1/users/me`) to show new avatar.

### 5.3 Transaction Rule
- No mandatory DB write for issuing URL (read-only / optional audit).

## 6. Security Notes
- Presigned URL is scoped to one object key and short TTL (default 900s).
- Do not log full presigned URLs in production.
- Auth Service never accepts avatar binary on this endpoint.

## 7. FE Behavior

### 7.1 UI Flow
1. User selects image; client checks type/size against `allowed_content_types` and `max_file_size_bytes`.
2. Request upload URL from Auth Service.
3. Upload directly to MinIO via `upload_url`.
4. Persist URL via Update Avatar API.
5. Refresh profile UI.

### 7.2 UX Rules
- Show progress during MinIO PUT.
- On `429`, show retry-after style message.
- On `503`, show temporary unavailable message.
- On `401`, refresh token once; else redirect to login.

## 8. Acceptance Criteria
- Authenticated active user receives valid presigned URL and metadata.
- Invalid type or oversize file returns `400` with field errors when applicable.
- Binary is not sent to Auth Service.
- After MinIO upload + `PATCH /avatar`, profile shows new `avatar_url`.

## 9. Prompt for Stitch (UI only)
```text
Create an Avatar Upload flow for 2Hands.

APIs:
1. POST /api/v1/users/me/avatar/upload-url
   Body: { "content_type": "image/png", "file_size_bytes": 12345 }
2. PUT file to upload_url (MinIO)
3. PATCH /api/v1/users/me/avatar
   Body: { "avatar_url": "<avatar_url from step 1>" }

UX: image picker, size/type validation, upload progress, error handling for 400/401/429/503, refresh profile on success.
```
