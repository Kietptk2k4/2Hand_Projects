# Mobile Account Implementation Order - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Step-by-step build sequence, dependencies, and file checklist so AI implements account settings in the correct order without breaking auth or social profile.

---

## 1) Prerequisites (must be done first)

| Step | Status | Files / notes |
|------|--------|---------------|
| Auth login works | Exists | `app/(auth)/login.jsx`, `src/features/auth/api/authApi.js` |
| Token storage | Exists | `src/services/auth/tokenStorage.js` |
| Auth HTTP client + refresh | Exists | `src/services/http/authApiClient.js` |
| Env auth URL | Configured | `EXPO_PUBLIC_AUTH_SERVICE_BASE_URL` in `.env` |
| auth-service running | Dev | Port **3001** |
| Social profile (self) | Exists | `(tabs)/profile.jsx` → `ProfileScreen` |
| Read account docs | Required | `mobile-account-scope.md`, `mobile-account-ui-map.md`, this file |

**Gate:** User can log in and open self social profile before starting account settings.

---

## 2) Implementation phases

```text
Phase 0: API + schemas + keys
    ↓
Phase 1: useAccountProfile + account stack + hub
    ↓
Phase 2: Account info screen
    ↓
Phase 3: Edit profile
    ↓
Phase 4: Update avatar
    ↓
Phase 5: Privacy toggle
    ↓
Phase 6: Settings (appearance)
    ↓
Phase 7: Delete account + logout from hub
    ↓
Phase 8: Entry from ProfileScreen + cache invalidation polish
    ↓
Phase 9 (defer): Change password, security sessions, notifications tab
```

Each phase should be mergeable and testable on emulator before the next.

---

## 3) Phase 0 — Auth API foundation

**Goal:** Call account APIs with same auth/unwrap pattern as web.

### Extend `src/features/auth/api/authApi.js`

| Function | Port from web `authApi.js` |
|----------|----------------------------|
| `getMyProfile` | Yes |
| `updateMyProfile` | Yes |
| `requestAvatarUploadUrl` | Yes |
| `updateMyAvatar` | Yes |
| `updateMyPrivacy` | Yes |
| `updateMySettings` | Yes |
| `softDeleteMyAccount` | Yes |

### Create

| File | Action |
|------|--------|
| `src/features/auth/utils/accountSchemas.js` | Port from web `account/accountSchemas.js` |
| `src/features/auth/constants/accountKeys.js` | React Query keys |
| `src/shared/constants/routes.js` | Add `ROUTES.account*` helpers |

### API behavior docs

- `account-info-api-and-behavior.md`
- `edit-profile-api-and-behavior.md`

### Verify

- Manual: `GET /api/v1/users/me` returns `{ user, profile, settings }` with valid JWT.

---

## 4) Phase 1 — Account stack + hub

**Goal:** Navigate to account hub; show menu list with loading state.

### Create routes

| File |
|------|
| `app/account/_layout.jsx` |
| `app/account/index.jsx` |

### Create

| File | Source (web) |
|------|----------------|
| `hooks/useAccountProfile.js` | `useAccountProfile.jsx` — prefer React Query |
| `components/AccountHubScreen.jsx` | `AccountTabNav` + hub concept |
| `components/AccountMenuRow.jsx` | Nav row |
| `components/AccountScreenHeader.jsx` | `TabPanelHeader` from `authUi.jsx` |

### Update

| File | Work |
|------|------|
| `app/_layout.jsx` | Register `account` stack |

### Verify

- [ ] Navigate to `/account` after login
- [ ] Hub shows skeleton while loading
- [ ] Hub shows avatar + display name when ready
- [ ] Menu rows visible (navigation can be stub until later phases)

---

## 5) Phase 2 — Account info

**Goal:** Read-only account summary.

### Create

| File |
|------|
| `app/account/info.jsx` |
| `components/AccountInfoScreen.jsx` |
| `components/AccountInfoSection.jsx` |
| `components/AccountCard.jsx` |

### Port from web

- `AccountInfoTab.jsx` — sections, labels, status badge

### Verify

- [ ] Email, status, verified, phone, last login
- [ ] Profile summary with avatar
- [ ] Link **Chỉnh sửa hồ sơ** → `/account/edit` (screen can be stub)
- [ ] Error + **Thử lại**

---

## 6) Phase 3 — Edit profile

**Goal:** Update display name, bio, website, social links.

### Create

| File |
|------|
| `app/account/edit.jsx` |
| `components/EditProfileScreen.jsx` |
| `components/EditProfileForm.jsx` |
| `components/SocialLinksEditor.jsx` |
| `hooks/useEditProfile.js` |

### API behavior doc

- `edit-profile-api-and-behavior.md`

### Cache

- On success: invalidate `accountKeys.me()` + social `profileKeys` for current user

### Verify

- [ ] Form pre-filled from `getMyProfile`
- [ ] Client validation matches `validateEditProfileForm`
- [ ] Server field errors mapped to inputs
- [ ] Success toast; social profile shows updated bio/name

---

## 7) Phase 4 — Update avatar

**Goal:** Pick image, upload to MinIO, patch avatar URL.

### Create

| File |
|------|
| `app/account/avatar.jsx` |
| `components/UpdateAvatarScreen.jsx` |
| `hooks/useUpdateAvatar.js` |

### Native

