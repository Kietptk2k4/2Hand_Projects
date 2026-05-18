# Edit Profile - API and Behavior Spec

## 1. Scope
This document defines backend API contract and frontend behavior for the Edit Profile feature in Auth Service.

In scope:
- Update profile basic information of the current authenticated user.
- Validate payload for `display_name`, `bio`, `website`, and `social_links`.
- Persist profile changes and write outbox event `USER_UPDATED` in one transaction.

Out of scope:
- Avatar upload/update flow (`PATCH /api/v1/users/me/avatar`).
- Privacy toggle, settings update, and account soft-delete.
- Public profile view for other users.

## 2. Source Docs
- `docs/feature-requirements/auth/FR_UpdateProfile.md`
- `docs/use-cases/uc-user-profile-management.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/api_fe_behavior/ProfileAccount-api-and-behavior.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/database/auth_schema.md`

## 3. API Contract

### 3.1 Endpoint
- Method: `PUT`
- Path: `/api/v1/users/me/profile`
- Auth: Required JWT (`Authorization: Bearer <access_token>`)

### 3.2 Request Body
```json
{
  "display_name": "Kiet Tran",
  "bio": "Backend engineer",
  "website": "https://example.com",
  "social_links": {
    "facebook": "https://facebook.com/user",
    "github": "https://github.com/user"
  }
}
```

### 3.3 Validation Rules (BE Authoritative)
- `display_name`:
  - Required, non-blank.
  - Max length 100.
- `bio`:
  - Optional.
  - Max length 500.
- `website`:
  - Optional.
  - If provided, must be valid URL with scheme `http` or `https` and non-empty host.
- `social_links`:
  - Optional object (`map<string,string>`).
  - Max size 10 entries.
  - Each key must be non-blank.
  - Each value must be valid URL (`http/https` + host).

Note:
- Domain layer normalizes optional string fields: blank `bio`/`website` becomes `null`.
- If `social_links` is not provided, backend stores empty object (`{}`).

### 3.4 Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Cap nhat ho so thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 4. Error Handling
- `400 Bad Request`:
  - Bean validation fail (`display_name` missing/blank, too long, `bio` too long).
  - URL format invalid (`website` or `social_links` value).
  - `social_links` over max entries.
  - Invalid request payload (malformed JSON).
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
  "message": "URL format is invalid",
  "data": null,
  "errors": [
    {
      "field": "website",
      "reason": "INVALID_FORMAT"
    }
  ],
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Backend Behavior (Authoritative)

### 5.1 Main Flow
1. Extract current user id from JWT context.
2. Validate request payload (annotation + business validation service).
3. Load current user from `USERS`.
4. Ensure user is active for account actions (reject `DELETED`).
5. Load current profile from `USER_PROFILES`.
6. Update profile basic info:
   - `display_name`
   - `bio`
   - `website`
   - `social_links`
   - `updated_at = now()`
7. Insert outbox event `USER_UPDATED` into `OUTBOX_EVENTS`.
8. Return HTTP 200.

### 5.2 Transaction Rule
- Profile update + outbox insert run in one ACID transaction.
- If any step fails, all writes are rolled back.

## 6. Database Impact
- `USER_PROFILES`:
  - Update: `display_name`, `bio`, `website`, `social_links`, `updated_at`.
- `OUTBOX_EVENTS`:
  - Insert event:
    - `event_type = USER_UPDATED`
    - `status = PENDING`
    - payload includes `user_id`, `email`, `updated_at`.

## 7. FE Behavior

### 7.1 Form Behavior
- Fields:
  - `display_name` (required)
  - `bio` (optional)
  - `website` (optional URL)
  - `social_links` (optional key-value URL pairs)
- FE keeps UI model in camelCase if needed, but must map request keys to snake_case at API boundary.

### 7.2 UX Rules
- Disable submit while mutation is pending.
- Show inline field errors from `errors[]` when backend returns `400`.
- On success (`200`):
  - show success toast/message.
  - invalidate/refetch `GET /api/v1/users/me` to sync account info screen.
- On `401`:
  - attempt refresh-token flow once via interceptor.
  - if still unauthorized, clear auth state and redirect login.
- On `404`:
  - show blocking state "Khong tim thay thong tin ho so".
- On `500`:
  - show generic retry message.

### 7.3 Input Constraints (Client-side mirror)
- `display_name` max 100 chars.
- `bio` max 500 chars.
- Max 10 social links.
- URL validator accepts only `http/https`.

## 8. Security Notes
- Endpoint is protected under `/api/v1/users/me/**`.
- Only current authenticated user can update own profile.
- Do not log JWT or profile payload containing personal links in production logs.

## 9. Acceptance Criteria
- Valid payload updates `USER_PROFILES` successfully and returns `200`.
- `USER_UPDATED` outbox event is created in same transaction.
- Invalid field/payload returns `400` with usable validation detail.
- Unauthorized request returns `401`.
- Missing profile record returns `404`.
- API response follows standard envelope `code/success/message/data/errors/timestamp`.

## 10. Prompt for Stitch (UI only)
```text
Create an Edit Profile form for 2Hands.

API:
- PUT /api/v1/users/me/profile
- Auth: Bearer token

Fields:
- display_name (required, max 100)
- bio (optional, max 500)
- website (optional, http/https URL)
- social_links (optional key-value URLs, max 10 items)

UX:
- Inline validation
- Submit loading state
- Success toast and refresh account data
- Handle 400 field errors, 401 redirect login, 404 profile not found, 500 generic retry
- Responsive and accessible design
```
