# Mobile Account UI Map - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Map every web account settings surface to its mobile route, components, and design references — so AI ports layout correctly (stack screens, not sidebar tabs).

---

## 1) Layout principle (web vs mobile)

| Web (`AccountPage`) | Mobile |
|---------------------|--------|
| `AccountSettingsLayout` + left `AccountTabNav` sidebar | **Hub menu list** at `/account` → push child screens |
| Tab state in one page (`useState("info")`) | **expo-router stack** — one route per tab |
| `AuthAlert` banner at top of layout | `useSocialToast()` or inline banner — match message text |
| Tailwind `AccountCard` | `View` + `colors` from `shared/theme/colors.js` |
| `<input type="file">` for avatar | `expo-image-picker` |
| `react-router` `Link` between tabs | `router.push()` between account routes |
| Desktop sidebar shows avatar preview | Avatar on hub header + each relevant screen |

---

## 2) Route map (expo-router)

Suggested file structure under `mobile/app/`:

```text
app/
├── account/
│   ├── _layout.jsx              # Stack navigator for account screens
│   ├── index.jsx                # Account hub (menu list)
│   ├── info.jsx                 # Account info (read-only)
│   ├── edit.jsx                 # Edit profile form
│   ├── avatar.jsx               # Update avatar
│   ├── privacy.jsx              # Privacy toggle
│   ├── settings.jsx             # Appearance / settings
│   └── delete.jsx               # Delete account
├── (tabs)/
│   └── profile.jsx              # UNCHANGED — social ProfileScreen (not account hub)
└── ...
```

**Phase 8 (defer):**

```text
app/account/
├── password.jsx                 # Change password (web: /account/password)
└── security.jsx                 # Sessions (web: /account/security)
```

Add helpers to `src/shared/constants/routes.js` when implemented:

```javascript
account: "/account",
accountInfo: "/account/info",
accountEdit: "/account/edit",
accountAvatar: "/account/avatar",
accountPrivacy: "/account/privacy",
accountSettings: "/account/settings",
accountDelete: "/account/delete",
```

Deep link examples (scheme `twohands://`):

| Path | Example |
|------|---------|
| Account hub | `twohands://account` |
| Edit profile | `twohands://account/edit` |

---

## 3) Web tabs → mobile screens

Web tab IDs from `accountTabs.js`:

| Tab ID | Label (VN) | Web component | Mobile route | Mobile screen component |
|--------|------------|---------------|--------------|-------------------------|
| `info` | Thông tin tài khoản | `AccountInfoTab.jsx` | `/account/info` | `AccountInfoScreen.jsx` |
| `edit` | Chỉnh sửa hồ sơ | `EditProfileTab.jsx` | `/account/edit` | `EditProfileScreen.jsx` |
| `avatar` | Ảnh đại diện | `UpdateAvatarTab.jsx` | `/account/avatar` | `UpdateAvatarScreen.jsx` |
| `privacy` | Quyền riêng tư | `PrivacyTab.jsx` | `/account/privacy` | `PrivacyScreen.jsx` |
| `settings` | Cài đặt | `SettingsTab.jsx` | `/account/settings` | `AccountSettingsScreen.jsx` |
| `notifications` | Thông báo | `NotificationSettingsTab.jsx` | — | **Defer** (out of scope v1) |
| `delete` | Xóa tài khoản | `DeleteAccountTab.jsx` | `/account/delete` | `DeleteAccountScreen.jsx` |

**Hub screen** (`/account/index`): list of menu rows mirroring v1 tabs + **Đăng xuất** row. Each row pushes the corresponding route.

---

## 4) Page-by-page mapping

### 4.1 Account hub

| | |
|--|--|
| **Web** | `AccountSettingsLayout` + `AccountTabNav` (navigation only) |
| **Mobile route** | `app/account/index.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_side_navigation_master_component/` |
| **Component** | `AccountHubScreen.jsx` |

**Hub rows (v1):**

| Row | Navigates to |
|-----|--------------|
| Thông tin tài khoản | `/account/info` |
| Chỉnh sửa hồ sơ | `/account/edit` |
| Ảnh đại diện | `/account/avatar` |
| Quyền riêng tư | `/account/privacy` |
| Cài đặt | `/account/settings` |
| Xóa tài khoản (danger) | `/account/delete` |
| Đăng xuất | Calls `logoutWithRefreshToken` + clear session |

Show user avatar + display name in hub header (from `useAccountProfile`).

---

### 4.2 Account info

| | |
|--|--|
| **Web** | `AccountInfoTab.jsx` |
| **Mobile route** | `app/account/info.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/account_info_classic_trust_desktop/` |
| **API behavior** | `account-info-api-and-behavior.md`, `ProfileAccount-api-and-behavior.md` |
| **Hook** | `useAccountProfile.js` |

**Sections (read-only):** Tài khoản, Hồ sơ, Cài đặt (tóm tắt), Bảo mật (link defer).

**Quick actions:** Chỉnh sửa hồ sơ → `/account/edit`; Cập nhật cài đặt → `/account/settings`.

---

### 4.3 Edit profile

