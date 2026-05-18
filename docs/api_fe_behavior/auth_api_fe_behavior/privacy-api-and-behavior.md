# Privacy - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the private profile toggle feature in Auth Service.

In scope:
- Toggle current user profile privacy flag `is_private`.
- Persist privacy state in `USER_PROFILES`.
- Write outbox event `USER_UPDATED` for cross-service sync.
- Clarify observable impact on public profile response after toggle.

Out of scope:
- Public profile endpoint full contract details (`GET /api/v1/users/{userId}/public-profile`).
- Editing profile content fields (`display_name`, `bio`, `website`, `social_links`).
- Feed visibility policy implementation in downstream services.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_TogglePrivateProfile.md`
- `docs/feature-requirements/auth/FR_ViewPublicUserProfile.md`
- `docs/use-cases/uc-user-profile-management.md`
- `docs/business-flow/profile-privacy-flow.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/api_fe_behavior/ProfileAccount-api-and-behavior.md`
- `docs/api_fe_behavior/ViewPublicUserProfile-api-and-behavior.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/database/auth_schema.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `PATCH`
- Path: `/api/v1/users/me/privacy`
- Auth: Required JWT (`Authorization: Bearer <access_token>`)

### 3.2 Request Body
```json
{
  "is_private": true
}
```

### 3.3 Validation Rules (BE Authoritative)
- `is_private`:
  - Required.
  - Type boolean.
  - `null` is invalid (`@NotNull`).

### 3.4 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat quyen rieng tu thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 4. Error Handling
- `400 Bad Request`:
  - Missing/null `is_private`.
  - Invalid payload format (malformed JSON).
- `401 Unauthorized`:
  - Missing/invalid JWT.
  - Invalid principal format.
  - User not found.
  - User status is `DELETED`.
- `404 Not Found`:
  - User profile record not found.
- `500 Internal Server Error`:
  - Unexpected backend/runtime errors.

Example error payload (`is_private` missing):
```json
{
  "code": 400,
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": [
    {
      "field": "isPrivate",
      "reason": "is_private is required"
    }
  ],
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Backend Behavior (Authoritative)

### 5.1 Main Flow
1. Extract current user id from JWT context.
2. Load current user from `USERS`.
3. Ensure user is active for account actions (reject `DELETED`).
4. Load profile from `USER_PROFILES`.
5. Update `is_private` and `updated_at`.
6. Insert outbox event `USER_UPDATED`.
7. Return HTTP 200.

### 5.2 Transaction Rule
- Privacy update + outbox insert run in one ACID transaction.
- Any failure rolls back all writes.

## 6. Database and Event Impact
- `USER_PROFILES`:
  - Update: `is_private`, `updated_at`.
- `OUTBOX_EVENTS`:
  - Insert event:
    - `event_type = USER_UPDATED`
    - `status = PENDING`
    - payload includes `user_id`, `email`, `updated_at`.

## 7. Privacy Effect on Public Profile
After toggling privacy, behavior of `GET /api/v1/users/{userId}/public-profile` is:
- If `is_private = false`:
  - return public fields: `display_name`, `avatar_url`, `bio`, `website`, `social_links`.
- If `is_private = true`:
  - still return `display_name`, `avatar_url`, `is_private`.
  - mask details:
    - `bio = null`
    - `website = null`
    - `social_links = {}`

This masking behavior is implemented in BE public-profile use case and should be reflected immediately after privacy toggle success.

## 8. FE Behavior

### 8.1 UI Flow
- Provide a toggle/switch bound to `is_private`.
- On toggle submit, call `PATCH /api/v1/users/me/privacy` with current boolean state.
- After success:
  - show success toast/message.
  - refetch `GET /api/v1/users/me` to sync account screen state.
  - optionally refresh public profile preview if screen supports preview mode.

### 8.2 UX Rules
- Disable toggle while request is in progress.
- If update fails, restore previous toggle state in UI.
- On `401`:
  - attempt refresh-token flow once via interceptor.
  - if still unauthorized, clear auth state and redirect login.
- On `404`: show blocking profile-not-found state.
- On `500`: show retry-friendly generic error.

## 9. Security Notes
- Endpoint is protected under `/api/v1/users/me/**`.
- Ownership is implicit via JWT principal (current user only).
- Do not expose sensitive account fields through privacy feature responses.
- Do not log JWT or full profile payload in production logs.

## 10. Acceptance Criteria
- Valid request toggles `is_private` successfully and returns `200`.
- `USER_UPDATED` outbox event is created in same transaction.
- Missing/null `is_private` returns `400`.
- Unauthorized request returns `401`.
- Missing profile record returns `404`.
- Public profile masking reflects new privacy state (`bio`, `website`, `social_links` hidden when private).
- API response follows standard envelope `code/success/message/data/errors/timestamp`.

## 11. Prompt for Stitch (UI only)
```text
Create a Privacy settings UI for 2Hands.

API:
- PATCH /api/v1/users/me/privacy
- Auth: Bearer token
- Request: { "is_private": true|false }

Requirements:
- Toggle switch for private profile mode
- Loading/disabled state while saving
- Revert toggle if request fails
- Handle 400 validation error, 401 redirect login, 404 profile missing, 500 retry
- Show short explanation:
  "When private mode is ON, public viewers only see display name and avatar."
- Responsive and accessible design
```
