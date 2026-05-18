# ProfileAccount - API and Behavior Spec

## 1. Scope
This document defines backend API contracts and frontend behavior for one Profile/Account screen in Auth Service with 6 vertical tabs:
- Account Info
- Edit Profile
- Update Avatar
- Privacy
- Settings
- Delete Account

In scope:
- Self account/profile/settings read and update
- Avatar URL update flow (MinIO URL is input)
- Privacy toggle
- Soft delete account with session invalidation

Out of scope:
- Public profile viewing of other users
- Binary avatar upload stream handling
- Synchronous MinIO object deletion during soft-delete transaction

## 2. Source Docs
- `docs/feature-requirements/auth/FR_ViewAccount.md`
- `docs/feature-requirements/auth/FR_UpdateProfile.md`
- `docs/feature-requirements/auth/FR_UpdateAvatar.md`
- `docs/feature-requirements/auth/FR_TogglePrivateProfile.md`
- `docs/feature-requirements/auth/FR_UpdateUserSettings.md`
- `docs/feature-requirements/auth/FR_SoftDeleteAccount.md`
- `docs/use-cases/uc-user-profile-management.md`
- `docs/business-flow/profile-privacy-flow.md`
- `docs/business-spec/auth-service-spec.md`
- `docs/database/auth_schema.md`
- `docs/engineering-rules/api-standard.md`
- `docs/engineering-rules/backend-convention.md`

## 3. Screen & Tab Mapping
One single screen with right-side vertical tabs:
1. **Account Info** -> `GET /api/v1/users/me`
2. **Edit Profile** -> `PUT /api/v1/users/me/profile`
3. **Update Avatar** -> `PATCH /api/v1/users/me/avatar`
4. **Privacy** -> `PATCH /api/v1/users/me/privacy`
5. **Settings** -> `PATCH /api/v1/users/me/settings`
6. **Delete Account** -> `POST /api/v1/users/me/soft-delete`

Auth rule:
- All 6 endpoints require JWT (`Authorization: Bearer <access_token>`).

## 4. API Contracts by Tab

### Tab 1 - Account Info
#### Endpoint
- Method: `GET`
- Path: `/api/v1/users/me`

#### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Lay thong tin tai khoan thanh cong.",
  "data": {
    "user": {
      "id": "uuid",
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

### Tab 2 - Edit Profile
#### Endpoint
- Method: `PUT`
- Path: `/api/v1/users/me/profile`

#### Request Body
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

#### Validation
- `display_name`: required, max 100
- `bio`: optional, max 500
- `website`: optional, valid http/https URL
- `social_links`: optional object, each value valid http/https URL

#### Success Response (200)
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

### Tab 3 - Update Avatar
#### Endpoint
- Method: `PATCH`
- Path: `/api/v1/users/me/avatar`

#### Request Body
```json
{
  "avatar_url": "https://minio.example.com/2hands-avatar/avatars/user-123.png"
}
```

#### Validation
- `avatar_url`: required, valid http/https URL

#### Success Response (200)
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

### Tab 4 - Privacy
#### Endpoint
- Method: `PATCH`
- Path: `/api/v1/users/me/privacy`

#### Request Body
```json
{
  "is_private": true
}
```

#### Success Response (200)
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

### Tab 5 - Settings
#### Endpoint
- Method: `PATCH`
- Path: `/api/v1/users/me/settings`

#### Request Body
```json
{
  "appearance_mode": "DARK"
}
```

#### Validation
- `appearance_mode`: required, enum `LIGHT | DARK | SYSTEM`

#### Success Response (200)
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

### Tab 6 - Delete Account
#### Endpoint
- Method: `POST`
- Path: `/api/v1/users/me/soft-delete`

#### Request Body
```json
{
  "password": "CurrentPassword123!"
}
```

#### Success Response (200)
```json
{
  "code": 200,
  "success": true,
  "message": "Xoa tai khoan thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Error Handling (All Tabs)
- `400`: validation/business errors (invalid URL, invalid enum, wrong password, payload invalid)
- `401`: missing/invalid JWT
- `500`: internal error

Example error:
```json
{
  "code": 400,
  "success": false,
  "message": "Mat khau khong chinh xac.",
  "data": null,
  "errors": [
    {
      "field": "password",
      "reason": "INVALID_CREDENTIAL"
    }
  ],
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 6. Backend Behavior Summary per Tab
- **Account Info:** Read combined data from `USERS`, `USER_PROFILES`, `USER_SETTINGS`.
- **Edit Profile:** Update profile basic fields, then write outbox event `USER_UPDATED`.
- **Update Avatar:** Update `USER_PROFILES.avatar_url`, then write outbox event `USER_UPDATED`.
- **Privacy:** Update `USER_PROFILES.is_private`, then write outbox event `USER_UPDATED`.
- **Settings:** Update `USER_SETTINGS.appearance_mode`.
- **Delete Account:** In one transaction:
  - set `USERS.status = DELETED`, `deleted_at = now`
  - revoke all ACTIVE refresh sessions
  - write outbox event `USER_DELETED`

## 7. MinIO Avatar Note
- Auth-service stores only `avatar_url` in `USER_PROFILES`.
- Avatar file/object is stored in MinIO.
- Recommended FE flow:
  1. FE uploads file to MinIO (presigned URL or upload gateway).
  2. FE calls `PATCH /api/v1/users/me/avatar` with resulting `avatar_url`.
- Soft-delete does not require synchronous MinIO object deletion inside DB transaction; handle deletion asynchronously via event/job if needed.

## 8. FE Behavior (One Screen - Six Vertical Tabs)
### Layout
- One Profile/Account page.
- Content panel on left, vertical tab menu on right with 6 tabs:
  - Account Info
  - Edit Profile
  - Update Avatar
  - Privacy
  - Settings
  - Delete Account

### UX Rules
- On screen load: call `GET /api/v1/users/me` once and populate all tab forms.
- Each tab has independent submit button and loading state.
- Show inline validation errors per field for `400`.
- On `401`: clear local auth and redirect to login.
- On `500`: show generic retry message.

### Tab-specific FE behavior
- **Account Info:** read-only display block.
- **Edit Profile:** editable form; prevent submit if invalid URL/social link.
- **Update Avatar:** show current avatar preview and allow URL update from MinIO flow.
- **Privacy:** toggle switch for `is_private`.
- **Settings:** radio/select for `appearance_mode`.
- **Delete Account:** danger zone; require password confirmation and confirmation dialog.

## 9. Acceptance Criteria
- All 6 endpoints are JWT-protected.
- Account Info returns combined account/profile/settings data.
- Profile/Avatar/Privacy updates persist and write `USER_UPDATED` outbox event.
- Settings update persists `appearance_mode` with enum validation.
- Soft-delete sets user to `DELETED`, revokes active sessions, and writes `USER_DELETED` outbox event in one transaction.
- API responses follow standard envelope: `code/success/message/data/errors/timestamp`.

## 10. Prompt for Stitch (UI only)
```text
Create one Profile/Account screen for 2Hands with right-side vertical tabs:
1) Account Info
2) Edit Profile
3) Update Avatar
4) Privacy
5) Settings
6) Delete Account

Requirements:
- Load data from GET /api/v1/users/me
- Forms map to APIs:
  - PUT /api/v1/users/me/profile
  - PATCH /api/v1/users/me/avatar
  - PATCH /api/v1/users/me/privacy
  - PATCH /api/v1/users/me/settings
  - POST /api/v1/users/me/soft-delete
- Show per-tab loading states and inline field errors
- Handle 400/401/500
- For avatar flow: assume FE gets MinIO URL first, then sends avatar_url to backend
- Responsive and accessible layout
```
