# Mobile Account Screen Checklist - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Definition of Done per account screen — loading, error, validation, success, auth — so AI and reviewers verify each screen before merge.

---

## How to use

1. Pick one screen from the table below.
2. Read its **Web source**, **API behavior** docs, and **Stitch** folder.
3. Implement route under `app/account/` + logic under `src/features/auth/`.
4. Check every box in **States**, **Actions**, and **Quality** before marking done.
5. Cross-check `mobile/docs/mobile-account-ui-map.md` for component mapping.

**Global rules (all screens):**

- [ ] No `axios` / `fetch` in `app/*.jsx` (except presigned avatar PUT inside hook if needed)
- [ ] JWT attached via `authApiClient` (not manual headers in screens)
- [ ] Response unwrapped per `mobile/docs/mobile-api-integration.md`
- [ ] Colors from `src/shared/theme/colors.js`
- [ ] UTF-8 encoding on new files (Windows)
- [ ] Vietnamese copy matches web where specified below
- [ ] Passwords never logged

---

## Screen index

| # | Screen | Route | Phase |
|---|--------|-------|-------|
| 1 | Account hub | `app/account/index.jsx` | 1 |
| 2 | Account info | `app/account/info.jsx` | 2 |
| 3 | Edit profile | `app/account/edit.jsx` | 3 |
| 4 | Update avatar | `app/account/avatar.jsx` | 4 |
| 5 | Privacy | `app/account/privacy.jsx` | 5 |
| 6 | Settings | `app/account/settings.jsx` | 6 |
| 7 | Delete account | `app/account/delete.jsx` | 7 |

---

## 1) Account hub

| Field | Value |
|-------|-------|
| **Route** | `app/account/index.jsx` |
| **Web** | `AccountSettingsLayout` + `AccountTabNav.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_side_navigation_master_component/` |
| **API** | `account-info-api-and-behavior.md` |
| **Hook** | `useAccountProfile.js` |
| **Components** | `AccountHubScreen`, `AccountMenuRow` |

### States

- [ ] **Loading:** skeleton for header + menu rows
- [ ] **Error:** message + button **Thử lại** (calls `refetch`)
- [ ] **Ready:** avatar, display name, menu list

### Actions & navigation

- [ ] Each menu row pushes correct `/account/*` route
- [ ] **Đăng xuất** clears session → login
- [ ] Back returns to previous screen (profile or feed)

### Auth

- [ ] Requires login; 401 → session expired flow

### Quality

- [ ] Delete row styled as danger (error color)
- [ ] Header shows current avatar from profile

---

## 2) Account info

| Field | Value |
|-------|-------|
| **Route** | `app/account/info.jsx` |
| **Web** | `AccountInfoTab.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/account_info_classic_trust_desktop/` |
| **API** | `account-info-api-and-behavior.md`, `ProfileAccount-api-and-behavior.md` |
| **Hook** | `useAccountProfile.js` |

### States

- [ ] **Loading:** card skeletons
- [ ] **Error:** not found / network + **Thử lại**
- [ ] **Ready:** three/four sections rendered

### Content (read-only)

- [ ] **Tài khoản:** email, trạng thái badge, email verified, phone, last login
- [ ] **Hồ sơ:** avatar, display name, bio, website, social links, privacy label
- [ ] **Cài đặt (tóm tắt):** appearance mode label
- [ ] **Bảo mật:** link deferred or coming-soon (Phase 9)

### Actions

- [ ] **Chỉnh sửa hồ sơ** → `/account/edit`
- [ ] **Cập nhật cài đặt** → `/account/settings`
- [ ] **Đổi mật khẩu** — defer or coming-soon

### Auth

- [ ] Login required; only shows current user's data

---

## 3) Edit profile

| Field | Value |
|-------|-------|
| **Route** | `app/account/edit.jsx` |
| **Web** | `EditProfileTab.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_edit_profile_validation_errors/` |
| **API** | `edit-profile-api-and-behavior.md` |
| **Hook** | `useEditProfile.js` |
| **Utils** | `accountSchemas.js` — `validateEditProfileForm` |

### States

- [ ] **Loading:** form skeleton while profile loads
- [ ] **Idle:** pre-filled form from `getMyProfile`
- [ ] **Submitting:** disable save button
- [ ] **Field errors:** inline under inputs
- [ ] **Global error:** banner for non-field failures
- [ ] **Success:** toast — match web success message

### Validation (client)

- [ ] `display_name` required; max 100 with counter
- [ ] `bio` max 500 with counter
- [ ] `website` must be http/https if provided
- [ ] `social_links` max 10; each URL validated
- [ ] Server `errors[]` mapped via `resolveFieldErrors` pattern from web

### Actions

- [ ] Save → `PUT /api/v1/users/me/profile`
- [ ] Add / remove social link rows
- [ ] Platform picker per row (`SOCIAL_PLATFORMS`)
- [ ] Cancel / back without save (confirm optional if dirty)

### Cache

- [ ] On success: invalidate `accountKeys.me()` + social profile for current user

