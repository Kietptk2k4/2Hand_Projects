# 2Hands Mobile — Agent Instructions

Before writing any code, read:

1. `mobile/docs/mobile-master-context.md`
2. `mobile/docs/mobile-convention.md`
3. `mobile/docs/mobile-api-integration.md`
4. `mobile/docs/mobile-design-system.md`

Expo SDK 56 docs: https://docs.expo.dev/versions/v56.0.0/

---

## Social module (read when implementing social screens)

Read **in this order**:

1. `mobile/docs/mobile-social-scope.md` — what is in/out of MVP
2. `mobile/docs/mobile-social-ui-map.md` — web page/modal → mobile route
3. `mobile/docs/mobile-social-implementation-order.md` — build phases
4. `mobile/docs/mobile-social-screen-checklist.md` — DoD per screen
5. `mobile/docs/mobile-social-rn-adaptations.md` — web → React Native patterns

API contracts: `docs/api_fe_behavior/social_api_fe_behavior/` (shared with web, do not duplicate).

Web reference: `frontend/src/fe-module/features/social/`

Start social work at **Phase 0** in `mobile-social-implementation-order.md` (`socialApiClient` + feed API) unless the task specifies a later phase.

---

## Account module (read when implementing account settings)

**Not the same as social profile.** Tab **Hồ sơ** = `ProfileScreen` (posts, follow). Account settings = `/account` hub (edit bio, avatar, privacy, delete).

Read **in this order**:

1. `mobile/docs/mobile-account-scope.md` — what is in/out of MVP
2. `mobile/docs/mobile-account-ui-map.md` — web tab → mobile route
3. `mobile/docs/mobile-account-implementation-order.md` — build phases
4. `mobile/docs/mobile-account-screen-checklist.md` — DoD per screen
5. `mobile/docs/mobile-account-rn-adaptations.md` — web → React Native patterns

API contracts: `docs/api_fe_behavior/auth_api_fe_behavior/` (shared with web, do not duplicate).

Web reference: `frontend/src/fe-module/features/auth/account/` + `AccountPage.jsx`

Start account work at **Phase 0** in `mobile-account-implementation-order.md` (extend `authApi.js` + `accountSchemas`) unless the task specifies a later phase.

After profile / avatar mutations: invalidate social profile cache for current user (`profileKeys`, `fetchPublicUserProfile`).

---

## Auth module (existing)

- Login: `app/(auth)/login.jsx`
- API: `src/features/auth/api/authApi.js` — extend for account (see account implementation order)
- Tokens: `src/services/auth/tokenStorage.js` (expo-secure-store)
- HTTP: `src/services/http/authApiClient.js`

---

## Porting from web

Port API/business logic from `frontend/src/fe-module/features/{domain}/` when a web equivalent exists.

API behavior specs live in `docs/api_fe_behavior/` at repo root.

---

## Rules

- JavaScript only (no TypeScript)
- expo-router for navigation
- No axios/fetch in `app/*.jsx` — use `src/features/*/api/`
- JWT refresh token in expo-secure-store only
- All `*.js`, `*.jsx`, `*.md` must be **UTF-8 without BOM** (Windows)
- Do not modify backend services unless explicitly requested

---

## Prompt template (social screen)

```text
Implement [SCREEN] in mobile/ per mobile/docs/mobile-social-screen-checklist.md (section [N]).

Read:
- mobile/AGENTS.md
- mobile/docs/mobile-social-scope.md
- mobile/docs/mobile-social-ui-map.md
- mobile/docs/mobile-social-rn-adaptations.md
- docs/api_fe_behavior/social_api_fe_behavior/[API].md

Port from frontend/src/fe-module/features/social/ as mapped in ui-map.
UTF-8 files only. No API calls in app/*.jsx.
```

---

## Prompt template (account screen)

```text
Implement [SCREEN] in mobile/ per mobile/docs/mobile-account-screen-checklist.md (section [N]).

Read:
- mobile/AGENTS.md
- mobile/docs/mobile-account-scope.md
- mobile/docs/mobile-account-ui-map.md
- mobile/docs/mobile-account-rn-adaptations.md
- docs/api_fe_behavior/auth_api_fe_behavior/[API].md

Port from frontend/src/fe-module/features/auth/account/ as mapped in ui-map.
UTF-8 files only. No API calls in app/*.jsx.
Invalidate social profile cache after profile-changing mutations.
```
