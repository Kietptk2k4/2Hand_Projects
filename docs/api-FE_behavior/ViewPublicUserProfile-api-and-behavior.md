# ViewPublicUserProfile - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the feature "View Public User Profile" in Auth Service.

In scope:
- Read public profile of another user by `userId`.
- Apply privacy masking based on `is_private`.

Out of scope:
- Editing profile/privacy.
- Viewing self account aggregate data (`/users/me`).
- Admin-only override to read private details.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ViewPublicUserProfile.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/ProfileAccount-api-and-behavior.md`
- `docs/business-flow/profile-privacy-flow.md`
- `docs/use-cases/uc-user-profile-management.md`

## 3. Endpoint Contract
### Endpoint
- Method: `GET`
- Path: `/api/v1/users/{userId}/public-profile`
- Auth: Optional (guest is allowed)

### Path Param
- `userId`: required UUID

### Notes
- Invalid UUID format returns `400`.
- If target user is not found or `DELETED`, returns `404`.
- Endpoint never returns `email`, `password_hash`, token/session info.

## 4. Success Response Examples (200)
### Public profile (`is_private = false`)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay public profile thanh cong.",
  "data": {
    "user_id": "uuid",
    "display_name": "Kiet Tran",
    "avatar_url": "https://minio.example.com/2hands-avatar/u1.png",
    "bio": "Backend engineer",
    "website": "https://example.com",
    "social_links": {
      "github": "https://github.com/kiet"
    },
    "is_private": false
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

### Private profile (`is_private = true`)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay public profile thanh cong.",
  "data": {
    "user_id": "uuid",
    "display_name": "Kiet Tran",
    "avatar_url": "https://minio.example.com/2hands-avatar/u1.png",
    "bio": null,
    "website": null,
    "social_links": {},
    "is_private": true
  },
  "errors": null,
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 5. Error Handling (400/404/500)
- `400 Bad Request`:
  - invalid `userId` UUID format.
- `404 Not Found`:
  - target user not found
  - target user has status `DELETED`
  - target profile not found
- `500 Internal Server Error`:
  - unexpected backend/system error

Example 400:
```json
{
  "code": 400,
  "success": false,
  "message": "Du lieu khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "userId",
      "reason": "INVALID_FORMAT"
    }
  ],
  "timestamp": "2026-05-17T10:00:00Z"
}
```

## 6. Backend Behavior Summary
- Parse and validate `userId` path param as UUID.
- Read target `USERS` and `USER_PROFILES`.
- Return `404` when user not found or status is `DELETED`.
- Privacy masking rules:
  - `is_private = false`: return full public fields.
  - `is_private = true`: return only `display_name`, `avatar_url`, `is_private`; force:
    - `bio = null`
    - `website = null`
    - `social_links = {}`
- Build response with standard ApiResponse wrapper.

## 7. FE Behavior
### Public profile render
- Request profile by `userId` from route.
- If 200 and `is_private=false`:
  - show full profile sections (bio, website, social links).

### Private profile notice
- If 200 and `is_private=true`:
  - still show `display_name`, `avatar_url`.
  - show notice: "Tai khoan dang o che do rieng tu."
  - hide detail sections (bio/website/social links).

### Not-found state
- If 404:
  - show "Khong tim thay nguoi dung."
  - provide navigation back action.

### Error state
- If 500:
  - show retry action and generic error message.

## 8. Acceptance Criteria
- Endpoint `GET /api/v1/users/{userId}/public-profile` is publicly accessible (guest allowed).
- Invalid `userId` format returns 400.
- Non-existing or `DELETED` user returns 404.
- Privacy masking is applied exactly as contract.
- Sensitive fields are never exposed.

## 9. Prompt for Stitch (UI only)
```text
Create a Public User Profile page for 2Hands.

API:
- GET /api/v1/users/{userId}/public-profile
- guest access allowed
- response fields:
  user_id, display_name, avatar_url, bio, website, social_links, is_private

Requirements:
- Show loading skeleton while fetching.
- If is_private=false: show full profile details.
- If is_private=true: show avatar + display name + private profile notice; hide bio/website/social links.
- Handle 404 with dedicated "User not found" state.
- Handle 500 with retry UI.
- Responsive and accessible layout.
```
