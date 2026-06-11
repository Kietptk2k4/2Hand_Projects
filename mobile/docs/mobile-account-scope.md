# Mobile Account Scope - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Define what the native account settings module includes, excludes, and defers — so AI does not confuse it with social profile or over-build unfinished web UI.

---

## 1) Context

- **Web reference (complete):** `frontend/src/fe-module/features/auth/account/` + `AccountPage.jsx`
- **API contracts (shared web + mobile):** `docs/api_fe_behavior/auth_api_fe_behavior/`
- **Functional requirements:** `docs/feature_requirements/auth/` (`FR_ViewAccount.md`, `FR_UpdateProfile.md`, `FR_TogglePrivateProfile.md`, `FR_SoftDeleteAccount.md`)
- **Visual reference:** `frontend/stitch/account_setting_screen/`
- **Mobile stack:** Expo SDK 56, JavaScript, expo-router — see `mobile/docs/mobile-master-context.md`

Account settings on web is the **source of truth for business flow and API mapping**. Mobile reimplements UI with React Native primitives; it does not duplicate API behavior docs.

---

## 2) Critical distinction: Social profile vs Account settings

| Concept | Purpose | Web route | Mobile today |
|---------|---------|-----------|--------------|
| **Social profile** | Posts, follow, stats — public identity | `/social/users/:userId` | Done — `(tabs)/profile` → `ProfileScreen` |
| **Account settings** | Edit bio, avatar, privacy, delete account | `/account-profile` | Not implemented |

On web, **"Chỉnh sửa hồ sơ"** on `ProfileHero.jsx` navigates to `/account-profile`, not the social profile page. Mobile must add a separate account stack and entry point (settings icon on self profile).

---

## 3) MVP (v1) — In Scope

### 3.1 Core navigation

| Item | Notes |
|------|-------|
| Account settings hub | Menu list screen at `app/account/index.jsx` |
| Auth gate | All account screens require JWT; 401 → session expired flow |
| Entry from social profile | Self `ProfileScreen` → settings icon → `/account` |
| Tab **Hồ sơ** unchanged | Stays social `ProfileScreen`; do **not** replace with account hub |

### 3.2 Screens and flows (port from web `ACCOUNT_TABS`)

| Flow | Mobile v1 | Web reference |
|------|-----------|---------------|
| Account info (read-only) | Yes | `AccountInfoTab.jsx` |
| Edit profile | Yes | `EditProfileTab.jsx` |
| Update avatar | Yes | `UpdateAvatarTab.jsx` |
| Privacy toggle | Yes | `PrivacyTab.jsx` |
| Settings (appearance mode) | Yes | `SettingsTab.jsx` |
| Delete account (soft-delete) | Yes | `DeleteAccountTab.jsx` |
| Logout from account menu | Yes | Web: header / session — add to mobile account hub |

### 3.3 API layer to extend (in `mobile/src/features/auth/api/`)

| API function | Method / path | Behavior doc |
|--------------|---------------|--------------|
| `getMyProfile` | `GET /api/v1/users/me` | `account-info-api-and-behavior.md`, `ProfileAccount-api-and-behavior.md` |
| `updateMyProfile` | `PUT /api/v1/users/me/profile` | `edit-profile-api-and-behavior.md` |
| `requestAvatarUploadUrl` | `POST /api/v1/users/me/avatar/upload-url` | `AvatarUpload-api-and-behavior.md` |
| `updateMyAvatar` | `PATCH /api/v1/users/me/avatar` | `update-avatar-api-and-behavior.md` |
| `updateMyPrivacy` | `PATCH /api/v1/users/me/privacy` | `privacy-api-and-behavior.md` |
| `updateMySettings` | `PATCH /api/v1/users/me/settings` | `update-user-setting-api-and-behavior.md` |
| `softDeleteMyAccount` | `POST /api/v1/users/me/soft-delete` | `soft-delete-account-api-and-behavior.md` |
| `logoutWithRefreshToken` | `POST /api/v1/auth/logout` | `Logout-api-and-behavior.md` — **exists** |

Shared HTTP client: `mobile/src/services/http/authApiClient.js` (already exists).

### 3.4 Validation and schemas (port from web)

Port `frontend/src/fe-module/features/auth/account/accountSchemas.js` to `mobile/src/features/auth/utils/accountSchemas.js`:

- `validateEditProfileForm` — display_name required, max 100; bio max 500; website URL; social_links max 10
- `SOCIAL_PLATFORMS`, `mapSocialLinksToRows`, `mapSocialLinksToObject`
- `AVATAR_MAX_BYTES` (5 MB), `AVATAR_ALLOWED_TYPES` (jpeg, png, webp)

### 3.5 Cache invalidation after writes

After profile / avatar / privacy / settings update:

- Invalidate `accountKeys.me` (React Query)
- Invalidate social profile cache for current user (`profileKeys`, `fetchPublicUserProfile` used by `ProfileScreen`)

### 3.6 Non-functional requirements (v1)

- Loading, error (+ retry), and success feedback on every screen
- JWT via existing auth refresh flow; no tokens or passwords in logs
- Field mapping: support `snake_case` from backend per API behavior docs
- UI tokens from `mobile/src/shared/theme/colors.js` and `mobile/docs/mobile-design-system.md`
- Vietnamese copy matches web where specified in screen checklist

---

## 4) Out of Scope (v1)

Do **not** implement in mobile v1 unless explicitly requested:

| Item | Reason |
|------|--------|
| **Notifications tab** | Web uses `NotificationSettingsTab` from notification module — mobile notification module not ready |
| **Account security / sessions** | Web route `/account/security` — separate feature (login sessions list) |
| **Change password** | Web route `/account/password` — separate page; defer to Phase 8 |
| **Register, forgot password, verify email** | Auth onboarding flows — not part of account settings hub |
| **OAuth linking** | Web OAuth buttons on register/login — not account settings |
| **Admin user management** | Web-only admin console |
| **Backend / Kafka / outbox** | Server-side only |

### v1 UX for deferred links

If account info screen shows links to change password or security (like web `AccountInfoTab`):

1. Show the row with label, and
2. Navigate only when Phase 8 screen exists; until then show **"Tính năng đang được phát triển."** or hide the row.

Do not invent alternate password or session APIs.

---

## 5) Phase 2 (defer)

| Feature | Notes |
|---------|-------|
| Change password screen | Port `ChangePasswordTab.jsx` + `POST /api/v1/auth/change-password` |
| Account security (active sessions) | Port session list from web `/account/security` |
| Notification preferences | After mobile notification module ships |
| Email verification resend | If product adds to account info |
| Biometric / local auth lock | Not on web MVP |

---

## 6) Dependencies on other mobile modules

| Module | Dependency |
|--------|------------|
| **Auth** | Login, `authApiClient`, token storage, session clear on delete/logout |
| **Social** | Self profile header must refresh after edit profile / avatar; entry point from `ProfileScreen` |
| **Notification** | Optional tab in account hub — defer until notification module exists |

**Prerequisite before account work:** working login + token storage + `authApiClient` refresh (`src/features/auth/`, `src/services/auth/tokenStorage.js`).

**Social profile (Phase 6) should exist** so cache invalidation and navigation entry can be wired.

---

## 7) Environment

```env
EXPO_PUBLIC_AUTH_SERVICE_BASE_URL=http://10.0.2.2:3001   # Android emulator
```

Local dev requires **auth-service** on port **3001**. Social profile preview may also need **social-service** on **3002**.

---

## 8) Definition of Done (account feature)

- [ ] Screen listed in `mobile/docs/mobile-account-ui-map.md` exists under `app/account/`
- [ ] Business logic in `src/features/auth/` (api + hooks + components + utils)
- [ ] Matches relevant `docs/api_fe_behavior/auth_api_fe_behavior/*` contract
- [ ] Loading / error / success states per screen checklist
- [ ] No axios calls in `app/*.jsx`
- [ ] Social profile cache invalidated after profile-changing operations
- [ ] UTF-8 encoding on all new files (Windows)

---

## 9) Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-account-ui-map.md` | Web tab → mobile route mapping |
| `mobile/docs/mobile-account-implementation-order.md` | Build sequence and file checklist |
| `mobile/docs/mobile-account-screen-checklist.md` | DoD per screen |
| `mobile/docs/mobile-account-rn-adaptations.md` | Web → React Native patterns |
| `mobile/docs/mobile-convention.md` | Naming and folder rules |
| `mobile/docs/mobile-api-integration.md` | Response unwrap, 401 refresh |
| `mobile/docs/mobile-design-system.md` | Colors, typography, spacing |
