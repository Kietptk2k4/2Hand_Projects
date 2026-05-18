# Account Info - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the Account Info feature in Auth Service.

In scope:
- Load current authenticated user account information from one endpoint.
- Render account identity, profile, and appearance settings in read-only mode.
- Handle auth errors and fallback UX states when account data cannot be loaded.

Out of scope:
- Editing profile fields (handled by `PUT /api/v1/users/me/profile`).
- Updating avatar (handled by `PATCH /api/v1/users/me/avatar`).
- Privacy, settings update, soft-delete, session management actions.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ViewAccount.md`
- `docs/use-cases/uc-user-profile-management.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/api_fe_behavior/ProfileAccount-api-and-behavior.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/database/auth_schema.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `GET`
- Path: `/api/v1/users/me`
- Auth: Required JWT (`Authorization: Bearer <access_token>`)

### 3.2 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay thong tin tai khoan thanh cong.",
  "data": {
    "user": {
      "id": "f8f7f932-6511-42f8-a0ae-a17c0282d6fe",
      "email": "user@example.com",
      "status": "ACTIVE",
      "email_verified": true,
      "phone": null,
      "last_login_at": "2026-05-16T10:00:00Z"
    },
    "profile": {
      "display_name": "Kiet Tran",
      "avatar_url": "https://minio.example.com/2hands-avatar/avatars/u1.png",
      "bio": "Backend engineer",
      "website": "https://example.com",
      "social_links": {
        "github": "https://github.com/user"
      },
      "is_private": false
    },
    "settings": {
      "appearance_mode": "SYSTEM"
    }
  },
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

### 3.3 Response Mapping Notes
- `data.user.id`: UUID string from authenticated account.
- `data.user.email`: normalized account email.
- `data.user.status`: `ACTIVE | PENDING_VERIFICATION | SUSPENDED | DELETED` (actual value from DB record).
- `data.user.email_verified`: boolean verification flag.
- `data.user.phone`: currently `null` in existing BE implementation.
- `data.user.last_login_at`: latest successful login timestamp or `null`.
- `data.profile.social_links`: JSON object map (`key -> URL`), can be empty.
- `data.settings.appearance_mode`: `LIGHT | DARK | SYSTEM`.

## 4. Error Handling
- `401 Unauthorized`:
  - Missing/invalid JWT.
  - Auth principal is not valid UUID.
  - User does not exist.
  - User is already `DELETED`.
- `404 Not Found`:
  - User profile or user settings record not found.
- `500 Internal Server Error`:
  - Unexpected backend error.

Example error payload:
```json
{
  "code": 401,
  "success": false,
  "message": "Yeu cau dang nhap.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Backend Behavior Summary
- Controller extracts user id from JWT principal and calls `ViewAccountUseCase`.
- Use case loads data from 3 sources:
  - `USERS` (identity + auth-related metadata)
  - `USER_PROFILES` (public profile fields)
  - `USER_SETTINGS` (appearance mode)
- API always returns standard envelope: `code/success/message/data/errors/timestamp`.
- No write operation or outbox event is triggered for this endpoint.

## 6. FE Behavior

### 6.1 Load Flow
- On opening Account Info screen/tab, call `GET /api/v1/users/me`.
- Show skeleton/loading placeholders while request is in progress.
- Render all fields in read-only mode after success.

### 6.2 Display Rules
- Show `email`, `display_name`, `avatar_url` preview, `bio`, `website`, `social_links`, `appearance_mode`.
- If nullable fields are `null` (`phone`, `last_login_at`, `bio`, `website`), show placeholder text (for example: `Chua cap nhat`).
- Keep raw enum values for status/appearance or map to localized labels in UI layer.

### 6.3 Error UX
- `401`: run refresh-token flow once (via interceptor). If still unauthorized, clear session and redirect to login.
- `404`: show blocking state "Thong tin tai khoan chua san sang" with retry action.
- `500`: show generic error toast/banner and allow retry.

## 7. FE Integration Checklist
- Use centralized API client and always parse from response envelope `data`.
- Do not assume `phone` is always available.
- Keep request key/response key naming in snake_case at API boundary.
- Do not log tokens or sensitive account payloads in production logs.

## 8. Acceptance Criteria
- Endpoint `GET /api/v1/users/me` is called with Bearer token.
- FE renders combined `user + profile + settings` from a single response.
- FE gracefully handles nullable fields without UI crash.
- `401/404/500` states are handled with expected UX behavior.
- API contract follows project response envelope standard.

## 9. Prompt for Stitch (UI only)
```text
Create an Account Info screen for 2Hands (read-only).

Data source:
- GET /api/v1/users/me

Display sections:
1) Account Identity: email, status, email_verified, last_login_at
2) Profile: display_name, avatar_url, bio, website, social_links
3) Settings Snapshot: appearance_mode

UX requirements:
- Loading skeleton while fetching
- Safe placeholders for null fields
- Handle 401 (redirect login), 404 (blocking empty state), 500 (retry)
- Responsive and accessible layout
```