### Auth

- [ ] Login required

### Quality

- [ ] `KeyboardAvoidingView` on iOS/Android
- [ ] Multiline bio input

---

## 4) Update avatar

| Field | Value |
|-------|-------|
| **Route** | `app/account/avatar.jsx` |
| **Web** | `UpdateAvatarTab.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_update_avatar_active_upload_state/` |
| **API** | `AvatarUpload-api-and-behavior.md`, `update-avatar-api-and-behavior.md` |
| **Hook** | `useUpdateAvatar.js` |

### States

- [ ] **Idle:** shows current avatar
- [ ] **Preview:** local image after pick
- [ ] **Uploading:** progress or spinner
- [ ] **Error:** type/size/network message
- [ ] **Success:** toast + updated preview from server URL

### Validation

- [ ] Allowed: JPEG, PNG, WEBP
- [ ] Max 5 MB — message: `Tệp vượt quá 5MB.` (match web)
- [ ] Wrong type: `Định dạng không được hỗ trợ. Chỉ JPG, PNG, WEBP.`

### Actions

- [ ] Pick from gallery via `expo-image-picker`
- [ ] Upload flow: upload-url → PUT → PATCH avatar
- [ ] Remove / reset selection before upload (if web supports)

### Cache

- [ ] Refetch account profile; invalidate social profile avatar

### Auth

- [ ] Login required

---

## 5) Privacy

| Field | Value |
|-------|-------|
| **Route** | `app/account/privacy.jsx` |
| **Web** | `PrivacyTab.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_privacy_classic_desktop/` |
| **API** | `privacy-api-and-behavior.md` |
| **Hook** | `usePrivacySettings.js` |

### States

- [ ] **Loading:** toggle disabled until profile loads
- [ ] **Saving:** toggle disabled during request
- [ ] **Error:** inline message + rollback toggle

### Actions

- [ ] Toggle `is_private` → `PATCH /api/v1/users/me/privacy`
- [ ] Success enable: `Đã bật chế độ riêng tư.`
- [ ] Success disable: `Đã tắt chế độ riêng tư.`
- [ ] Explanatory copy from web about private profile behavior

### Auth

- [ ] Login required

---

## 6) Settings (appearance)

| Field | Value |
|-------|-------|
| **Route** | `app/account/settings.jsx` |
| **Web** | `SettingsTab.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_settings_tab_modern_cards/` |
| **API** | `update-user-setting-api-and-behavior.md` |
| **Hook** | `useAccountSettings.js` |

### States

- [ ] **Loading:** options skeleton
- [ ] **Idle:** current mode selected from profile
- [ ] **Submitting:** disable save
- [ ] **Error:** banner or inline
- [ ] **Success:** toast + theme applied

### Actions

- [ ] Select `LIGHT` | `DARK` | `SYSTEM`
- [ ] Save → `PATCH /api/v1/users/me/settings`
- [ ] Apply theme locally after save (`AppearanceContext` or equivalent)

### Labels (VN)

- [ ] Sáng / Tối / Theo hệ thống — match web `THEME_OPTIONS`

### Auth

- [ ] Login required

---

## 7) Delete account

| Field | Value |
|-------|-------|
| **Route** | `app/account/delete.jsx` |
| **Web** | `DeleteAccountTab.jsx` |
| **Stitch** | `frontend/stitch/account_setting_screen/profile_delete_account_complete_flow/` |
| **API** | `soft-delete-account-api-and-behavior.md` |
| **Hook** | `useDeleteAccount.js` |

### States

- [ ] **Idle:** danger zone UI + password field
- [ ] **Confirm modal:** second step before delete
- [ ] **Submitting:** disable confirm
- [ ] **Field error:** wrong password on input
- [ ] **Success:** session cleared → login

### Actions

- [ ] Password required — `Vui lòng nhập mật khẩu hiện tại.`
- [ ] Wrong password — `Mật khẩu không chính xác.`
- [ ] Confirm → `POST /api/v1/users/me/soft-delete`
- [ ] Success message: `Tài khoản đã được xóa.`

### Auth

- [ ] Login required
- [ ] After delete: user cannot access protected routes

### Quality

- [ ] Password field secure entry
- [ ] Modal accessible on small screens

---

## Reviewer quick pass (any screen)

| Check | Pass? |
|-------|-------|
| Matches `mobile-account-scope.md` (no out-of-scope features) | |
| Matches `mobile-account-ui-map.md` route | |
| All states handled where applicable | |
| Tested on Android emulator with auth-service :3001 | |
| Social profile cache invalidated after profile mutations | |
| No secrets in logs | |

---

## Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-account-scope.md` | In/out of scope |
| `mobile/docs/mobile-account-ui-map.md` | Route + component map |
| `mobile/docs/mobile-account-implementation-order.md` | Build phase |
| `mobile/docs/mobile-account-rn-adaptations.md` | Web → RN patterns |
| `mobile/docs/mobile-design-system.md` | Visual tokens |