| | |
|--|--|
| **Web** | `EditProfileTab.jsx` |
| **Mobile route** | `app/account/edit.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_edit_profile_validation_errors/` |
| **API behavior** | `edit-profile-api-and-behavior.md` |
| **Hook** | `useEditProfile.js` |
| **Utils** | `accountSchemas.js` |

**Form fields:** `display_name` (required, max 100), `bio` (max 500), `website`, `social_links` (max 10 rows).

---

### 4.4 Update avatar

| | |
|--|--|
| **Web** | `UpdateAvatarTab.jsx` |
| **Mobile route** | `app/account/avatar.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_update_avatar_active_upload_state/` |
| **API behavior** | `AvatarUpload-api-and-behavior.md`, `update-avatar-api-and-behavior.md` |
| **Hook** | `useUpdateAvatar.js` |

**Flow:** pick image → validate → upload-url → PUT presigned → PATCH avatar → refetch + invalidate social cache.

---

### 4.5 Privacy

| | |
|--|--|
| **Web** | `PrivacyTab.jsx` |
| **Mobile route** | `app/account/privacy.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_privacy_classic_desktop/` |
| **API behavior** | `privacy-api-and-behavior.md` |
| **Hook** | `usePrivacySettings.js` |

Toggle `is_private` with web success copy.

---

### 4.6 Settings (appearance)

| | |
|--|--|
| **Web** | `SettingsTab.jsx` |
| **Mobile route** | `app/account/settings.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_settings_tab_modern_cards/` |
| **API behavior** | `update-user-setting-api-and-behavior.md` |
| **Hook** | `useAccountSettings.js` |

Options: `LIGHT` | `DARK` | `SYSTEM`.

---

### 4.7 Delete account

| | |
|--|--|
| **Web** | `DeleteAccountTab.jsx` |
| **Mobile route** | `app/account/delete.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_delete_account_complete_flow/` |
| **API behavior** | `soft-delete-account-api-and-behavior.md` |
| **Hook** | `useDeleteAccount.js` |

Password confirm → modal → soft-delete → clear session → login.

---

## 5) Entry points (navigation)

| Source | Action | Destination |
|--------|--------|-------------|
| Self `ProfileScreen` | Settings icon in header | `/account` |
| Self `ProfileScreen` | **Chỉnh sửa hồ sơ** button | `/account/edit` |
| Account info | Quick links | `/account/edit`, `/account/settings` |

**Do not** change `(tabs)/profile` to render account hub.

---

## 6) Component library (`src/features/auth/components/`)

| Component | Used in |
|-----------|---------|
| `AccountHubScreen.jsx` | Hub |
| `AccountMenuRow.jsx` | Hub list item |
| `AccountInfoScreen.jsx` | Info |
| `EditProfileScreen.jsx` | Edit |
| `EditProfileForm.jsx` | Edit form |
| `SocialLinksEditor.jsx` | Edit |
| `UpdateAvatarScreen.jsx` | Avatar |
| `PrivacyScreen.jsx` | Privacy |
| `AccountSettingsScreen.jsx` | Settings |
| `AppearanceModePicker.jsx` | Settings |
| `DeleteAccountScreen.jsx` | Delete |
| `AccountCard.jsx` | Shared card |
| `AccountTextInput.jsx` | Shared input |
| `AccountScreenHeader.jsx` | Title + subtitle |

---

## 7) Hooks map

| Web | Mobile |
|-----|--------|
| `useAccountProfile.jsx` | `hooks/useAccountProfile.js` |
| `EditProfileTab` submit | `hooks/useEditProfile.js` |
| `UpdateAvatarTab` upload | `hooks/useUpdateAvatar.js` |
| `PrivacyTab` toggle | `hooks/usePrivacySettings.js` |
| `SettingsTab` submit | `hooks/useAccountSettings.js` |
| `DeleteAccountTab` | `hooks/useDeleteAccount.js` |

**Query keys:** `constants/accountKeys.js` — `accountKeys.me()`.

---

## 8) Web route → mobile route

| Web | Mobile |
|-----|--------|
| `/account-profile` (hub) | `/account` |
| tab `info` | `/account/info` |
| tab `edit` | `/account/edit` |
| tab `avatar` | `/account/avatar` |
| tab `privacy` | `/account/privacy` |
| tab `settings` | `/account/settings` |
| tab `delete` | `/account/delete` |
| `/account/password` | `/account/password` — Phase 8 |
| `/social/users/:userId` (self) | `/(tabs)/profile` — **social** |

---

## 9) Design tokens

1. `frontend/stitch/account_setting_screen/service_professionalism/DESIGN.md`
2. Stitch `screen.png` per section
3. `mobile/src/shared/theme/colors.js`
4. `mobile/docs/mobile-design-system.md`

---

## 10) AI prompt snippet

```text
Implement mobile account screen: [ROUTE]

Read:
- mobile/docs/mobile-account-scope.md
- mobile/docs/mobile-account-ui-map.md (section [X])
- mobile/docs/mobile-account-screen-checklist.md (section [N])
- docs/api_fe_behavior/auth_api_fe_behavior/[API].md
- frontend/src/fe-module/features/auth/account/components/[Tab].jsx

Stitch: frontend/stitch/account_setting_screen/[folder]/
No API in app/*.jsx. Invalidate social profile cache after profile mutations.
```
