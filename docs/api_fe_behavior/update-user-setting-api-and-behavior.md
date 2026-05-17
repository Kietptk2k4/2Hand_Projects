# Update User Setting - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for updating user settings in Auth Service.

In scope:
- Update `appearance_mode` for current authenticated user.
- Validate and normalize enum input.
- Persist settings and return updated setting value.

Out of scope:
- Profile content update (`display_name`, `bio`, `website`, `social_links`).
- Avatar/privacy/soft-delete/session management flows.
- Advanced notification settings (not in current auth-service MVP scope).

## 2. Source Docs
- `docs/feature-requirements/auth/FR_UpdateUserSettings.md`
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
- Path: `/api/v1/users/me/settings`
- Auth: Required JWT (`Authorization: Bearer <access_token>`)

### 3.2 Request Body
```json
{
  "appearance_mode": "DARK"
}
```

### 3.3 Validation Rules (BE Authoritative)
- `appearance_mode`:
  - Required, non-blank.
  - Accepted values: `LIGHT`, `DARK`, `SYSTEM`.
  - Backend normalizes input by `trim()` and `toUpperCase()` before enum parse.
  - Example: `" dark "` is accepted and parsed as `DARK`.

### 3.4 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat cai dat thanh cong.",
  "data": {
    "appearance_mode": "DARK"
  },
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 4. Error Handling
- `400 Bad Request`:
  - Missing/blank `appearance_mode`.
  - Invalid enum value.
  - Malformed JSON payload.
- `401 Unauthorized`:
  - Missing/invalid JWT.
  - Invalid principal format.
  - User not found.
  - User status is `DELETED`.
- `404 Not Found`:
  - User settings record not found.
- `500 Internal Server Error`:
  - Unexpected backend/runtime errors.

Example invalid enum response:
```json
{
  "code": 400,
  "success": false,
  "message": "Appearance mode must be LIGHT, DARK or SYSTEM",
  "data": null,
  "errors": [
    {
      "field": "appearance_mode",
      "reason": "INVALID_ENUM"
    }
  ],
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Backend Behavior (Authoritative)

### 5.1 Main Flow
1. Extract current user id from JWT context.
2. Validate and parse `appearance_mode`.
3. Load current user from `USERS`.
4. Ensure user is active for account actions (reject `DELETED`).
5. Load settings from `USER_SETTINGS`.
6. Update `appearance_mode` and `updated_at`.
7. Persist setting update.
8. Return updated `appearance_mode` in response `data`.

### 5.2 Transaction Rule
- Settings update runs in one transactional boundary.
- Any persistence failure should rollback write operation.

Note:
- Current BE implementation for this endpoint does not create outbox event.

## 6. Database Impact
- `USER_SETTINGS`:
  - Update: `appearance_mode`, `updated_at`.
- No direct insert into `OUTBOX_EVENTS` for this flow in current implementation.

## 7. FE Behavior

### 7.1 UI Flow
- User selects theme mode (`LIGHT`, `DARK`, `SYSTEM`) from settings tab.
- FE calls `PATCH /api/v1/users/me/settings`.
- On success:
  - apply returned `appearance_mode` to UI theme state.
  - invalidate/refetch `GET /api/v1/users/me` to keep account data synchronized.

### 7.2 UX Rules
- Disable control/save button while request is pending.
- Show clear field/global error on `400`.
- On `401`:
  - attempt refresh-token flow once via interceptor.
  - if still unauthorized, clear auth state and redirect login.
- On `404`: show settings-unavailable state and retry option.
- On `500`: show generic retry message.

### 7.3 API Mapping Rule
- Keep API payload key as snake_case (`appearance_mode`) at API boundary.
- FE internal state can use camelCase, but must map explicitly before request.

## 8. Security Notes
- Endpoint is protected under `/api/v1/users/me/**`.
- Ownership is enforced by JWT principal (current user only).
- Do not log JWT or sensitive auth headers.

## 9. Acceptance Criteria
- Valid enum request updates `appearance_mode` successfully and returns `200`.
- Returned response `data.appearance_mode` matches persisted value.
- Missing/blank/invalid enum request returns `400`.
- Unauthorized request returns `401`.
- Missing settings record returns `404`.
- API response follows standard envelope `code/success/message/data/errors/timestamp`.

## 10. Prompt for Stitch (UI only)
```text
Create a User Settings (Theme Mode) UI for 2Hands.

API:
- PATCH /api/v1/users/me/settings
- Auth: Bearer token
- Request: { "appearance_mode": "LIGHT|DARK|SYSTEM" }

Requirements:
- Theme selector with 3 options: LIGHT, DARK, SYSTEM
- Submit/loading state while saving
- Apply updated appearance_mode from response after success
- Handle 400 validation errors, 401 redirect login, 404 settings not found, 500 retry
- Responsive and accessible design
```