- `expo-image-picker` (already used in social create post)
- `fetch` PUT to presigned URL with progress optional

### API behavior docs

- `AvatarUpload-api-and-behavior.md`
- `update-avatar-api-and-behavior.md`

### Verify

- [ ] Reject wrong type / > 5 MB
- [ ] Preview before upload
- [ ] Avatar updates on hub, info, social profile

---

## 8) Phase 5 — Privacy

**Goal:** Toggle private profile.

### Create

| File |
|------|
| `app/account/privacy.jsx` |
| `components/PrivacyScreen.jsx` |
| `hooks/usePrivacySettings.js` |

### API behavior doc

- `privacy-api-and-behavior.md`

### Verify

- [ ] Toggle optimistic with rollback on error
- [ ] Success messages match web
- [ ] Account info reflects new privacy state

---

## 9) Phase 6 — Settings (appearance)

**Goal:** Save `appearance_mode` to backend and apply locally.

### Create

| File |
|------|
| `app/account/settings.jsx` |
| `components/AccountSettingsScreen.jsx` |
| `components/AppearanceModePicker.jsx` |
| `hooks/useAccountSettings.js` |

### Optional port

- `AppearanceContext` / `normalizeAppearanceMode` from web auth utils

### API behavior doc

- `update-user-setting-api-and-behavior.md`

### Verify

- [ ] Three options: Sáng / Tối / Theo hệ thống
- [ ] Save persists after app restart (from API refetch)
- [ ] Theme applies immediately after save

---

## 10) Phase 7 — Delete account + logout

**Goal:** Soft-delete with password; logout from hub.

### Create

| File |
|------|
| `app/account/delete.jsx` |
| `components/DeleteAccountScreen.jsx` |
| `hooks/useDeleteAccount.js` |

### Wire hub

- **Đăng xuất** row: `logoutWithRefreshToken` + clear tokens → login

### API behavior doc

- `soft-delete-account-api-and-behavior.md`

### Verify

- [ ] Password required before confirm modal
- [ ] Wrong password shows field error
- [ ] Success clears session → login screen
- [ ] Logout from hub works without delete

---

## 11) Phase 8 — Navigation entry + polish

| Task | Notes |
|------|-------|
| ProfileScreen settings icon | Self only → `router.push(ROUTES.account)` |
| **Chỉnh sửa hồ sơ** on self profile | → `/account/edit` (match web `ProfileHero`) |
| Cache invalidation audit | After edit/avatar/privacy: social profile refetch |
| Shared toast | `useSocialToast()` for success/error copy |
| Hub danger styling | Delete row uses error color |
| Register all hub routes | Each menu row navigates to working screen |

---

## 12) Phase 9 (defer) — Password, security, notifications

| Task | Web reference |
|------|---------------|
| Change password | `ChangePasswordTab.jsx`, `/account/password` |
| Active sessions | `/account/security` |
| Notification preferences | `NotificationSettingsTab.jsx` |

---

## 13) Suggested AI task breakdown (one PR each)

| # | Task | Deliverable |
|---|------|-------------|
| 1 | Phase 0 | Extended `authApi` + `accountSchemas` + keys |
| 2 | Phase 1 | Account stack + hub |
| 3 | Phase 2 | Account info |
| 4 | Phase 3 | Edit profile |
| 5 | Phase 4 | Avatar upload |
| 6 | Phase 5–6 | Privacy + settings |
| 7 | Phase 7–8 | Delete + logout + profile entry |

---

## 14) Testing checklist (manual on emulator)

1. Start auth-service (3001).
2. Login with test user.
3. Open `/account` — info loads.
4. Edit display name and bio — social profile updates.
5. Upload new avatar — visible on profile tab.
6. Toggle privacy — reflected on account info.
7. Change appearance mode — UI theme updates.
8. Logout from hub — returns to login.
9. (Optional) Soft-delete test account on staging only.

---

## 15) Files not to create on mobile (v1)

| Web-only | Reason |
|----------|--------|
| `AccountSettingsLayout` as sidebar layout | Use hub + stack screens |
| `NotificationSettingsTab` in account v1 | Defer |
| Duplicate `docs/api_fe_behavior/*` | Shared repo docs |

---

## 16) Related documents

| Document | When to read |
|----------|--------------|
| `mobile/docs/mobile-account-scope.md` | Before any account task |
| `mobile/docs/mobile-account-ui-map.md` | When implementing a specific screen |
| `mobile/docs/mobile-account-screen-checklist.md` | Before marking screen done |
| `mobile/docs/mobile-account-rn-adaptations.md` | Web → RN patterns |
| `mobile/docs/mobile-convention.md` | Naming, folder layout |
| `mobile/AGENTS.md` | Agent entry point |

---

## 17) Prompt template

```text
Implement Phase [N] of mobile account per mobile/docs/mobile-account-implementation-order.md.

Read first:
- mobile/docs/mobile-account-scope.md
- mobile/docs/mobile-account-ui-map.md
- mobile/docs/mobile-account-implementation-order.md (Phase [N])
- mobile/docs/mobile-account-screen-checklist.md
- docs/api_fe_behavior/auth_api_fe_behavior/[relevant].md

Port from frontend/src/fe-module/features/auth/account/ where listed.
Do not modify backend. UTF-8 files only. No API calls in app/*.jsx.
Invalidate social profile cache after profile-changing mutations.
```
