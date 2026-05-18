# Update Avatar - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the Update Avatar feature in Auth Service.

In scope:
- Accept validated `avatar_url` for current authenticated user.
- Update `USER_PROFILES.avatar_url`.
- Write outbox event `USER_UPDATED` in the same transaction.

Out of scope:
- Binary file upload stream handling in this endpoint.
- MinIO bucket provisioning/policy management.
- Deleting old avatar object synchronously during this API call.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_UpdateAvatar.md`
- `docs/use-cases/uc-user-profile-management.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/api_fe_behavior/ProfileAccount-api-and-behavior.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/database/auth_schema.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `PATCH`
- Path: `/api/v1/users/me/avatar`
- Auth: Required JWT (`Authorization: Bearer <access_token>`)

### 3.2 Request Body
```json
{
  "avatar_url": "https://minio.example.com/2hands-avatar/avatars/user-123.png"
}
```

### 3.3 Validation Rules (BE Authoritative)
- `avatar_url`:
  - Required, non-blank.
  - Must be valid URL.
  - Allowed scheme: `http` or `https`.
  - URL host must exist (non-empty).

### 3.4 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat avatar thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 4. Error Handling
- `400 Bad Request`:
  - `avatar_url` missing/blank.
  - Invalid `avatar_url` format or unsupported scheme.
  - Malformed JSON payload.
- `401 Unauthorized`:
  - Missing/invalid JWT.
  - Invalid principal format.
  - User not found.
  - User status is `DELETED`.
- `404 Not Found`:
  - User profile record not found.
- `500 Internal Server Error`:
  - Unexpected backend/runtime errors.

Example validation error response:
```json
{
  "code": 400,
  "success": false,
  "message": "Avatar URL format is invalid",
  "data": null,
  "errors": [
    {
      "field": "avatar_url",
      "reason": "INVALID_FORMAT"
    }
  ],
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Backend Behavior (Authoritative)

### 5.1 Main Flow
1. Extract current user id from JWT context.
2. Validate request payload (`avatar_url` required and valid URL).
3. Load current user from `USERS`.
4. Ensure user is active for account actions (reject `DELETED`).
5. Load profile from `USER_PROFILES`.
6. Update profile avatar URL and `updated_at`.
7. Insert outbox event `USER_UPDATED`.
8. Return HTTP 200.

### 5.2 Transaction Rule
- Avatar update + outbox insert run in one ACID transaction.
- Any failure rolls back all writes.

## 6. Database Impact
- `USER_PROFILES`:
  - Update: `avatar_url`, `updated_at`.
- `OUTBOX_EVENTS`:
  - Insert event:
    - `event_type = USER_UPDATED`
    - `status = PENDING`
    - payload includes `user_id`, `email`, `updated_at`.

## 7. MinIO Integration Boundary
- Auth-service only stores `avatar_url`.
- FE is responsible for upload step before calling this API:
  1. Upload image to MinIO (presigned URL or upload gateway).
  2. Receive final file URL.
  3. Call `PATCH /api/v1/users/me/avatar` with `avatar_url`.
- This endpoint does not process multipart file upload.

## 8. FE Behavior

### 8.1 UI Flow
- Show current avatar preview from account/profile data.
- User selects image and completes upload flow to MinIO.
- FE sends resulting `avatar_url` to backend endpoint.
- On success, invalidate/refetch `GET /api/v1/users/me` to refresh profile UI.

### 8.2 UX Rules
- Disable submit while request is pending.
- Show inline/global error based on backend `errors[]` and `message`.
- On `401`:
  - attempt refresh-token flow once via interceptor.
  - if refresh fails, clear auth state and redirect login.
- On `404`: show blocking message that profile data is unavailable.
- On `500`: show generic retry message.

### 8.3 Client-side Checks
- Basic URL validation before submit (must be `http/https`).
- Prevent empty `avatar_url`.

## 9. Security Notes
- Endpoint is protected under `/api/v1/users/me/**`.
- User can update only own avatar via JWT context.
- Do not log JWT, presigned upload URLs, or sensitive upload metadata in production.
- Prefer HTTPS avatar URL in production environments.

## 10. Acceptance Criteria
- Valid `avatar_url` updates `USER_PROFILES.avatar_url` and returns `200`.
- Outbox event `USER_UPDATED` is created in the same transaction.
- Invalid or missing `avatar_url` returns `400` with usable error detail.
- Unauthorized request returns `401`.
- Missing profile record returns `404`.
- API response follows standard envelope `code/success/message/data/errors/timestamp`.

## 11. Prompt for Stitch (UI only)
```text
Create an Update Avatar UI for 2Hands.

API:
- PATCH /api/v1/users/me/avatar
- Auth: Bearer token
- Request: { "avatar_url": "https://..." }

UX requirements:
- Show current avatar preview
- Support flow: upload image to MinIO first, then call backend with avatar_url
- Submit loading state
- Handle 400 field/global errors, 401 redirect login, 404 profile missing, 500 retry
- Refresh account data after success
- Responsive and accessible design
```
