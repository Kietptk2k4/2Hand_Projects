# Mobile Master Context - 2Hands

Version: 1.2  
Owner: Mobile Team  
Purpose: Single source of truth for AI implementing the native Expo app.

---

## 1) Fixed Tech Stack (required)

- Expo SDK 56
- React Native
- JavaScript only (no TypeScript)
- expo-router (file-based routing)
- axios for HTTP
- expo-secure-store for JWT refresh token storage

Implementation rules:

- Screen files use `.jsx` under `app/`
- Shared logic under `src/features/` and `src/services/`
- No API calls directly inside JSX event handlers beyond calling hooks/api functions
- Read Expo v56 docs: https://docs.expo.dev/versions/v56.0.0/

---

## 2) Priority Documents (read before coding)

### 2.1 Mobile engineering rules (always)

- `mobile/docs/mobile-master-context.md` (this file)
- `mobile/docs/mobile-convention.md`
- `mobile/docs/mobile-api-integration.md`
- `mobile/docs/mobile-design-system.md`
- `mobile/AGENTS.md` — agent entry point

### 2.2 Social module (when implementing social)

Read in order:

| # | Document | Purpose |
|---|----------|---------|
| 1 | `mobile/docs/mobile-social-scope.md` | MVP in/out of scope |
| 2 | `mobile/docs/mobile-social-ui-map.md` | Routes, components, stitch refs |
| 3 | `mobile/docs/mobile-social-implementation-order.md` | Phases 0–8, file checklist |
| 4 | `mobile/docs/mobile-social-screen-checklist.md` | DoD per screen |
| 5 | `mobile/docs/mobile-social-rn-adaptations.md` | Web → RN (FlatList, picker, modals) |

Social API contracts (shared with web): `docs/api_fe_behavior/social_api_fe_behavior/`

Web implementation reference: `frontend/src/fe-module/features/social/`

### 2.3 Account module (when implementing account settings)

**Social profile ≠ account settings.** `(tabs)/profile` shows posts and follow stats. Account settings (`/account`) is where users edit bio, avatar, privacy, and delete account — same as web `/account-profile`.

Read in order:

| # | Document | Purpose |
|---|----------|---------|
| 1 | `mobile/docs/mobile-account-scope.md` | MVP in/out of scope |
| 2 | `mobile/docs/mobile-account-ui-map.md` | Routes, components, stitch refs |
| 3 | `mobile/docs/mobile-account-implementation-order.md` | Phases 0–9, file checklist |
| 4 | `mobile/docs/mobile-account-screen-checklist.md` | DoD per screen |
| 5 | `mobile/docs/mobile-account-rn-adaptations.md` | Web → RN (hub menu, picker, forms) |

Account API contracts (shared with web): `docs/api_fe_behavior/auth_api_fe_behavior/`

Web implementation reference: `frontend/src/fe-module/features/auth/account/` + `pages/AccountPage.jsx`

### 2.4 Repo-wide references

- `docs/engineering_rules/api-standard.md` — response envelope
- `docs/api_fe_behavior/*` — per-endpoint contract (same as web)
- `frontend/src/fe-module/` — port business flow and API mapping from web

### 2.5 Backend services (no changes from mobile)

| Service | Port (local) | API prefix |
|---------|--------------|------------|
| auth-service | 3001 | `/api/v1/auth`, `/api/v1/users` |
| social-service | 3002 | `/api/v1/social/*` |
| commerce-service | 3003 | `/commerce/api/v1/*` |
| notification-service | 3005 | `/api/v1/notification/*` |

Admin console is web-only for MVP.

---

## 3) Folder Structure

```text
mobile/
├── app/                    # expo-router screens
│   ├── (auth)/             # login, etc.
│   ├── (tabs)/             # authenticated shell (feed, shop, profile)
│   ├── account/            # account settings hub + child screens (to build)
│   ├── post/               # create, detail, edit
│   ├── profile/            # social profile, followers, following
│   └── ...                 # saved, search, hashtag, suggestions
├── src/
│   ├── features/
│   │   ├── auth/           # login API + account settings (extend)
│   │   └── social/         # feed, post, profile, discovery
│   ├── services/http/      # authApiClient, socialApiClient, refresh, unwrap
│   ├── services/auth/      # secure token storage
│   └── shared/theme/       # colors, spacing tokens
└── docs/                   # mobile conventions + social + account specs
```

---

## 4) Profile vs account (do not confuse)

| | Social profile | Account settings |
|--|----------------|------------------|
| **Purpose** | View posts, follow, public identity | Manage own account data |
| **Web** | `/social/users/:userId` | `/account-profile` |
| **Mobile** | `(tabs)/profile` → `ProfileScreen` | `/account` hub (to build) |
| **Service** | social-service + auth public profile | auth-service `/api/v1/users/me` |
| **"Chỉnh sửa hồ sơ"** | Button on self profile → account edit | `EditProfileTab` / `/account/edit` |

---

## 5) Module status

| Module | Status | Entry doc |
|--------|--------|-----------|
| Auth (login) | Done | `src/features/auth/` |
| Social | Done (v1) | `mobile/docs/mobile-social-implementation-order.md` |
| Account settings | Documented, not built | `mobile/docs/mobile-account-implementation-order.md` Phase 0 |
| Commerce | Not started | TBD (port from `frontend/.../commerce/`) |
| Notification | Not started | TBD |

---

## 6) Definition of Done (mobile feature)

- [ ] Screen in `app/` with loading / error / empty states
- [ ] API via `src/features/*/api/` + `apiResponse.js` unwrap
- [ ] Matches `docs/api_fe_behavior` contract
- [ ] UI uses `src/shared/theme/colors.js` and design system doc
- [ ] No secrets logged; tokens in SecureStore only
- [ ] For social: checklist in `mobile-social-screen-checklist.md` completed
- [ ] For account: checklist in `mobile-account-screen-checklist.md` completed
- [ ] List files created/updated in PR or task summary

---

## 7) Prompt Template for AI

### Generic

```text
Implement [SCREEN/FLOW] in mobile/ using Expo 56 + JavaScript + expo-router.

Read first:
- mobile/AGENTS.md
- mobile/docs/mobile-master-context.md
- mobile/docs/mobile-convention.md
- mobile/docs/mobile-api-integration.md
- mobile/docs/mobile-design-system.md
- docs/api_fe_behavior/[relevant]-api-and-behavior.md

Port business logic from frontend/src/fe-module/features/[domain]/ when applicable.
Do not modify backend services unless explicitly requested.
```

### Social screen

```text
Implement Phase [N] / screen [NAME] per mobile/docs/mobile-social-implementation-order.md.

Read:
- mobile/AGENTS.md
- mobile/docs/mobile-social-scope.md
- mobile/docs/mobile-social-ui-map.md
- mobile/docs/mobile-social-screen-checklist.md (section [N])
- mobile/docs/mobile-social-rn-adaptations.md
- docs/api_fe_behavior/social_api_fe_behavior/[API].md

Port from frontend/src/fe-module/features/social/ as listed in ui-map.
```

### Account screen

```text
Implement Phase [N] / screen [NAME] per mobile/docs/mobile-account-implementation-order.md.

Read:
- mobile/AGENTS.md
- mobile/docs/mobile-account-scope.md
- mobile/docs/mobile-account-ui-map.md
- mobile/docs/mobile-account-screen-checklist.md (section [N])
- mobile/docs/mobile-account-rn-adaptations.md
- docs/api_fe_behavior/auth_api_fe_behavior/[API].md

Port from frontend/src/fe-module/features/auth/account/ as listed in ui-map.
Invalidate social profile cache after profile-changing mutations.
```

---

## 8) Conflict Resolution

1. `mobile/docs/mobile-master-context.md`
2. `mobile/docs/mobile-social-scope.md` (for social scope only)
3. `mobile/docs/mobile-account-scope.md` (for account scope only)
4. `mobile/docs/mobile-api-integration.md`
5. `docs/api_fe_behavior/*`
6. `frontend/src/fe-module/` (reference only)
